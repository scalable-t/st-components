package org.scalablet.components.bed.core;

import org.springframework.lang.Nullable;

/**
 * Tracer support, implementations should behave like ThreadLocal
 *
 * @author abomb4 2021-11-10 16:28:54
 */
public interface BedTracer {

    /**
     * Get trace id of this thread
     *
     * @return trace id
     */
    @Nullable
    String get();

    /**
     * Set trace id to current thread. MUST call {@link #clear()} later.
     *
     * @param traceId trace id
     */
    void set(String traceId);

    /**
     * Clear thread local variables, MUST call after {@link #set(String)}.
     */
    void clear();
}
