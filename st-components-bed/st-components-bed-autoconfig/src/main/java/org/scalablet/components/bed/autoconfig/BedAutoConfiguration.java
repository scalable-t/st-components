package org.scalablet.components.bed.autoconfig;

import org.scalablet.components.bed.BedFacadeService;
import org.scalablet.components.bed.core.BedConfiguration;
import org.scalablet.components.bed.core.BedDispatcher;
import org.scalablet.components.bed.core.BedExecutorRegistry;
import org.scalablet.components.bed.core.BedRunner;
import org.scalablet.components.bed.core.BedSerializer;
import org.scalablet.components.bed.core.BedServiceImpl;
import org.scalablet.components.bed.core.BedTaskRepository;
import org.scalablet.components.bed.core.BedTracer;
import org.scalablet.components.bed.core.impl.BedExecutorRegistrySpringImpl;
import org.scalablet.components.bed.core.impl.BedSerializerJacksonImpl;
import org.scalablet.components.bed.core.impl.BedTaskRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * bed 组件自动配置
 *
 * @author abomb4 2021-12-11 02:09:40 +0800
 */
@Configuration
@ConditionalOnClass(BedServiceImpl.class)
@EnableConfigurationProperties(BedProperties.class)
public class BedAutoConfiguration {

    /**
     * 门面服务实现
     *
     * @return 实例
     */
    @Bean
    @ConditionalOnMissingBean(BedFacadeService.class)
    public BedServiceImpl bedServiceImpl(BedExecutorRegistry bedExecutorRegistry,
                                         BedSerializer serializer,
                                         BedTaskRepository repository,
                                         BedRunner runner,
                                         BedConfiguration configuration,
                                         BedDispatcher dispatcher,
                                         BedTracer tracer) {
        return new BedServiceImpl(bedExecutorRegistry, serializer, repository, runner, configuration, dispatcher,
                tracer);
    }

    /**
     * BedExecutorRegistry 实现
     *
     * @param applicationContext spring
     * @return BedExecutorRegistryImpl 实例
     */
    @Bean
    @ConditionalOnMissingBean(BedExecutorRegistry.class)
    public BedExecutorRegistrySpringImpl bedExecutorRegistrySpringImpl(ApplicationContext applicationContext) {
        return new BedExecutorRegistrySpringImpl(applicationContext);
    }

    /**
     * BedSerializer 实现
     *
     * @return BedSerializerImpl 实例
     */
    @Bean
    @ConditionalOnMissingBean(BedSerializer.class)
    public BedSerializerJacksonImpl bedSerializerJacksonImpl() {
        return new BedSerializerJacksonImpl();
    }

    /**
     * BedTaskRepository 实现
     *
     * @param jdbcTemplate  jdbcTemplate
     * @param configuration 配置
     * @return BedTaskRepositoryImpl 实例
     */
    @Bean
    @ConditionalOnMissingBean(BedTaskRepository.class)
    public BedTaskRepositoryImpl bedTaskRepositoryImpl(JdbcTemplate jdbcTemplate,
                                                       BedConfiguration configuration) {
        return new BedTaskRepositoryImpl(jdbcTemplate, configuration);
    }

    /**
     * BedRunner 实现
     *
     * @param runnerConfig     执行器配置
     * @param taskRepository   仓储
     * @param executorRegistry 异步执行器注册中心，存储异步执行器的实例
     * @param serializer       cmd 序列化功能
     * @return BedRunnerImpl 实例
     */
    @Bean
    public BedRunner bedRunner(BedConfiguration.BedRunnerConfig runnerConfig, BedTaskRepository taskRepository,
                               BedExecutorRegistry executorRegistry, BedSerializer serializer) {
        return new BedRunner(runnerConfig, taskRepository, executorRegistry, serializer);
    }

    /**
     * 配置
     *
     * @param properties properties
     * @return 配置
     */
    @Bean
    public BedConfiguration bedConfiguration(BedProperties properties) {
        return properties.toBedConfiguration();
    }

}
