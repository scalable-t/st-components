package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.BedResult;
import org.scalablet.components.bed.EnumBedResultType;
import org.scalablet.components.bed.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 任务运行执行器，管理线程池，自动在仓储中更新任务状态
 *
 * @author abomb4 2021-10-14 19:19:23
 */
public class BedRunner {

    /** 主日志 */
    private static final Logger log = LoggerFactory.getLogger(BedRunner.class);
    /** 工作日志 */
    private static final Logger workerLog = LoggerFactory.getLogger(WorkerTask.class);

    /** 执行器配置 */
    private final BedConfiguration.BedRunnerConfig runnerConfig;
    /** 仓储 */
    private final BedTaskRepository taskRepository;
    /** 异步执行器注册中心，存储异步执行器的实例 */
    private final BedExecutorRegistry executorRegistry;
    /** cmd 序列化功能 */
    private final BedSerializer serializer;

    /** 线程资源管理器 */
    private final ResourceThreadPoolManagement poolPool;

    /**
     * 构建
     *
     * @param runnerConfig     执行器配置
     * @param taskRepository   仓储
     * @param executorRegistry 异步执行器注册中心，存储异步执行器的实例
     * @param serializer       cmd 序列化功能
     */
    public BedRunner(BedConfiguration.BedRunnerConfig runnerConfig, BedTaskRepository taskRepository,
                     BedExecutorRegistry executorRegistry, BedSerializer serializer) {
        this.runnerConfig = runnerConfig;
        this.taskRepository = taskRepository;
        this.executorRegistry = executorRegistry;
        this.serializer = serializer;
        this.poolPool = new ResourceThreadPoolManagement();
    }

    /**
     * 在调用线程立即执行，会在仓储层更新任务状态
     *
     * @param task 任务信息
     * @param <T>  任务参数类型
     * @return 执行结果
     */
    public <T extends BedExecutorCmd> BedResult runImmediately(BedTask task) {

        final BedExecutor<T> executor;
        final T cmd;
        final String executorName = task.getExecutorType();
        try {
            executor = this.executorRegistry.getExecutor(executorName);
            cmd = this.serializer.deserialize(task.getCmd(), executor.getCmdClass());
        } catch (Exception e) {
            log.error("Task {} failed to run! Tag it as unrecognized! executorType: {}", task.getTaskId(),
                    executorName, e);
            this.unrecognized(task, e);
            return BedResult.unrecognized(e.getMessage());
        }
        return new WorkerTask<T>(executor, cmd, executorName, task, this.taskRepository).call();
    }

    /**
     * 提交任务线程池，线程池根据 {@link BedExecutor#getThreadResourceName} 来寻找
     *
     * @param task 任务信息
     * @param <T>  任务参数类型
     */
    public <T extends BedExecutorCmd> void submitImmediately(BedTask task) {
        final BedExecutor<T> executor;
        final String executorName = task.getExecutorType();
        final T cmd;
        try {
            executor = this.executorRegistry.getExecutor(executorName);
            cmd = this.serializer.deserialize(task.getCmd(), executor.getCmdClass());
        } catch (Exception e) {
            log.error("Task {} failed to run! Tag it as unrecognized! executorType: {}", task.getTaskId(),
                    executorName, e);
            this.unrecognized(task, e);
            return;
        }

        final WorkerTask<T> worker = new WorkerTask<>(executor, cmd, executorName, task, this.taskRepository);
        final String resourceName = executor.getThreadResourceName();
        this.poolPool.getPool(resourceName).submit(worker);
    }

    /**
     * 将任务标记为未知，一般是在 {@link BedExecutorRegistry} 中无法找到任务，或者无法序列化导致
     *
     * @param task 任务信息
     * @param e    异常信息
     */
    private void unrecognized(BedTask task, Exception e) {
        task.tagAsUnrecognized(e.toString());
        this.taskRepository.save(task);
    }

    /**
     * 获取某种资源剩余可用线程数
     *
     * @param resourceName 资源名称
     * @return 空闲线程数
     */
    public int getAvailableThreads(String resourceName) {
        final ScheduledThreadPoolExecutor pool = this.poolPool.getPool(resourceName);
        return pool.getCorePoolSize() - pool.getActiveCount();
    }

    /**
     * 工作线程，会调用 {@link BedExecutor#execute(BedExecutorCmd)}
     *
     * @param <T> 任务参数类型
     */
    private static class WorkerTask<T extends BedExecutorCmd> implements Callable<BedResult> {
        /** 执行器实例 */
        final BedExecutor<? super T> executor;
        /** 已反序列化的参数 */
        final T cmd;
        /** 执行器名称 */
        final String executorName;
        /** 任务实例 */
        final BedTask bedTask;
        /** 任务仓储 */
        final BedTaskRepository taskRepository;

