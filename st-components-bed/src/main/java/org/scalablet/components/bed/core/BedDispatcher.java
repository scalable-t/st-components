package org.scalablet.components.bed.core;

/**
 * Dispatcher interface. A dispatcher should continuously bring {@link BedTask} into {@link BedRunner} at expected time.
 *
 * @author abomb4 2021-11-11 15:53:18
 */
public interface BedDispatcher {

    /**
     * Called when a task created
     *
     * @param bedTask task info
     */
    void taskCreated(BedTask bedTask);
}
