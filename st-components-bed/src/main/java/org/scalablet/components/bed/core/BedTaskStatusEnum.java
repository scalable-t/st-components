package org.scalablet.components.bed.core;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Task status
 *
 * @author abomb4 2021-11-10 15:36:08
 */
@Getter
public enum BedTaskStatusEnum {

    /** Initial, never executed */
    INIT("I", "Initial"),
    /** Executed but not success and max retry times not exceeded */
    RETRYING("R", "Retrying"),
    /** Failed and max retry times exceeded */
    FAILED("F", "Failed"),
    /** Unrecognized, means the executorType cannot found in BedExecutorRegistry */
    UNRECOGNIZED("U", "Unrecognized"),
    /** Succeed */
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
