package org.scalablet.components.bed;

/**
 * Best-effort 最大努力通知门面服务
 *
 * @author abomb4 2021-10-13 21:33:49
 */
public interface BedFacadeService {

    /**
     * 不断使用传入的 {@code cmd} 调用 {@link BedExecutor#execute(BedExecutorCmd)}
     * 直到到达成功状态 {@link EnumBedResultType#COMPLETED}。
     *
     * @param executorType 异步执行器类型
     * @param cmd          {@code execute} 使用的指令
     * @param <T>          cmd 类型
     */
    <T extends BedExecutorCmd> void submit(Class<? extends BedExecutor<T>> executorType, T cmd);
}
