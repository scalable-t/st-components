package org.scalablet.components.bed.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Task 仓储
 *
 * @author abomb4 2021-10-26 15:40:28
 */
public interface BedTaskRepository {

    /**
     * 获取任务，同时锁住
     *
     * @param limit limit
     * @param resourceName
     * @return tasks
     */
    @Nonnull
    List<BedTask> getSomeNeedRunTasks(int limit, String resourceName);

    /**
     * 获取单条记录
     *
     * @param get get request
     * @return task nullable
     */
    @Nullable
    <T extends BedTaskGet> BedTask getTaskByPrimary(T get);

    /**
     * 存储一个任务，初始状态应当为 {@link BedTaskStatusEnum#INIT}
     *
     * @param bedTask 任务信息
     */
    void save(BedTask bedTask);

    /**
     * Update task execution info, its status will in {@link BedTaskStatusEnum#RETRYING},
     * {@link BedTaskStatusEnum#FAILED} or {@link BedTaskStatusEnum#UNRECOGNIZED}.
     *
     * @param bedTask task info with modification of {@link BedTask#getExecutedTimes()},
     *                {@link BedTask#getLastMessage()} and {@link BedTask#getStatus()}
     */
    void updateExecuted(BedTask bedTask);
}
