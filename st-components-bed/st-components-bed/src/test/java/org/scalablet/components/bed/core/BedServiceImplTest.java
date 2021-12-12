package org.scalablet.components.bed.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.BedResult;
import org.scalablet.components.bed.core.impl.BedSerializerJacksonImpl;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author abomb4 2021-11-12 11:14:05
 */
class BedServiceImplTest {

    private final BedExecutorRegistry bedExecutorRegistry = Mockito.mock(BedExecutorRegistry.class);
    private final BedTaskRepository bedTaskRepository = Mockito.mock(BedTaskRepository.class);
    private final BedDispatcher bedDispatcher = Mockito.mock(BedDispatcher.class);
    private final BedRunner bedRunner = Mockito.mock(BedRunner.class);
    private final BedConfiguration bedConfiguration = Mockito.mock(BedConfiguration.class);
    private final BedTracer bedTracer = Mockito.mock(BedTracer.class);

    @ParameterizedTest
    @MethodSource("stringIntAndListProvider")
    void succeedingTest(BedExecutor.RetryControl retryControl, BedExecutorCmd.ImmediatelyEnum immediate, int runTimes
            , int submitTimes) {

        // given: "test"
        BedServiceImpl test = new BedServiceImpl(bedExecutorRegistry, new BedSerializerJacksonImpl(),
                bedTaskRepository, bedRunner, bedConfiguration, bedDispatcher, bedTracer);

        final MockCmd cmd = new MockCmd();
        final MockExecutor executor = new MockExecutor();
        Mockito.when(bedExecutorRegistry.getExecutor(MockExecutor.class)).thenReturn(executor);

        cmd.immediate = immediate;
        executor.bedResult = BedResult.complete();
        executor.retryControl = retryControl;

        // and: "mock conf"
        Mockito.when(bedConfiguration.getServerRoomId()).thenReturn("aaa");

        // when: "submit"
        test.submit(MockExecutor.class, cmd);

        // then: "mock called"
        Mockito.verify(bedTaskRepository, Mockito.times(1)).save(any());
        Mockito.verify(bedDispatcher, Mockito.times(1)).taskCreated(any());
        Mockito.verify(bedRunner, Mockito.times(runTimes)).runImmediately(any());
        Mockito.verify(bedRunner, Mockito.times(submitTimes)).submitImmediately(any());
    }

    static Stream<Arguments> stringIntAndListProvider() {
        return Stream.of(
                arguments(BedExecutor.RetryControl.retry(10), BedExecutorCmd.ImmediatelyEnum.NO, 0, 0),
                arguments(BedExecutor.RetryControl.retry(10), BedExecutorCmd.ImmediatelyEnum.AT_CALLER_THREAD, 1, 0),
                arguments(BedExecutor.RetryControl.retry(10), BedExecutorCmd.ImmediatelyEnum.AT_BED_THREAD, 0, 1)
        );
    }

    // and: "mock executor"
    static class MockCmd implements BedExecutorCmd {
        private ImmediatelyEnum immediate;

        @Override
        public String getTaskId() {
            return "aaa";
        }

        @Nonnull
        @Override
        public ImmediatelyEnum executeOnceImmediately() {
            return this.immediate;
        }
    }

    static class MockExecutor implements BedExecutor<MockCmd> {
        BedResult bedResult;
        RetryControl retryControl;

        @Override
        public BedResult execute(MockCmd cmd) {
            return this.bedResult;
        }

        @Override
        public Class<MockCmd> getCmdClass() {
            return MockCmd.class;
        }

        @Override
        public RetryControl shouldRetry(MockCmd cmd, int executedTimes) {
            return this.retryControl;
        }
    }
}
