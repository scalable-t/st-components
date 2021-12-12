package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * 执行器注册中心，注册一系列执行器的实例
 *
 * @author abomb4 2021-10-13 21:35:39
 */
public interface BedExecutorRegistry {

    /**
     * 获取执行器
     *
     * @param executorType 执行器类型
     * @param <C>          指令类型
     * @param <T>          执行器类型
     * @return 实例
     * @throws BedExecutorNotFoundException 未找到执行器
     */
    @Nonnull
    <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(Class<T> executorType);

    /**
     * 获取执行器
     *
     * @param executorType 执行器类型
     * @param <C>          指令类型
     * @param <T>          执行器类型
     * @return 实例
     * @throws BedExecutorNotFoundException 未找到执行器
     */
    <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(String executorType);

    /**
     * 获取某个类型执行器的正式名字
     *
     * @param executorType 执行器类型
     * @return 名字
     */
    String getExecutorName(Class<? extends BedExecutor<?>> executorType);

    /**
     * 直接取得该执行器中所有实例
     *
     * @return 所有实例
     */
    @SuppressWarnings("rawtypes")
    Collection<BedExecutor> getAllExecutors();

    /** Thrown if executor not found */
    class BedExecutorNotFoundException extends RuntimeException {

        /**
         * Construct with a one line stacktrace
         *
         * @param message exception message
         */
        public BedExecutorNotFoundException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
