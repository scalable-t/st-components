package org.scalablet.components.bed.core.impl;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.core.BedExecutorRegistry;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * Spring 实现
 *
 * @author abomb4 2021-10-14 11:14:58
 */
public class BedExecutorRegistrySpringImpl implements BedExecutorRegistry {

    /** spring */
    private final ApplicationContext applicationContext;

    /**
     * 根据 spring application context 构建
     *
     * @param applicationContext spring
     */
    public BedExecutorRegistrySpringImpl(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext cannot be null");
    }

    @Nonnull
    @Override
    public <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(Class<T> executorType) {
        if (executorType == null) {
            throw new IllegalArgumentException("executorType cannot be null");
        }
        try {
            return this.applicationContext.getBean(executorType);
        } catch (Exception ignore) {
            throw new BedExecutorNotFoundException("Cannot found Executor of type " +
                    executorType.getName() + "in spring");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(String executorType) {
        if (executorType == null) {
            throw new IllegalArgumentException("executorType cannot be null");
        }
        try {
            return (T) this.applicationContext.getBean(executorType);
        } catch (Exception ignore) {
            throw new BedExecutorNotFoundException("Cannot found Executor of name " + executorType + "in spring");
        }
    }

    @Override
    public String getExecutorName(Class<? extends BedExecutor<?>> executorType) {
        if (executorType == null) {
            throw new IllegalArgumentException("executorType cannot be null");
        }
        final String[] names = this.applicationContext.getBeanNamesForType(executorType);
        if (names.length == 0) {
            throw new BedExecutorNotFoundException("Cannot found Executor of name " + executorType + "in spring");
        }
        return names[0];
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<BedExecutor> getAllExecutors() {
        return this.applicationContext.getBeansOfType(BedExecutor.class).values();
    }
}
