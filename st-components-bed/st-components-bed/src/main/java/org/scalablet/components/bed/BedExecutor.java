package org.scalablet.components.bed;

import lombok.Value;

/**
 * 业务代码执行器接口，使用方实现此接口以提供自己的异步功能。
 * <p>
 * 在 {@code org.scalablet.components.bed.support} 包下有一些方便使用的方法可以继承使用。
 *
 * @author abomb4 2021-10-13 21:35:26
 * @see org.scalablet.components.bed.support.AbstractFixedRetryTimesBedExecutor
 * @see org.scalablet.components.bed.support.AbstractListedRetryTimesBedExecutor
 */
public interface BedExecutor<T extends BedExecutorCmd> {

    /** 默认 */
    String DEFAULT_THREAD_RESOURCE_NAME = "default";

    /**
     * 业务代码，会被 {@link org.scalablet.components.bed.core.BedRunner} 调用
     *
     * @param cmd 任务参数
     * @return 执行结果
     */
    BedResult execute(T cmd);

    /**
     * 获取 cmd 类型
     *
     * @return 类型
     */
    Class<T> getCmdClass();

    /**
     * 获取线程资源名称，有些重要的任务可以设置不同的名字，以使用独立的线程池资源。
     *
     * @return 资源名称
     */
    default String getThreadResourceName() {
        return DEFAULT_THREAD_RESOURCE_NAME;
    }

    /**
     * 达到最大执行次数之后的回调。
     * <p>
     * 默认啥也不干
     *
     * @param cmd 任务参数
     */
    default void maxRetryExceeded(T cmd) {
    }

    /**
     * 根据异步指令与已经执行的次数决定是否需要重试.
     * <p>
     * 当 {@link BedFacadeService#submit(Class, BedExecutorCmd)} 方法调用时，会使用 executedTimes = 0 来调用此方法，
     * 来决定第一次执行需要多久
     *
     * @param cmd           任务参数
     * @param executedTimes 已执行次数，从未执行时为 0 ，执行一次之后为 1
     * @return 控制信息
     */
    RetryControl shouldRetry(T cmd, int executedTimes);

    /** 重试控制信息，决定如何重试 */
    @Value
    class RetryControl {

        /** 无需重试的常量，防止不必要的 new */
        private static final RetryControl NO_RETRY = new RetryControl(false, 0);

        /**
         * 创建一个不重试控制
         *
         * @return 不重试
         */
        public static RetryControl noRetry() {
            return NO_RETRY;
        }

        /**
         * 创建一个重试控制
         *
         * @param retryDelaySeconds 重试延迟秒数
         * @return 重试控制
         */
        public static RetryControl retry(int retryDelaySeconds) {
            return new RetryControl(true, retryDelaySeconds);
        }

        /** 是否需要重试 */
        boolean shouldRetry;
        /** 重试间隔秒数 */
        int delaySeconds;

        /**
         * 私有构造
         *
         * @param shouldRetry  是否需要重试
         * @param delaySeconds 重试间隔秒数
         */
        private RetryControl(boolean shouldRetry, int delaySeconds) {
            this.shouldRetry = shouldRetry;
            if (shouldRetry && delaySeconds < 0) {
                throw new IllegalArgumentException("delaySeconds cannot lesser than 0 if shouldRetry is true");
            }
            this.delaySeconds = delaySeconds;
        }

        @Override
        public String toString() {
            if (this.shouldRetry) {
                return "RetryControl(Shouldn't retry)";
            } else {
                // noinspection StringConcatenationMissingWhitespace
                return "RetryControl(Retry at " + this.delaySeconds + "s)";
            }
        }
    }
}
