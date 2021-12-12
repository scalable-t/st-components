package org.scalablet.components.bed.common;

import org.scalablet.components.bed.core.BedRunner;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名线程池，接收一个 lambda 作为参数，方便构建。
 * <p>
 * 正确用法：
 * <pre>
 * final ThreadFactory f = new NamedThreadFactory((fid, tid) -> "BED-" + fid + "-WORK-" + tid) {};
 * </pre>
 *
 * @author abomb4 2021-12-11 00:33:42
 */
public abstract class NamedThreadFactory implements ThreadFactory {
    /** 静态的工厂 id ，每创建一个工厂递增一个 */
    private static final AtomicInteger FACTORY_ID_GEN = new AtomicInteger(0);

    /** 工厂内线程数量 */
    private final AtomicInteger threadIdGen = new AtomicInteger(0);
    /** 当前工厂 id */
    private final int factoryId = FACTORY_ID_GEN.incrementAndGet();
    /** 线程名组装器 */
    private final Func func;

    /**
     * 根据 lambda 构建
     *
     * @param func 线程名函数
     */
    protected NamedThreadFactory(Func func) {
        this.func = Objects.requireNonNull(func);
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        return new Thread(r, this.makeName(this.factoryId, this.threadIdGen.incrementAndGet()));
    }

    /**
     * 拼装线程名
     *
     * @param factoryId 工厂 id
     * @param threadId  线程 id
     * @return 线程名
     */
    protected String makeName(int factoryId, int threadId) {
        return this.func.makeName(factoryId, threadId);
    }

    /** 线程名称拼装函数 */
    @FunctionalInterface
    public interface Func {

        /**
         * 拼装线程名
         *
         * @param factoryId 工厂 id
         * @param threadId  线程 id
         * @return 线程名
         */
        String makeName(int factoryId, int threadId);
    }
}
