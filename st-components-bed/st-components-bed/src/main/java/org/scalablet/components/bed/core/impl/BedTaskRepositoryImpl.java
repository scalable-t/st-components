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
     * @param jdbcTemplate jdbcTemplate
     */
    public BedTaskRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nonnull
    @Override
    public List<BedTask> getSomeNeedRunTasks(int limit, String resourceName) {
        // update and then query
        final int updated = this.jdbcTemplate.update("""
                update bed_task_info
                   set app_name = ?, update_datetime = now(), status = (?, ?)
                 where server_room_id = ? and resource_name = ? and v
                 ((app_name = null and status = ?) or (status = ? and (now - update_datetime) > ?))
                 limit ?
                """, this.configuration.getAppId(), BedTaskStatusEnum.EXECUTING,
                this.configuration.getServerRoomId(), resourceName, );
        return this.jdbcTemplate.query("""
                select task_id, server_room_id, executor_type, executed_times, status, cmd, trace_id, message
                  from bed_task_info
                 where server_room_id = ? and app_name = ? and status = ?
                """,
                (rs, rowNum) -> this.resultSetToTask(rs),
                this.configuration.getServerRoomId(), this.configuration.getAppId(), BedTaskStatusEnum.EXECUTING.getCode());
    }

    @Nullable
    @Override
    public <T extends BedTaskGet> BedTask getTaskByPrimary(T get) {
        return null;
    }

    @Override
    public void save(BedTask bedTask) {

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
        return new BedTask(taskId, serverRoomId, executorType, executedTimes, BedTaskStatusEnum.getByCode(status),
                0, traceId, message, cmd);
    }
}
