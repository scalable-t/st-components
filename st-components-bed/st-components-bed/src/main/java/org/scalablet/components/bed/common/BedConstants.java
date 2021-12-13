package org.scalablet.components.bed.common;

/**
 * 常量
 *
 * @author abomb4 2021-12-11 14:28:15 +0800
 */
public final class BedConstants {

    /** 默认核心线程数 */
    public static final int DEFAULT_CORE_SIZE = 20;
    /** 默认队列长度 */
    public static final int DEFAULT_QUEUE_SIZE = 8192;

    /** 默认基础轮询间隔 */
    public static final int DEFAULT_LOOP_INTERVAL_SECONDS = 30;
    /** 默认随机轮须间隔范围 */
    public static final int DEFAULT_LOOP_INTERVAL_RANDOM_SECONDS = 10;
    /** 默认锁过期秒数，一般是一个任务执行的最长时间 */
    public static final long DEFAULT_LOCK_EXPIRATION_SECONDS = 30 * 60;

    /** no construct */
    private BedConstants() {}
}
