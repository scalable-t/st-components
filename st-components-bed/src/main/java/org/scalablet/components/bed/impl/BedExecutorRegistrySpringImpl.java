package org.scalablet.components.bed.impl;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.core.BedExecutorRegistry;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * Spring impl
 *
 * @author abomb4 2021-10-14 11:14:58
 */
public class BedExecutorRegistrySpringImpl implements BedExecutorRegistry {

    /** spring */
    private final ApplicationContext applicationContext;

    /**
     * Construct with spring application context
     *
     * @param applicationContext spring
     */
    public BedExecutorRegistrySpringImpl(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext cannot be null");
    }

    @Override
    public <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(Class<T> executorType) {
        if (executorType == null) {
            throw new NullPointerException("executorType cannot be null");
        }
        try {
            return applicationContext.getBean(executorType);
        } catch (Exception e) {
            throw new ExecutorNotFoundException("Cannot found Executor of type " + executorType.getName() + "in spring",
                    "BedExecutorRegistrySpringImpl.class", "getExecutor", "BedExecutorRegistrySpringImpl.java", 35);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends BedExecutorCmd, T extends BedExecutor<C>> T getExecutor(String executorType) {
        if (executorType == null) {
            throw new NullPointerException("executorType cannot be null");
        }
        try {
            return (T) applicationContext.getBean(executorType);
        } catch (Exception e) {
            throw new ExecutorNotFoundException("Cannot found Executor of name " + executorType + "in spring",
                    "BedExecutorRegistrySpringImpl.class", "getExecutor", "BedExecutorRegistrySpringImpl.java", 46);
        }
    }
}
