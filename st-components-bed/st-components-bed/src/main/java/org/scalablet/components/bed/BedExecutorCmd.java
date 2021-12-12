package org.scalablet.components.bed;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * 异步指令
 *
 * @author abomb4 2021-10-13 21:39:17
 */
public interface BedExecutorCmd extends Serializable {

    /**
     * 获取唯一 ID
     *
     * @return 异步指令唯一 ID ，需要使用方生成，并保证全局唯一
     */
    String getTaskId();

    /**
     * 是否立即执行
     *
     * @return 是否立即执行
     */
    @Nonnull
    default ImmediatelyEnum executeOnceImmediately() {
        return ImmediatelyEnum.NO;
    }

    /** 如何立即执行 */
    enum ImmediatelyEnum {
        /** 不立即执行 */
        NO,
        /** 在调用线程执行 */
        AT_CALLER_THREAD,
        /** 在一步框架线程执行 */
        AT_BED_THREAD,
    }
}
