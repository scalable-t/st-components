package org.scalablet.components.bed.impl;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.core.BedConfiguration;
import org.scalablet.components.bed.core.BedDispatcher;
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
 * Task dispatcher, find tasks to execute
 *
 * @author abomb4 2021-10-14 19:19:23
 */
public class BedDispatcherLoopImpl implements BedDispatcher {

    /** main log */
    private static final Logger log = LoggerFactory.getLogger(BedDispatcherLoopImpl.class);
    /** dispatcher log */
    private static final Logger dispatcherLog = LoggerFactory.getLogger(DispatcherTask.class);

    /** config */
    private final BedConfiguration.BedDispatcherConfig dispatcherConfig;
    /** Repository */
    private final BedTaskRepository taskRepository;
    /** Runner */
    private final BedRunner runner;
    /** All executors instance */
    private final List<BedExecutor<?>> executorList;

    /** Pool of dispatcher thread */
    private final ScheduledThreadPoolExecutor dispatcherPool = new ScheduledThreadPoolExecutor(1,
            r -> new Thread(r, "bed-dispatcher"));

    /** inited */
    private boolean inited = false;

    /**
     * Construct a dispatcher, should call {@link #init()} later for ready to use
     *
     * @param dispatcherConfig config of dispatcher
     * @param taskRepository   repository
     * @param runner           Task runner
     * @param executorList     All executor instances in this dispatcher
     */
    BedDispatcherLoopImpl(BedConfiguration.BedDispatcherConfig dispatcherConfig,
                          BedTaskRepository taskRepository,
                          BedRunner runner,
                          List<BedExecutor<?>> executorList) {
        this.dispatcherConfig = dispatcherConfig;
        this.taskRepository = taskRepository;
        this.runner = runner;
        this.executorList = executorList;
    }

    /**
     * init
     */
    public void init() {
        if (!inited) {
            executorList.stream().map(BedExecutor::getThreadResourceName).distinct().forEach(resourceName -> {
                final int randomSeconds = dispatcherConfig.getLoopIntervalRandomSecondsByResource(resourceName);
                final int loopSecond = dispatcherConfig.getLoopIntervalSecondsByResource(resourceName);
                final long random = RandomGeneratorFactory.of("Random").create().nextLong(randomSeconds);
                final long waits = random + loopSecond;
                final DispatcherTask task = new DispatcherTask(resourceName, randomSeconds, loopSecond);
                dispatcherPool.schedule(task, waits, TimeUnit.SECONDS);
                log.info("BedDispatcher task will start after {}s.", waits);
            });
            inited = true;
        }
    }

    @Override
    public void taskCreated(BedTask bedTask) {
        // do nothing, nobody could break my loop
    }

    /**
     * Dispatcher.
     * <p>
     * This task continuously get Tasks (that made of type of BedExecutor and a cmd) and then dispatch them to
     * specific thread pool.
     */
    private class DispatcherTask implements Runnable {

        /** Random generator */
        final RandomGenerator random = RandomGeneratorFactory.of("Random").create();

        /** how many times this task run */
        int runCounter = 0;
        /** Resource name */
        final String resourceName;
        /** Random second */
        final int randomSeconds;
        /** Loop second */
        final int loopSecond;

        /**
         * Construct a task
         *
         * @param resourceName  resource name
         * @param randomSeconds random seconds
         * @param loopSeconds   loop seconds
         */
        private DispatcherTask(String resourceName, int randomSeconds, int loopSeconds) {
            this.resourceName = resourceName;
            this.randomSeconds = randomSeconds;
            this.loopSecond = loopSeconds;
        }

        @Override
        public void run() {
            // 1. find tasks
            // 2. create tasks in different pool
            runCounter += 1;
            try {
                final List<BedTask> someTasks = taskRepository.getSomeNeedRunTasks(100);
                for (BedTask task : someTasks) {
                    runner.submitImmediately(task);
                }
                dispatcherLog.info("({}) Dispatcher dispatched {} tasks", runCounter, someTasks.size());
            } catch (Exception e) {
                dispatcherLog.warn("({}) Unexpected exception in dispatcher task! Continues to run periodically.",
                        runCounter, e);
            } finally {
                final long seconds = random.nextLong(randomSeconds) + loopSecond;
                dispatcherPool.schedule(this, seconds, TimeUnit.SECONDS);
                dispatcherLog.debug("({}) next iteration after {}s.", runCounter, seconds);
            }
        }
    }
}
