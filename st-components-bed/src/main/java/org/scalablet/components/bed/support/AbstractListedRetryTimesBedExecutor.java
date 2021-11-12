package org.scalablet.components.bed.support;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

import java.util.Collections;
import java.util.List;

/**
 * BedExecutor with a list of retry times
 *
 * @author abomb4 2021-11-11 09:55:19
 */
public abstract class AbstractListedRetryTimesBedExecutor<T extends BedExecutorCmd> implements BedExecutor<T> {

    /** List of retry time in seconds */
    private List<Integer> retryTimeList;


    /**
     * Construct with list of retry time
     *
     * @param retryTimeList Retry time (in seconds) list, the first element will be
     */
    protected AbstractListedRetryTimesBedExecutor(List<Integer> retryTimeList) {
        this.setRetryTimeList(retryTimeList);
    }

    /**
     * Set retry time list
     *
     * @param retryTimeList cannot be null or empty, cannot contains value lesser than 0
     */
    protected void setRetryTimeList(List<Integer> retryTimeList) {
        if (retryTimeList == null || retryTimeList.isEmpty()) {
            throw new IllegalArgumentException("retryTimeList cannot be null or empty");
        }
        int i = 0;
        for (Integer integer : retryTimeList) {
            if (integer < 0) {
                throw new IllegalArgumentException(
                        "retryTimesLimit[" + i + "] = " + integer + " lesser than 0 is " + "illegal");
            }
            i += 1;
        }
        this.retryTimeList = Collections.unmodifiableList(retryTimeList);
    }

    /**
     * Get retry time list
     *
     * @return retry time list
     */
    protected List<Integer> getRetryTimeList() {
        return retryTimeList;
    }

    @Override
    public RetryControl shouldRetry(T cmd, int executedTimes) {
        final List<Integer> list = this.retryTimeList;
        return executedTimes >= list.size()
                ? RetryControl.noRetry()
                : RetryControl.retry(list.get(executedTimes));
    }
}
