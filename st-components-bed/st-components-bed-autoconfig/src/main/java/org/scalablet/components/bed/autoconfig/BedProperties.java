package org.scalablet.components.bed.autoconfig;

import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.scalablet.components.bed.common.BedConstants;
import org.scalablet.components.bed.core.BedConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 参数配置
 *
 * @author abomb4 2021-12-11 14:26:45 +0800
 */
@Data
@ConfigurationProperties("st.bed")
public class BedProperties {

    /** mapstruct */
    private static final BedPropertiesAssemblerMapper MAPPER = Mappers.getMapper(BedPropertiesAssemblerMapper.class);

    /** 机房号 */
    private String serverRoomId;
    /** 分发器配置 */
    private BedDispatcherConfig dispatcher = new BedDispatcherConfig();
    /** 执行器配置 */
    private BedRunnerConfig runner = new BedRunnerConfig();
    /** 其他配置 */
    private Map<String, Properties> extension = new HashMap<>(8);

    /** 执行器配置 */
    @Data
    public static class BedRunnerConfig {
        /** 线程池配置 */
        private List<BedPoolConfig> bedPoolConfigList = new ArrayList<>(2);
        /** 默认线程池配置 */
        private BedPoolConfig defaultConfig = new BedPoolConfig();
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
        private DispatcherResourceConfig defaultConfig = new DispatcherResourceConfig();
        /** 不同资源轮询配置 */
        private Map<String, DispatcherResourceConfig> resources = new HashMap<>(8);
    }

    /** 分发器配置 */
    @Data
    public static class DispatcherResourceConfig {
        /** 轮询间隔秒数 */
        private int loopIntervalSeconds = BedConstants.DEFAULT_LOOP_INTERVAL_SECONDS;
        /** 轮询间隔随机增加秒数 */
        private int loopIntervalRandomSeconds = BedConstants.DEFAULT_LOOP_INTERVAL_RANDOM_SECONDS;
    }

    /**
     * 转换成核心层配置
     *
     * @return 核心层配置
     */
    public BedConfiguration toBedConfiguration() {
        return MAPPER.fromPropertiesToConfig(this);
    }

    /** MapStruct mapper */
    @Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    interface BedPropertiesAssemblerMapper {

        /**
         * from BedProperties to BedConfiguration
         *
         * @param config BedProperties
         * @return BedConfiguration
         */
        @Mapping(target = "runnerConfig", source = "runner")
        @Mapping(target = "dispatcherConfig", source = "dispatcher")
        BedConfiguration fromPropertiesToConfig(BedProperties config);
    }
}
