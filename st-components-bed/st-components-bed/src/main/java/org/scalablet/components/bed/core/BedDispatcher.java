package org.scalablet.components.bed.core;

/**
 * 分发器，按指定时间不断从仓储中获取 {@link BedTask} 放到 {@link BedRunner}。
 *
 * @author abomb4 2021-11-11 15:53:18
 */
public interface BedDispatcher {

    /**
     * 任务创建时被调用
     *
     * @param bedTask 任务
     */
    void taskCreated(BedTask bedTask);
}
