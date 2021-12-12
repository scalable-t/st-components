package org.scalablet.components.bed.core;

import lombok.Getter;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务状态
 *
 * @author abomb4 2021-11-10 15:36:08
 */
@Getter
public enum BedTaskStatusEnum {

    /** 初始状态，从未执行 */
    INIT("I", "Initial"),
    /** 失败过一次，正准备重试 */
    RETRYING("R", "Retrying"),
    /** 正在执行 */
    EXECUTING("E", "Executing"),
    /** 已失败，达到最大重试次数 */
    FAILED("F", "Failed"),
    /** 未知，说明任务无法在 BedExecutorRegistry 中找到 */
    UNRECOGNIZED("U", "Unrecognized"),
    /** 成功 */
    SUCCEED("S", "Succeed"),
    ;

    /** code */
    private final String code;
    /** description */
    private final String description;

    /** code to enum map */
    private static final Map<String, BedTaskStatusEnum> CODE_ENUM_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(v -> v.code, v -> v));

    /**
     * Get enum by code
     *
     * @param code code
     * @return enum, nullable
     */
    @Nullable
    public static BedTaskStatusEnum getByCode(String code) {
        return CODE_ENUM_MAP.get(code);
    }

    /**
     * private enum constructor
     *
     * @param code        code
     * @param description default description
     */
    BedTaskStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
