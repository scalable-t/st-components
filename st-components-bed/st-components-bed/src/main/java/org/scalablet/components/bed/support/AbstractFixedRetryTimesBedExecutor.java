package org.scalablet.components.bed.support;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

/**
 * 固定重试次数与重试间隔
 *
 * @author abomb4 2021-11-11 09:55:19
 */
public abstract class AbstractFixedRetryTimesBedExecutor<T extends BedExecutorCmd> implements BedExecutor<T> {

    /** 最大重试次数，0 代表不重试 */
    protected final int maxRetryTimes;
    /** 重试间隔秒数 */
    protected final int retryDelaySeconds;

    /**
     * 根据最大重试次数与重试间隔秒数构建
     *
     * @param maxRetryTimes     最大重试次数，0 代表不重试，1 代表重试一次（共执行两次）
     * @param retryDelaySeconds 重试间隔秒数
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
