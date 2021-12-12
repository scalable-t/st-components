create table bed_task_info (
    task_id varchar(64) not null primary key,
    server_room_id varchar(32) not null,
    executor_type varchar(64) not null,
    executed_times int not null default 0,
    app_name varchar(32),
    status char(1) not null default 'I',
    cmd text,
    trace_id varchar(32),
    next_execute_datetime datetime,
    create_datetime datetime,
    update_datetime datetime
);
