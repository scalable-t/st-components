package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.BedResult;
import org.scalablet.components.bed.EnumBedResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task Runner, manage thread pools
 *
 * @author abomb4 2021-10-14 19:19:23
 */
public class BedRunner {

    /** main log */
    private static final Logger log = LoggerFactory.getLogger(BedRunner.class);
    /** worker log */
    private static final Logger workerLog = LoggerFactory.getLogger(WorkerTask.class);

    /** config */
    private final BedConfiguration.BedRunnerConfig runnerConfig;
    /** Repository */
    private final BedTaskRepository taskRepository;
    /** Executor registry, stores executors singleton instance */
    private final BedExecutorRegistry executorRegistry;
    /** Serializer */
    private final BedSerializer serializer;

    /** Pool of every type of resource of executor */
    private final ResourceThreadPoolManagement poolPool;

    /**
     * Construct a runner
     *
     * @param runnerConfig     config of runner
     * @param taskRepository   repository
     * @param executorRegistry executor registry
     * @param serializer       serializer
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
     * Run executor once in caller thread, will save task result to repository
     *
     * @param task task info
     * @param <T>  type of cmd
     * @return run result
     */
    public <T extends BedExecutorCmd> BedResult runImmediately(BedTask task) {

        final BedExecutor<T> executor;
        final T cmd;
        try {
            executor = executorRegistry.getExecutor(task.getExecutorType());
            cmd = serializer.deserialize(task.getCmd(), executor.getCmdClass());
        } catch (Exception e) {
            log.error("Task {} failed to run! Tag it as unrecognized! executorType: {}", task.getTaskId(),
                    task.getExecutorType(), e);
            unrecognized(task, e);
            return BedResult.unrecognized(e.getMessage());
        }
        return new WorkerTask<T>(executor, cmd, task, taskRepository).call();
    }

    /**
     * Submit a task to specific pool that find by {@link BedExecutor#getThreadResourceName}
     *
     * @param task task info
     * @param <T>  type of cmd
     */
    public <T extends BedExecutorCmd> void submitImmediately(BedTask task) {
        final BedExecutor<T> executor;
        final T cmd;
        try {
            executor = executorRegistry.getExecutor(task.getExecutorType());
            cmd = serializer.deserialize(task.getCmd(), executor.getCmdClass());
        } catch (Exception e) {
            log.error("Task {} failed to run! Tag it as unrecognized! executorType: {}", task.getTaskId(),
                    task.getExecutorType(), e);
            unrecognized(task, e);
            return;
        }

        final WorkerTask<T> worker = new WorkerTask<>(executor, cmd, task, taskRepository);
        final String resourceName = executor.getThreadResourceName();
        poolPool.getPool(resourceName).submit(worker);
    }

    /**
     * Tag this task as unrecognized
     *
     * @param task task
     * @param e    exception
     */
    private void unrecognized(BedTask task, Exception e) {
        task.tagAsUnrecognized(e.toString());
        taskRepository.save(task);
    }

    /**
     * Get available threads of specific resource
     *
     * @param resourceName resource name
     * @return available
     */
    public int getAvailableThreads(String resourceName) {
        final ScheduledThreadPoolExecutor pool = poolPool.getPool(resourceName);
        return pool.getMaximumPoolSize() - pool.getActiveCount();
    }

    /**
     * Worker task, call {@link BedExecutor#execute(BedExecutorCmd)}
     *
     * @param <T> type of cmd
     */
    private static class WorkerTask<T extends BedExecutorCmd> implements Callable<BedResult> {
        /** executor instance */
        final BedExecutor<T> executor;
        /** cmd */
        final T cmd;
        /** simple executor name */
        final String simpleName;
        /** task */
        final BedTask bedTask;
        /** final BedTaskRepository taskRepository */
        final BedTaskRepository taskRepository;

        private WorkerTask(BedExecutor<T> executor, T cmd, BedTask bedTask, BedTaskRepository taskRepository) {
            this.executor = executor;
            this.cmd = cmd;
            this.simpleName = executor.getClass().getSimpleName();
            this.bedTask = bedTask;
            this.taskRepository = taskRepository;
        }

