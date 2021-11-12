package org.scalablet.components.bed.core;

/**
 * Base BED Exception
 *
 * @author abomb4 2021-11-11 14:04:45
 */
public class BedException extends RuntimeException {
    /**
     * Construct with message
     *
     * @param message message
     */
    public BedException(String message) {
        super(message);
    }

    /**
     * Construct with message and cause
     *
     * @param message message
     * @param cause   cause
     */
    public BedException(String message, Throwable cause) {
        super(message, cause);
    }
}
