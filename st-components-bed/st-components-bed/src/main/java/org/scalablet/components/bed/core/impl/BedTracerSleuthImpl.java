package org.scalablet.components.bed.core.impl;

import org.scalablet.components.bed.core.BedTracer;
import org.springframework.cloud.sleuth.ScopedSpan;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import javax.annotation.Nullable;

/**
 * 支持 Spring Sleuth 3.0.0+ 的
 *
 * @author abomb4 2021-12-11 15:17:41 +0800
 */
public class BedTracerSleuthImpl implements BedTracer {

    /** Sleuth Tracer */
    private final Tracer tracer;
    /** Thread local */
    private final ThreadLocal<ScopedSpan> spanLocal = new ThreadLocal<>();

    public BedTracerSleuthImpl(Tracer tracer) {
        this.tracer = tracer;
    }

    @Nullable
    @Override
    public String get() {
        final Span span = this.tracer.currentSpan();
        if (span == null) {
            return null;
        }
        return span.context().traceId();
    }

    @Override
    public void set(String traceId) {
        final ScopedSpan span = this.tracer.startScopedSpan(traceId);
        this.spanLocal.set(span);
    }

    @Override
    public void clear() {
        this.spanLocal.remove();
    }
}
