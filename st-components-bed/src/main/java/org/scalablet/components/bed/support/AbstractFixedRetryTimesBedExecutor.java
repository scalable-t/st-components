package org.scalablet.components.bed.support;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

/**
 * BedExecutor with fixed retry times
 *
 * @author abomb4 2021-11-11 09:55:19
 */
public abstract class AbstractFixedRetryTimesBedExecutor<T extends BedExecutorCmd> implements BedExecutor<T> {

    /** Max retry times */
    protected final int maxRetryTimes;
    /** Retry delay seconds */
    protected final int retryDelaySeconds;

    /**
     * Construct with retry times and delay seconds
     *
     * @param maxRetryTimes     Max retry times, 0 means no retry, 1 means retry once (totally execute twice)
     * @param retryDelaySeconds Retry delay seconds
     */
    protected AbstractFixedRetryTimesBedExecutor(int maxRetryTimes, int retryDelaySeconds) {
        if (maxRetryTimes < 0) {
            throw new IllegalArgumentException("maxRetryTimes should greater or equal 0");
        }
        if (retryDelaySeconds < 0) {
            throw new IllegalArgumentException("retryDelaySeconds should greater or equal 0");
        }
        this.maxRetryTimes = maxRetryTimes;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Override
    public RetryControl shouldRetry(T cmd, int executedTimes) {
        return executedTimes >= maxRetryTimes ? RetryControl.noRetry() : RetryControl.retry(retryDelaySeconds);
    }
}
