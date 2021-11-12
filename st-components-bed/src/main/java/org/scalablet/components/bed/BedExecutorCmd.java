package org.scalablet.components.bed;

import java.io.Serializable;

/**
 * The executor command interface
 *
 * @author abomb4 2021-10-13 21:39:17
 */
public interface BedExecutorCmd extends Serializable {

    /**
     * Get unique task id
     *
     * @return unique async task id
     */
    String getTaskId();

    /**
     * Execute once immediately in BED thread
     *
     * @return immediately
     */
    default ImmediatelyEnum executeOnceImmediately() {
        return ImmediatelyEnum.NO;
    }

    /** How can BED execute immediately */
    enum ImmediatelyEnum {
        /** Don't */
        NO,
        /** In caller thread */
        AT_CALLER_THREAD,
        /** In BED thread */
        AT_BED_THREAD,
    }
}
