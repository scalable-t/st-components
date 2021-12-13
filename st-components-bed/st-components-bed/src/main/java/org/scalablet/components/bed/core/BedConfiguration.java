package org.scalablet.components.bed.core;

import lombok.Data;
import org.scalablet.components.bed.common.BedConstants;

import java.util.List;
import java.util.Map;

/**
 * 构造最大努力通知框架所需的配置项
 *
 * @author abomb4 2021-11-10 15:17:29
 */
@Data
public class BedConfiguration {

    /** 全局唯一确定的应用编号，最好每次都随机生成 */
    private String appId;
    /** 机房号 */
    private String serverRoomId;
    /** 分发器配置 */
    private BedDispatcherConfig dispatcherConfig;
    /** 执行器配置 */
    private BedRunnerConfig runnerConfig;

    /** 执行器配置 */
    @Data
    public static class BedRunnerConfig {
        /** 线程池配置 */
        private List<BedPoolConfig> bedPoolConfigList;
        /** 默认线程池配置 */
        private BedPoolConfig defaultConfig;
    }

    /** 执行器线程池配置 */
    @Data
    public static class BedPoolConfig {
        /** name of pool */
        private String name;
        /** core size */
        private int coreSize = BedConstants.DEFAULT_CORE_SIZE;
        /** Queue size */
        private int queueSize = BedConstants.DEFAULT_QUEUE_SIZE;
    }

    /** 分发器配置 */
    @Data
    public static class BedDispatcherConfig {
        /** 默认轮询配置 */
        private DispatcherResourceConfig defaultConfig;
        /** 不同资源轮询配置 */
        private Map<String, DispatcherResourceConfig> resources;
        /** 锁过期秒数 */
        private long lockExpirationSeconds;
    }

    /** 分发器配置 */
    @Data
    public static class DispatcherResourceConfig {
        /** 轮询间隔秒数 */
        private int loopIntervalSeconds;
        /** 轮询间隔随机增加秒数 */
        private int loopIntervalRandomSeconds;
    }
}
