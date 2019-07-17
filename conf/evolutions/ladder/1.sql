# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table binaries (
  id                            integer auto_increment not null,
  name                          varchar(255),
  type                          varchar(255),
  t_create                      datetime(6),
  data                          varbinary(255),
  constraint pk_binaries primary key (id)
);

create table cellocation (
  id                            integer auto_increment not null,
  cell_mcc                      integer,
  cell_mnc                      integer,
  cell_lac                      integer,
  cell_cid                      integer,
  lat                           double,
  lon                           double,
  radius                        double,
  address                       varchar(255),
  constraint pk_cellocation primary key (id)
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

create table device_info (
  id                            integer auto_increment not null,
  imei                          varchar(255),
  iplocation_id                 integer,
  cellocation_id                integer,
  device_name                   varchar(255),
  maintenance_type              varchar(255),
  maintenance_nexttime          varchar(255),
  maintenance_remind            varchar(255),
  maintenance_lasttime          varchar(255),
  inspection_type               varchar(255),
  inspection_lasttime           varchar(255),
  inspection_nexttime           varchar(255),
  inspection_remind             varchar(255),
  install_date                  varchar(255),
  install_addr                  varchar(255),
  register                      varchar(255),
  tagcolor                      varchar(255),
  state                         varchar(255),
  device_type                   varchar(255),
  commond                       varchar(255),
  delay                         varchar(255),
  rssi                          integer,
  runtime_state                 integer,
  group_id                      integer,
  ladder_id                     integer,
  constraint pk_device_info primary key (id)
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

create table iplocation (
  id                            integer auto_increment not null,
  ip                            varchar(255),
  area                          varchar(255),
  area_id                       varchar(255),
  city                          varchar(255),
  city_id                       varchar(255),
  country                       varchar(255),
  country_id                    varchar(255),
  county                        varchar(255),
  county_id                     varchar(255),
  isp                           varchar(255),
  region                        varchar(255),
  region_id                     varchar(255),
  constraint pk_iplocation primary key (id)
);

create table ladder (
  id                            integer auto_increment not null,
  ctrl_id                       integer,
  name                          varchar(255),
  ctrl                          varchar(255),
  door1                         varchar(255),
  door2                         varchar(255),
  install_addr                  varchar(255),
  state                         varchar(255),
  constraint pk_ladder primary key (id)
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

create table offline (
  id                            integer auto_increment not null,
  device_id                     integer,
  t_logout                      datetime(6),
  constraint pk_offline primary key (id)
);

create table `order` (
  id                            integer auto_increment not null,
  device_id                     integer,
  type                          integer,
  create_time                   varchar(255),
  state                         varchar(255),
  code                          integer,
  device_type                   varchar(255),
  producer                      varchar(255),
  islast                        integer,
  constraint pk_order primary key (id)
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

drop table if exists cellocation;

drop table if exists commands;

drop table if exists credentials;

drop table if exists device_info;

drop table if exists devices;

drop table if exists docks;

drop table if exists events;

drop table if exists iplocation;

drop table if exists ladder;

drop table if exists logs;

drop table if exists monitor;

drop table if exists offline;

drop table if exists `order`;

drop table if exists runtime;

