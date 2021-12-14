package org.scalablet.components.bed.core.impl;

import org.scalablet.components.bed.core.BedTracer;
import org.springframework.cloud.sleuth.ScopedSpan;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import javax.annotation.Nullable;

/**
 * 不支持 tracer 的默认实现
 *
 * @author abomb4 2021-12-14 23:55:36
 */
public class BedTracerDummyImpl implements BedTracer {

    @Nullable
    @Override
    public String get() {
        return null;
    }

    @Override
    public void set(String traceId) {
    }

    @Override
    public void clear() {

    }
}
