package org.scalablet.components.bed.autoconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.BedFacadeService;
import org.scalablet.components.bed.BedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author abomb4 2021-12-14 22:34:48 +0800
 */
@Slf4j
@SpringBootTest(classes = {
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        BedAutoConfiguration.class,
        BedAutoConfigurationTest.TestConfig.class
})
@ActiveProfiles("test1")
class BedAutoConfigurationTest {

    @Autowired
    private BedFacadeService bedFacadeService;

    @Autowired
    private TestExecutor testExecutor;

    @Test
    void test1() throws InterruptedException {
        final Object lock = new Object();
        this.testExecutor.callListener = v -> {
            synchronized (lock) {
                lock.notifyAll();
            }
        };

        final long start = System.currentTimeMillis();
        this.bedFacadeService.submit(TestExecutor.class, new TestCmd("1", "kkk1"));

        synchronized (lock) {
            lock.wait(TimeUnit.SECONDS.toMillis(10L));
        }
        assertFalse(this.testExecutor.executeList.isEmpty(), "10s 都没有执行，出问题了");
        log.info("经过 {}ms 后异步任务得到执行。", System.currentTimeMillis() - start);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public TestExecutor beanTestExecutor() {
            return new TestExecutor();
        }
    }

    static class TestExecutor implements BedExecutor<TestCmd> {

        final List<TestCmd> executeList = new CopyOnWriteArrayList<>();
        BedResult result = BedResult.complete();
        Consumer<TestCmd> callListener;

        @Override
        public BedResult execute(TestCmd cmd) {
            this.executeList.add(cmd);
            if (this.callListener != null) {
                this.callListener.accept(cmd);
            }
            return this.result;
        }

        @Override
        public Class<TestCmd> getCmdClass() {
            return TestCmd.class;
        }

        @Override
        public RetryControl shouldRetry(TestCmd cmd, int executedTimes) {
            return executedTimes == 0 ? RetryControl.retry(3) : RetryControl.noRetry();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestCmd implements BedExecutorCmd {
        private String taskId;
        private String key;
    }
}
