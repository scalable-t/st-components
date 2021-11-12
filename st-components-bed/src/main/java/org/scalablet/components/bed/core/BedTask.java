package org.scalablet.components.bed.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.scalablet.components.bed.BedExecutor;

/**
 * Task object
 *
 * @author abomb4 2021-10-26 19:55:09
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class BedTask {

    /**
     * Create new task instance
     *
     * @param taskId       Unique task id
     * @param serverRoomId Server room id
     * @param executorType Type of executor
     * @param delaySeconds Delay seconds
     * @param cmd          Cmd
     * @param traceId      Trace id
     * @return task instance
     */
    static BedTask createNewInstance(String taskId, String serverRoomId, String executorType,
                                     int delaySeconds, String traceId, String cmd) {
        return new BedTask(taskId, serverRoomId, executorType, 0, BedTaskStatusEnum.INIT, delaySeconds,
                traceId, "", cmd);
    }

    /** Unique task id */
    private String taskId;
    /** Server room id, an application can only create and execute task with their own room id */
    private String serverRoomId;
    /** Executor type */
    private String executorType;
    /** Executed times */
    private int executedTimes;
    /** Task status */
    private BedTaskStatusEnum status;
    /** Delay seconds */
    private int nextDelaySeconds;
    /** Trace id for better logging */
    private String traceId;
    /** Last message */
    private String lastMessage;
    /** Serialized cmd */
    private String cmd;

    /**
     * Call if this task success executed
     */
    void successExecuted() {
        this.executedTimes += 1;
        this.status = BedTaskStatusEnum.SUCCEED;
    }

    /**
     * Call if this task failed to execute
     *
     * @param retryControl Retry control info
     * @param message      fail message up to 100 chars
     */
    void failExecuted(BedExecutor.RetryControl retryControl, String message) {
        this.executedTimes += 1;
        this.lastMessage = message.substring(0, Math.min(100, message.length()));
        this.nextDelaySeconds = retryControl.getDelaySeconds();
        if (retryControl.isShouldRetry()) {
            this.status = BedTaskStatusEnum.FAILED;
        } else {
            this.status = BedTaskStatusEnum.RETRYING;
        }
    }

    /**
     * Tag this task as {@link BedTaskStatusEnum#UNRECOGNIZED}
     *
     * @param errorMsg errorMsg
     */
    void tagAsUnrecognized(String errorMsg) {
        this.status = BedTaskStatusEnum.UNRECOGNIZED;
        this.lastMessage = errorMsg;
    }
}
