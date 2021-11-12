package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutorCmd;

/**
 * Serializer for cmd
 *
 * @author abomb4 2021-11-10 11:30:13
 */
public interface BedSerializer {

    /**
     * Serialize cmd to string
     *
     * @param cmd cmd object
     * @return string
     */
    String serialize(BedExecutorCmd cmd);

    /**
     * Deserialize string to specific cmd type
     *
     * @param s    json string
     * @param type type of cmd
     * @param <T>  type of cm
     * @return cmd object
     */
    <T extends BedExecutorCmd> T deserialize(String s, Class<T> type);

    /**
     * Serialization exception
     */
    class BedSerializeException extends BedException {

        /**
         * Construct with message
         *
         * @param message message
         */
        public BedSerializeException(String message) {
            super(message);
        }
    }
}
