package org.scalablet.components.bed.core.impl;

import org.scalablet.components.bed.core.BedConfiguration;
import org.scalablet.components.bed.core.BedTask;
import org.scalablet.components.bed.core.BedTaskGet;
import org.scalablet.components.bed.core.BedTaskRepository;
import org.scalablet.components.bed.core.BedTaskStatusEnum;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 仓储层实现
 *
 * @author abomb4 2021-12-11 15:52:18 +0800
 */
public class BedTaskRepositoryImpl implements BedTaskRepository {

    /** JdbcTemplate */
    private final JdbcTemplate jdbcTemplate;
    /** config */
    private final BedConfiguration configuration;

    /**
     * 完整构造
     *
     * @param jdbcTemplate  jdbcTemplate
     * @param configuration 配置
     */
    public BedTaskRepositoryImpl(JdbcTemplate jdbcTemplate,
                                 BedConfiguration configuration) {
        this.jdbcTemplate = jdbcTemplate;
        this.configuration = configuration;
    }

    @Nonnull
    @Override
    public List<BedTask> getSomeNeedRunTasks(int limit, String resourceName, long durationSeconds) {
        // update and then query
        this.jdbcTemplate.update("""
                        update bed_task_info
                           set app_name = ?, update_datetime = now(), status = ?
                         where server_room_id = ? and resource_name = ? and
                         ((app_name is null and status in (?, ?)) or (status = ? and (now() - update_datetime) > ?))
                         limit ?
                        """,
                this.configuration.getAppId(), BedTaskStatusEnum.EXECUTING.getCode(),
                this.configuration.getServerRoomId(), resourceName,
                BedTaskStatusEnum.INIT.getCode(), BedTaskStatusEnum.RETRYING.getCode(),
                BedTaskStatusEnum.EXECUTING.getCode(), durationSeconds,
                limit);
        final List<BedTask> query = this.jdbcTemplate.query("""
                        select task_id, server_room_id, executor_type, executed_times,
                               status, cmd, trace_id, message, resource_name
                          from bed_task_info
                        """,
                (rs, rowNum) -> this.resultSetToTask(rs));

        return this.jdbcTemplate.query("""
                        select task_id, server_room_id, executor_type, executed_times,
                               status, cmd, trace_id, message, resource_name
                          from bed_task_info
                         where server_room_id = ? and app_name = ? and status = ?
                        """,
                (rs, rowNum) -> this.resultSetToTask(rs),
                this.configuration.getServerRoomId(), this.configuration.getAppId(),
                BedTaskStatusEnum.EXECUTING.getCode());
    }

    @Nullable
    @Override
    public <T extends BedTaskGet> BedTask getTaskByPrimary(T get) {
        return this.jdbcTemplate.queryForObject("""
                        select task_id, server_room_id, executor_type, executed_times,
                               status, cmd, trace_id, message, resource_name
                          from bed_task_info
                         where task_id = ?
                        """,
                (rs, rowNum) -> this.resultSetToTask(rs),
                get.getTaskId());
    }

    @Override
    public void save(BedTask bedTask) {

        this.jdbcTemplate.update("""
                        insert into bed_task_info (task_id, server_room_id, executor_type, executed_times,
                        status, cmd, trace_id, message, resource_name) values (?,?,?,?, ?,?,?,?,?)
                        """,
                bedTask.getTaskId(), bedTask.getServerRoomId(), bedTask.getExecutorType(), bedTask.getExecutedTimes(),
                bedTask.getStatus().getCode(), bedTask.getCmd(), bedTask.getTraceId(), bedTask.getLastMessage(),
                bedTask.getResourceName());
    }

    @Override
    public void updateExecuted(BedTask bedTask) {

    }

    private BedTask resultSetToTask(ResultSet rs) throws SQLException {
        final String taskId = rs.getString(1);
        final String serverRoomId = rs.getString(2);
        final String executorType = rs.getString(3);
        final int executedTimes = rs.getInt(4);
        final String status = rs.getString(5);
        final String cmd = rs.getString(6);
        final String traceId = rs.getString(7);
        final String message = rs.getString(8);
        final String resourceName = rs.getString(9);
        return new BedTask(taskId, serverRoomId, resourceName, executorType, executedTimes,
                BedTaskStatusEnum.getByCode(status), 0, traceId, message, cmd);
    }
}
