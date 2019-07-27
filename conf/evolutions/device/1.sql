# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table binaries (
  id                            integer auto_increment not null,
  t_create                      datetime(6),
  data                          varbinary(255),
  constraint pk_binaries primary key (id)
);

create table commands (
  id                            integer auto_increment not null,
  imei                          varchar(255),
  command                       varchar(255),
  `int1`                        integer,
  `int2`                        integer,
  `int3`                        integer,
  `int4`                        integer,
  str1                          varchar(255),
  str2                          varchar(255),
  str3                          varchar(255),
  str4                          varchar(255),
  contract                      varbinary(255),
  binary_id                     integer,
  result                        varchar(255),
  submit                        datetime(6),
  execute                       datetime(6),
  finish                        datetime(6),
  `binary`                      varbinary(255),
  constraint pk_commands primary key (id)
);

create table credentials (
  id                            integer auto_increment not null,
  device_id                     integer,
  credential                    varbinary(255),
  constraint pk_credentials primary key (id)
);

create table devices (
  id                            integer auto_increment not null,
  t_create                      datetime(6),
  t_update                      datetime(6),
  t_logon                       datetime(6),
  t_logout                      datetime(6),
  dock_id                       integer,
  board                         varchar(255),
  cellular                      varchar(255),
  firmware                      varchar(255),
  imei                          varchar(255) not null,
  imsi                          varchar(255),
  device                        varchar(255),
  model                         varchar(255),
  contract_id                   varbinary(255),
  cell_mcc                      integer,
  cell_mnc                      integer,
  cell_lac                      integer,
  cell_cid                      integer,
  ipaddr                        varchar(255),
  order_times                   integer,
  constraint uq_devices_imei unique (imei),
  constraint pk_devices primary key (id)
);

create table docks (
  id                            integer auto_increment not null,
  name                          varchar(255),
  `desc`                        varchar(255),
  t_create                      datetime(6),
  t_update                      datetime(6),
  t_logon                       datetime(6),
  t_logout                      datetime(6),
  ipaddr                        varchar(255),
  uuid                          varchar(255),
  constraint pk_docks primary key (id)
);

create table events (
  id                            integer auto_increment not null,
  device_id                     integer,
  time                          datetime(6),
  `length`                      integer,
  `interval`                    integer,
  data                          varbinary(255),
  constraint pk_events primary key (id)
);

create table logs (
  id                            integer auto_increment not null,
  dock_id                       integer,
  device_id                     integer,
  time                          datetime(6),
  constraint pk_logs primary key (id)
);

create table monitor (
  id                            integer auto_increment not null,
  device_id                     integer,
  session                       integer,
  sequence                      integer,
  `length`                      integer,
  `interval`                    integer,
  time                          datetime(6),
  data                          varbinary(255),
  constraint pk_monitor primary key (id)
);

create table runtime (
  id                            integer auto_increment not null,
  device_id                     integer,
  type                          integer,
  data                          varbinary(255),
  t_update                      datetime(6),
  constraint pk_runtime primary key (id)
);


# --- !Downs

drop table if exists binaries;

drop table if exists commands;

drop table if exists credentials;

drop table if exists devices;

drop table if exists docks;

drop table if exists events;

drop table if exists logs;

drop table if exists monitor;

drop table if exists runtime;

