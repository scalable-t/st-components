package org.scalablet.components.bed.core;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Configurations for constructing bed facade service
 *
 * @author abomb4 2021-11-10 15:17:29
 */
@Data
public class BedConfiguration {

    /** Server room id */
    private String serverRoomId;
    /** Dispatcher config */
    private BedDispatcherConfig dispatcherConfig;
    /** Runner config */
    private BedRunnerConfig runnerConfig;

    /** Runner config */
    @Data
    public static class BedRunnerConfig {
        /** Pool config */
        private List<BedPoolConfig> bedPoolConfigList;
        /** Default pool config */
        private BedPoolConfig defaultConfig;

        /**
         * Get pool config or default {@link #defaultConfig}
         *
         * @param name resource name
         * @return config
         */
        public BedPoolConfig getPoolConfig(String name) {
            return bedPoolConfigList.stream()
                    .filter(v -> name.equals(v.getName()))
                    .findFirst()
                    .orElse(this.defaultConfig);
        }
    }

    /** Dispatcher config */
    @Data
    public static class BedDispatcherConfig {
        private int loopIntervalSeconds;
        private int loopIntervalRandomSeconds;
        private Map<String, DispatcherResourceConfig> resources;

        public int getLoopIntervalRandomSecondsByResource(String resourceName) {
            final DispatcherResourceConfig conf =
                    Optional.ofNullable(this.resources).orElseGet(Collections::emptyMap).get(resourceName);
            return conf == null ? loopIntervalRandomSeconds : conf.loopIntervalRandomSeconds;
        }

        public int getLoopIntervalSecondsByResource(String resourceName) {
            final DispatcherResourceConfig conf =
                    Optional.ofNullable(this.resources).orElseGet(Collections::emptyMap).get(resourceName);
            return conf == null ? loopIntervalSeconds : conf.loopIntervalSeconds;
        }

        @Data
        public static class DispatcherResourceConfig {
            private int loopIntervalSeconds;
            private int loopIntervalRandomSeconds;
        }
    }

    /**
     * Configuration of a pool
     *
     * @author abomb4 2021-10-14 22:08:06
     */
    @Data
    public static class BedPoolConfig {

        /** name of pool */
        private String name;
        /** core size */
        private int coreSize = 20;
        /** Queue size */
        private int queueSize = 8192;
    }
}
