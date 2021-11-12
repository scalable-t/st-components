package org.scalablet.components.bed.core;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Task repository
 *
 * @author abomb4 2021-10-26 15:40:28
 */
public interface BedTaskRepository {

    /**
     * Get some task
     *
     * @param limit limit
     * @return tasks
     */
    @NonNull
    List<BedTask> getSomeNeedRunTasks(int limit);

    /**
     * Get one record
     *
     * @param get get request
     * @return task nullable
     */
    @Nullable
    <T extends BedTaskGet> BedTask getTaskByPrimary(T get);

    /**
     * Save a new task into repository, its status will be {@link BedTaskStatusEnum#INIT}
     *
     * @param bedTask task info
     */
    void save(BedTask bedTask);

    /**
     * Mark a task as finished
     *
     * @param bedTask task info, its status will be {@link BedTaskStatusEnum#SUCCEED}
     */
    void finishTask(BedTask bedTask);

    /**
     * Update task execution info, its status will in {@link BedTaskStatusEnum#RETRYING},
     * {@link BedTaskStatusEnum#FAILED} or {@link BedTaskStatusEnum#UNRECOGNIZED}.
     *
     * @param bedTask task info with modification of {@link BedTask#getExecutedTimes()},
     *                {@link BedTask#getLastMessage()} and {@link BedTask#getStatus()}
     */
    void updateExecuted(BedTask bedTask);
}
