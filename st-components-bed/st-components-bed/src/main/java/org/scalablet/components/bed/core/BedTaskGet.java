package org.scalablet.components.bed.core;

import lombok.Data;

/**
 * 查询请求
 *
 * @author abomb4 2021-11-11 15:48:35
 */
@Data
public class BedTaskGet {

    /** 唯一任务 id */
    private String taskId;
    /** 机房号 */
    private String serverRoomId;
}