        /**
         * 非公开构造
         *
         * @param executor       执行器实例
         * @param cmd            已反序列化的参数
         * @param executorName   执行器正式名称
         * @param bedTask        任务实例
         * @param taskRepository 任务仓储
         */
        private WorkerTask(BedExecutor<? super T> executor, T cmd, String executorName, BedTask bedTask,
                           BedTaskRepository taskRepository) {
            this.executor = executor;
            this.cmd = cmd;
            this.executorName = executorName;
            this.bedTask = bedTask;
            this.taskRepository = taskRepository;
        }

        @Override
        public BedResult call() {
            BedResult execute;
            Exception ex = null;
            try {
                workerLog.info("Prepare to call executor of type {}, cmd: {}", this.executorName, this.cmd);
                execute = this.executor.execute(this.cmd);
            } catch (Exception e) {
                ex = e;
                execute = BedResult.incomplete(e.getMessage());
            }

            if (execute.getResultType() == EnumBedResultType.COMPLETED) {
                workerLog.info("call executor {} task id {} completed.", this.executorName, this.cmd.getTaskId());
                this.finish();
            } else {
                final int newExecutedTimes = this.bedTask.getExecutedTimes() + 1;
                final BedExecutor.RetryControl retryControl = this.executor.shouldRetry(this.cmd, newExecutedTimes);
                if (retryControl.isShouldRetry()) {
                    final int delaySeconds = retryControl.getDelaySeconds();
                    logConditional(
                            "Call executor {} task id {} incomplete, will retry in {} seconds.",
                            "Call executor {} task id {} cause exception, will retry in {} seconds.",
                            this.executorName, this.cmd.getTaskId(), delaySeconds, ex);
                    this.increaseExecutedCount(retryControl, execute.getMessage());
                } else {
                    logConditional(
                            "Call executor {} task id {} incomplete, tried {} times, max retry times exceeded.",
                            "Call executor {} task id {} cause exception, tried {} times, max retry times exceeded.",
                            this.executorName, this.cmd.getTaskId(), newExecutedTimes);
                    this.maxRetryExceeded(retryControl, execute.getMessage());
                }
            }
            return execute;
        }

        /**
         * 假设 objs 最后一个参数是一个可能为空的异常
         *
         * @param okLog 没有异常的 log
         * @param exLog 出现异常的 log
         * @param objs  日志参数
         */
        private static void logConditional(String okLog, String exLog, Object... objs) {
            final Object lastObj = objs[objs.length - 1];
            if (lastObj instanceof Exception) {
                workerLog.warn(exLog, objs);
            } else {
                workerLog.info(okLog, objs);
            }
        }

        /**
         * 正常结束一个任务
         */
        private void finish() {
            // finish this task
            this.bedTask.successExecuted();
            this.taskRepository.updateExecuted(this.bedTask);
        }

        /**
         * 任务未结束，更新任务状态，增加执行次数
         *
         * @param retryControl 重试控制
         * @param message      执行信息
         */
        private void increaseExecutedCount(BedExecutor.RetryControl retryControl, String message) {
            // increase count, update last message
            this.bedTask.failExecuted(retryControl, message);
            this.taskRepository.updateExecuted(this.bedTask);
        }

        /**
         * 达到最大重试次数
         *
         * @param retryControl 重试控制信息
         * @param message      last message
         */
        private void maxRetryExceeded(BedExecutor.RetryControl retryControl, String message) {
            // increase count, update last message and status
            this.bedTask.failExecuted(retryControl, message);
            this.taskRepository.updateExecuted(this.bedTask);
        }
    }

    /** 线程池管理器 */
    private class ResourceThreadPoolManagement {
        /** key: resourceName, value: pool */
        final ConcurrentHashMap<String, ScheduledThreadPoolExecutor> poolMap = new ConcurrentHashMap<>(16);
        /** 默认执行器 */
        final ScheduledThreadPoolExecutor defaultPool = createExecutor(BedRunner.this.runnerConfig.getDefaultConfig());

        /**
         * 获取某个资源的线程池。如果这个资源没有相关配置，则与其他人共用默认线程池。
         *
         * @param resourceName 资源名称
         * @return 线程池
         */
        @Nonnull
        ScheduledThreadPoolExecutor getPool(String resourceName) {
            return this.poolMap.computeIfAbsent(resourceName, key ->
                    Optional.ofNullable(BedRunner.this.runnerConfig.getBedPoolConfigList())
                            .flatMap(list -> list.stream().filter(v -> v.getName().equals(resourceName)).findAny())
                            .map(ResourceThreadPoolManagement::createExecutor)
                            .orElse(defaultPool)
            );
        }

        /**
         * 根据配置创建一个线程池
         *
         * @param config 配置
         * @return 线程池
         */
        private static ScheduledThreadPoolExecutor createExecutor(BedConfiguration.BedPoolConfig config) {
            return new ScheduledThreadPoolExecutor(config.getCoreSize(), new NamedThreadFactory(
                    (fid, tid) -> "BED-" + config.getName() + "-" + fid + "-WORK-" + tid) {
            });
        }
    }
}
