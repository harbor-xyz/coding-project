create table config
(
    name         varchar(255)                not null
        constraint config_pk
            primary key,
    value        varchar(4096),
    last_updated datetime2 default getdate() not null
)
    go

create table course_data
(
    name    nvarchar(20),
    teacher nvarchar(20),
    id      int identity
)
    go

create table [user]
(
    id           int identity,
    name         nvarchar(100),
    mobile_number nvarchar(100),
    email_at      nvarchar(100),
    created_at    datetime2 default CURRENT_TIMESTAMP,
    updated_at    datetime2 default CURRENT_TIMESTAMP,
    created_by    nvarchar(30),
    updated_by    nvarchar(30)
    )
    go

create table user_availability
(
    user_id   int,
    date      bigint,
    start_time bigint,
    end_time   bigint,
    created_at    datetime2 default CURRENT_TIMESTAMP,
    updated_at    datetime2 default CURRENT_TIMESTAMP,
    created_by    nvarchar(30),
    updated_by    nvarchar(30)

)
    go