        @Override
        public BedResult call() {
            BedResult execute;
            Exception ex = null;
            try {
                workerLog.info("Prepare to call executor of type {}, cmd: {}",
                        simpleName, cmd);
                execute = executor.execute(cmd);
            } catch (Exception e) {
                ex = e;
                execute = BedResult.incomplete(e.getMessage());
            }

            if (execute.getResultType() == EnumBedResultType.COMPLETED) {
                workerLog.info("call executor {} task id {} completed.", simpleName, cmd.getTaskId());
                this.finish();
            } else {
                final int newExecutedTimes = bedTask.getExecutedTimes() + 1;
                final BedExecutor.RetryControl retryControl = executor.shouldRetry(cmd, newExecutedTimes);
                if (retryControl.isShouldRetry()) {
                    final int delaySeconds = retryControl.getDelaySeconds();
                    logConditional(
                            "Call executor {} task id {} incomplete, will retry in {} seconds.",
                            "Call executor {} task id {} cause exception, will retry in {} seconds.",
                            simpleName, cmd.getTaskId(), delaySeconds, ex);
                    this.increaseExecutedCount(retryControl, execute.getMessage());
                } else {
                    logConditional(
                            "Call executor {} task id {} incomplete, tried {} times, max retry times exceeded.",
                            "Call executor {} task id {} cause exception, tried {} times, max retry times exceeded.",
                            simpleName, cmd.getTaskId(), newExecutedTimes);
                    this.maxRetryExceeded(retryControl, execute.getMessage());
                }
            }
            return execute;
        }

        /**
         * Assume last element of objs is a nullable exception.
         *
         * @param okLog if no exception log
         * @param exLog exception log
         * @param objs  log elements
         */
        private void logConditional(String okLog, String exLog, Object... objs) {
            final Object lastObj = objs[objs.length - 1];
            if (lastObj instanceof Exception) {
                workerLog.warn(exLog, objs);
            } else {
                workerLog.info(okLog, objs);
            }
        }

        /**
         * Finish this task normally
         */
        private void finish() {
            // finish this task
            bedTask.successExecuted();
            taskRepository.finishTask(bedTask);
        }

        /**
         * Not finished, increase counter
         *
         * @param retryControl Retry control
         * @param message      last message
         */
        private void increaseExecutedCount(BedExecutor.RetryControl retryControl, String message) {
            // increase count, update last message
            bedTask.failExecuted(retryControl, message);
            taskRepository.updateExecuted(bedTask);
        }

        /**
         * Max retry exceeded
         *
         * @param retryControl Retry control
         * @param message      last message
         */
        private void maxRetryExceeded(BedExecutor.RetryControl retryControl, String message) {
            // increase count, update last message and status
            bedTask.failExecuted(retryControl, message);
            taskRepository.updateExecuted(bedTask);
        }
    }

    /** Named thread factory */
    private abstract static class NamedTf implements ThreadFactory {
        /** static factory id */
        static final AtomicInteger FACTORY_ID_GEN = new AtomicInteger(0);
        /** Thread id generator in factory */
        final AtomicInteger threadIdGen = new AtomicInteger(0);
        /** final factory id */
        final int factoryId = FACTORY_ID_GEN.incrementAndGet();
        /** name maker */
        final Func func;

        /**
         * Construct with name maker function
         *
         * @param func name maker function
         */
        private NamedTf(Func func) {
            this.func = func;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, makeName(this.factoryId, this.threadIdGen.incrementAndGet()));
        }

        /**
         * make thread name
         *
         * @param factoryId id of this factory
         * @param threadId  thread id
         * @return thread name
         */
        protected String makeName(int factoryId, int threadId) {
            return func.makeName(factoryId, threadId);
        }
    }

    /** Make thread name */
    private interface Func {

        /**
         * Make thread name
         *
         * @param factoryId factory id
         * @param threadId  thread id in factory
         * @return thread name
         */
        String makeName(int factoryId, int threadId);
    }

    /** Pool management */
    private class ResourceThreadPoolManagement {
        /** key: resourceName, value: pool */
        final ConcurrentHashMap<String, ScheduledThreadPoolExecutor> poolMap = new ConcurrentHashMap<>();

        /**
         * Get thread pool configuration of resource
         *
         * @param resourceName Resource name
         * @return pool
         */
        @NonNull
        ScheduledThreadPoolExecutor getPool(String resourceName) {
            return poolMap.computeIfAbsent(resourceName, key ->
                    createExecutor(runnerConfig.getPoolConfig(key)));
        }

        /**
         * Create pool by config
         *
         * @param config config
         * @return pool
         */
        private ScheduledThreadPoolExecutor createExecutor(BedConfiguration.BedPoolConfig config) {
            return new ScheduledThreadPoolExecutor(config.getCoreSize(), new NamedTf(
                    (fid, tid) -> "BED-" + fid + "-WORK-" + tid) {
            });
        }
    }
}
