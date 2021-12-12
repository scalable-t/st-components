package org.scalablet.components.bed.autoconfig;

import org.scalablet.components.bed.core.BedTracer;
import org.scalablet.components.bed.core.impl.BedTracerSleuthImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * bed 组件跟踪号自动配置
 *
 * @author abomb4 2021-12-11 02:09:40 +0800
 */
@Configuration
@ConditionalOnClass({Tracer.class, BedTracerSleuthImpl.class})
public class BedTracerAutoConfiguration {

    /**
     * BedTracer 实现
     *
     * @return BedTracerSleuthImpl 实例
     */
    @Bean
    @ConditionalOnMissingBean(BedTracer.class)
    public BedTracerSleuthImpl bedTracerImpl(Tracer tracer) {
        return new BedTracerSleuthImpl(tracer);
    }
}
