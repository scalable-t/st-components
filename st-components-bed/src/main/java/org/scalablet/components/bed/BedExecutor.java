package org.scalablet.components.bed;

import lombok.Value;

/**
 * Executor interface, users should implement this to do their business.
 *
 * @author abomb4 2021-10-13 21:35:26
 * @see org.scalablet.components.bed.support.AbstractFixedRetryTimesBedExecutor
 */
public interface BedExecutor<T extends BedExecutorCmd> {

    /**
     * Execute your code, invoked by Bed dispatcher
     *
     * @param cmd cmd
     * @return Execution result
     */
    BedResult execute(T cmd);

    /**
     * 获取 cmd 类型
     *
     * @return 类型
     */
    Class<T> getCmdClass();

    /**
     * Get the resource name, task in different resource may execute by different thread pool
     *
     * @return resource name
     */
    default String getThreadResourceName() {
        return "default";
    }

    /**
     * Called if max retry times exceeded.
     * <p>
     * Do nothing by default.
     *
     * @param cmd cmd
     */
    default void maxRetryExceeded(T cmd) {
    }

    /**
     * Decide how a task should retry.
     * <p>
     * Will called by {@link BedFacadeService#submit(Class, BedExecutorCmd)} with executedTimes = 0 to decide first
     * execution delay seconds.
     *
     * @param cmd           Cmd object
     * @param executedTimes Already executed times
     * @return Control info
     */
    RetryControl shouldRetry(T cmd, int executedTimes);

    /** Decide how a task should retry */
    @Value
    class RetryControl {

        /** No retry control instance */
        private static final RetryControl NO_RETRY = new RetryControl(false, 0);

        /**
         * Create a no retry control
         *
         * @return Control info
         */
        public static RetryControl noRetry() {
            return NO_RETRY;
        }

        /**
         * Create a retry control
         *
         * @param retryDelaySeconds Retry delay seconds
         * @return Control info
         */
        public static RetryControl retry(int retryDelaySeconds) {
            return new RetryControl(true, retryDelaySeconds);
        }

        /** Should retry? */
        boolean shouldRetry;
        /** delay seconds */
        int delaySeconds;

        /**
         * Private constructor
         *
         * @param shouldRetry  should retry
         * @param delaySeconds delay seconds
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
                return "RetryControl(Retry at " + this.delaySeconds + "s)";
            }
        }
    }
}
