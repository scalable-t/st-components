package org.scalablet.components.bed.core;

import lombok.extern.slf4j.Slf4j;
import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.BedFacadeService;
import org.scalablet.components.bed.BedResult;

import java.util.Objects;

/**
 * 门面服务实现，不变的是一定要落库
 *
 * @author abomb4 2021-10-13 21:37:07
 */
@Slf4j
public class BedServiceImpl implements BedFacadeService {

    /** 执行器注册中心 */
    private final BedExecutorRegistry bedExecutorRegistry;
    /** cmd 序列化器 */
    private final BedSerializer serializer;
    /** task 仓储 */
    private final BedTaskRepository repository;
    /** dispatcher */
    private final BedDispatcher dispatcher;
    /** runner */
    private final BedRunner runner;
    /** configuration */
    private final BedConfiguration configuration;
    /** Tracer */
    private final BedTracer tracer;

    /**
     * 构造
     *
     * @param bedExecutorRegistry 注册
     * @param serializer          序列化
     * @param repository          仓储
     * @param runner              任务执行器
     * @param configuration       配置
     * @param dispatcher          分发器
     * @param tracer              系统跟踪号支持
     */
    public BedServiceImpl(BedExecutorRegistry bedExecutorRegistry,
                          BedSerializer serializer,
                          BedTaskRepository repository,
                          BedRunner runner,
                          BedConfiguration configuration,
                          BedDispatcher dispatcher,
                          BedTracer tracer) {
        this.bedExecutorRegistry = Objects.requireNonNull(bedExecutorRegistry, "bedExecutorRegistry cannot be null");
        this.serializer = Objects.requireNonNull(serializer, "serializer cannot be null");
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
        this.runner = Objects.requireNonNull(runner, "runner cannot be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration cannot be null");
        this.tracer = Objects.requireNonNull(tracer, "tracer cannot be null");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher cannot be null");
    }

    @Override
    public <T extends BedExecutorCmd> void submit(Class<? extends BedExecutor<T>> executorType, T cmd) {
        // 1. validate parameters
        // 2. save to repository
        Objects.requireNonNull(executorType, "executorType cannot be null");
        Objects.requireNonNull(cmd, "cmd cannot be null");
        Check.notEmpty(cmd.getTaskId(), "cmd.getTaskId() cannot be empty (" + cmd + ")");

        final String executorTypeName = executorType.getName();

        // get executor
        final BedExecutor<T> executor = this.bedExecutorRegistry.getExecutor(executorType);

        // create task and save
        final BedExecutor.RetryControl retryControl;
        retryControl = this.getRetryControl(cmd, executorTypeName, executor);
        if (!retryControl.isShouldRetry()) {
            throw new BedException(String.format("task id [%s] with type [%s] shouldRetry(cmd, 0) " +
                    "returns [should not retry], illegal.", cmd.getTaskId(), executorTypeName));
        }

        final int delaySeconds = retryControl.getDelaySeconds();
        final BedTask bedTask = BedTask.createNewInstance(
                cmd.getTaskId(),
                this.configuration.getServerRoomId(),
                executorTypeName,
                delaySeconds,
                this.tracer.get(),
                this.serializer.serialize(cmd));

        log.info("Prepare to save task id [{}] with type [{}]", cmd.getTaskId(), executorTypeName);
        this.repository.save(bedTask);
        // Tell dispatcher a task has been created
        this.dispatcher.taskCreated(bedTask);

        // do once in caller thread if necessary
        switch (cmd.executeOnceImmediately()) {
            case AT_BED_THREAD -> {
                log.info("Submit task id [{}] with type [{}] to bed thread", cmd.getTaskId(), executorTypeName);
                this.runner.submitImmediately(bedTask);
            }
            case AT_CALLER_THREAD -> {
                log.info("Prepare call task id [{}] with type [{}] at caller thread immediately",
                        cmd.getTaskId(), executorTypeName);
                final BedResult bedResult = this.runner.runImmediately(bedTask);
                log.info("Call task id [{}] with type [{}] result [{}]", cmd.getTaskId(), executorTypeName, bedResult);
            }
            default -> log.info("Task id [{}] with type [{}] saved, may execute after [{}s]",
                    cmd.getTaskId(), executorTypeName, delaySeconds);
        }
    }

    /**
     * get RetryControl from executor
     *
     * @param cmd              cmd
     * @param executorTypeName executor type name
     * @param executor         executor instance
     * @param <T>              type of cmd
     * @return executor
     * @throws BedException wrap exception
     */
    private <T extends BedExecutorCmd> BedExecutor.RetryControl getRetryControl(T cmd, String executorTypeName,
                                                                                BedExecutor<T> executor) {
        final BedExecutor.RetryControl retryControl;
        try {
            retryControl = Objects.requireNonNull(executor.shouldRetry(cmd, 0));
        } catch (Exception e) {
            log.error("task id [{}] with type [{}] shouldRetry(cmd, 0) cause exception, ignore this task and throw {}",
                    cmd.getTaskId(), executorTypeName, e.toString());
            throw new BedException("shouldRetry(cmd, 0) failed", e);
        }
        return retryControl;
    }

    /** Parameter checking util */
    private static class Check {

        /**
         * throw IllegalArgumentException if s is empty
         *
         * @param s       string
         * @param message exception message
         */
        static void notEmpty(String s, String message) {
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException(message);
            }
        }
    }
}
