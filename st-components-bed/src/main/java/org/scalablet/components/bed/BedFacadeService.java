package org.scalablet.components.bed;

/**
 * Best-effort delivery service
 *
 * @author abomb4 2021-10-13 21:33:49
 */
public interface BedFacadeService {

    /**
     * Keep invoking {@link BedExecutor#execute(BedExecutorCmd)} with {@code cmd}
     * until {@link EnumBedResultType#COMPLETED}.
     *
     * @param executorType type of the executor
     * @param cmd          cmd object for invoking the {@code execute} of executor.
     * @param <T>          type of the cmd of the executor
     */
    <T extends BedExecutorCmd> void submit(Class<? extends BedExecutor<T>> executorType, T cmd);
}
