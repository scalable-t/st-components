package org.scalablet.components.bed.core;

import org.scalablet.components.bed.BedExecutorCmd;

/**
 * cmd 序列化器
 *
 * @author abomb4 2021-11-10 11:30:13
 */
public interface BedSerializer {

    /**
     * 序列化成 string
     *
     * @param cmd cmd 对象
     * @return string
     */
    String serialize(BedExecutorCmd cmd);

    /**
     * 反序列化成指定类型的 cmd
     *
     * @param s    json string
     * @param type type of cmd
     * @param <T>  type of cmd
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
