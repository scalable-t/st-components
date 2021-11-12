package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

/**
 * Singleton executor instance registry
 *
 * @author abomb4 2021-10-13 21:35:39
 */
public interface BedExecutorRegistry {

    /**
     * Get executor
     *
     * @param executorType type
     * @param <C>          class of cmd
     * @param <T>          class of executor
     * @return instance
     * @throws ExecutorNotFoundException if executor not found
     */
    <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(Class<T> executorType);

    /**
     * Get executor
     *
     * @param executorType type name
     * @param <C>          class of cmd
     * @param <T>          class of executor
     * @return instance
     * @throws ExecutorNotFoundException if executor not found
     */
    <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(String executorType);

    /** Thrown if executor not found */
    class ExecutorNotFoundException extends RuntimeException {

        /**
         * Construct with a one line stacktrace
         *
         * @param message    exception message
         * @param className  class name
         * @param methodName method name
         * @param fileName   file name
         * @param lineNumber line number
         */
        public ExecutorNotFoundException(String message, String className, String methodName, String fileName,
                                         int lineNumber) {
            super(message);
            this.setStackTrace(new StackTraceElement[]{
                    new StackTraceElement(className, methodName, fileName, lineNumber)
            });
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
