package org.scalablet.components.bed.core.impl;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.common.NamedThreadFactory;
import org.scalablet.components.bed.core.BedConfiguration;
import org.scalablet.components.bed.core.BedDispatcher;
import org.scalablet.components.bed.core.BedExecutorRegistry;
import org.scalablet.components.bed.core.BedRunner;
import org.scalablet.components.bed.core.BedTask;
import org.scalablet.components.bed.core.BedTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * 定时轮询数据库的分发器
 *
 * @author abomb4 2021-10-14 19:19:23
 */
public class BedDispatcherLoopImpl implements BedDispatcher {

    /** 主日志 */
    private static final Logger log = LoggerFactory.getLogger(BedDispatcherLoopImpl.class);
    /** 分发器日志 */
    private static final Logger dispatcherLog = LoggerFactory.getLogger(DispatcherTask.class);

    /** 分发器配置 */
    private final BedConfiguration.BedDispatcherConfig dispatcherConfig;
    /** 仓储 */
    private final BedTaskRepository taskRepository;
    /** 执行器注册中心 */
    private final BedExecutorRegistry executorRegistry;
    /** 任务运行执行器 */
    private final BedRunner runner;

    /** 分发线程池 */
    private final ScheduledThreadPoolExecutor dispatcherPool = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory(((factoryId, threadId) -> "bed-dispatcher-" + threadId)) {
            });

    /** inited */
    private boolean notInited = true;

    /**
     * 构造一个分发器，必须调用 {@link #init()} 后才能使用
     *
     * @param dispatcherConfig 分发器配置
     * @param taskRepository   仓储
     * @param executorRegistry 任务执行器注册中心
     * @param runner           任务执行器
     */
    public BedDispatcherLoopImpl(BedConfiguration.BedDispatcherConfig dispatcherConfig,
                                 BedTaskRepository taskRepository,
                                 BedExecutorRegistry executorRegistry,
                                 BedRunner runner) {
        this.dispatcherConfig = dispatcherConfig;
        this.taskRepository = taskRepository;
        this.executorRegistry = executorRegistry;
        this.runner = runner;
    }

    /**
     * init
     */
    public void init() {
        if (this.notInited) {
            this.executorRegistry.getAllExecutors().stream()
                    .map(BedExecutor::getThreadResourceName)
                    .distinct()
                    .forEach(resourceName -> {
                        final BedConfiguration.DispatcherResourceConfig config = this.dispatcherConfig.getResources()
                                .getOrDefault(resourceName, this.dispatcherConfig.getDefaultConfig());
                        final int loopSecond = config.getLoopIntervalSeconds();
                        final int randomSeconds = config.getLoopIntervalRandomSeconds();
                        final long random = RandomGeneratorFactory.of("Random").create().nextLong(randomSeconds);
                        final long waits = random + loopSecond;
                        final DispatcherTask task = new DispatcherTask(resourceName, randomSeconds, loopSecond,
                                this.dispatcherConfig.getLockExpirationSeconds());
                        this.dispatcherPool.schedule(task, waits, TimeUnit.SECONDS);
                        log.info("BedDispatcher task will start after {}s.", waits);
                    });
            this.notInited = false;
        }
    }

    @Override
    public void taskCreated(BedTask bedTask) {
        // do nothing, nobody could break my loop
    }

    /**
     * 分发器任务，会不停将自己塞入定时线程池中实现不断轮询。
     * <p>
     * 持续从仓储中获取任务，然后塞入 {@link BedRunner} 中去执行。
     */
    private class DispatcherTask implements Runnable {

        /** 随机数生成器 */
        final RandomGenerator random = RandomGeneratorFactory.of("Random").create();

        /** 这个分发任务已经执行了多少次了 */
        int runCounter = 0;
        /** 资源名称 */
        final String resourceName;
        /** 随机范围，0 - randomSeconds */
        final int randomSeconds;
        /** 基础轮询间隔 */
        final int loopSecond;
        /** 锁过期秒数 */
        final long lockExpirationSeconds;

        /**
         * 构造
         *  @param resourceName          资源名称
         * @param randomSeconds         随机范围，0 - randomSeconds
         * @param loopSeconds           基础轮询间隔
         * @param lockExpirationSeconds 锁过期秒数
         */
        private DispatcherTask(String resourceName, int randomSeconds, int loopSeconds, long lockExpirationSeconds) {
            this.resourceName = resourceName;
            this.randomSeconds = randomSeconds;
            this.loopSecond = loopSeconds;
            this.lockExpirationSeconds = lockExpirationSeconds;
        }

        @Override
        public void run() {
            // 1. find tasks
            // 2. push into Runner
            this.runCounter += 1;
            try {
                final List<BedTask> someTasks = BedDispatcherLoopImpl.this.taskRepository.getSomeNeedRunTasks(
                        100, this.resourceName, this.lockExpirationSeconds);
                for (BedTask task : someTasks) {
                    BedDispatcherLoopImpl.this.runner.submitImmediately(task);
                }
                dispatcherLog.info("({}) Dispatcher dispatched {} tasks", this.runCounter, someTasks.size());
            } catch (Exception e) {
                dispatcherLog.warn("({}) Unexpected exception in dispatcher task! Continues to run periodically.",
                        this.runCounter, e);
            } finally {
                final long seconds = this.random.nextLong(this.randomSeconds) + this.loopSecond;
                BedDispatcherLoopImpl.this.dispatcherPool.schedule(this, seconds, TimeUnit.SECONDS);
                dispatcherLog.debug("({}) next iteration after {}s.", this.runCounter, seconds);
            }
        }
    }
}
