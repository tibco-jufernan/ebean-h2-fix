-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            integer not null,
  assoc_one                     nvarchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);
create sequence migtest_ckey_assoc_seq as bigint start with 1;

create table migtest_ckey_detail (
  id                            integer not null,
  something                     nvarchar(255),
  constraint pk_migtest_ckey_detail primary key (id)
);
create sequence migtest_ckey_detail_seq as bigint start with 1;

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       nvarchar(127) not null,
  name                          nvarchar(255),
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_fk_cascade (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_cascade primary key (id)
);
create sequence migtest_fk_cascade_seq as bigint start with 1;

create table migtest_fk_cascade_one (
  id                            numeric(19) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);
create sequence migtest_fk_cascade_one_seq as bigint start with 1;

create table migtest_fk_none (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none primary key (id)
);
create sequence migtest_fk_none_seq as bigint start with 1;

create table migtest_fk_none_via_join (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none_via_join primary key (id)
);
create sequence migtest_fk_none_via_join_seq as bigint start with 1;

create table migtest_fk_one (
  id                            numeric(19) not null,
  constraint pk_migtest_fk_one primary key (id)
);
create sequence migtest_fk_one_seq as bigint start with 1;

create table migtest_fk_set_null (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_set_null primary key (id)
);
create sequence migtest_fk_set_null_seq as bigint start with 1;

create table migtest_e_basic (
  id                            integer not null,
  status                        nvarchar(1),
  status2                       nvarchar(1) default 'N' not null,
  name                          nvarchar(127),
  description                   nvarchar(127),
  description_file              image,
  some_date                     datetime2,
  old_boolean                   bit default 0 not null,
  old_boolean2                  bit,
  eref_id                       integer,
  indextest1                    nvarchar(127),
  indextest2                    nvarchar(127),
  indextest3                    nvarchar(127),
  indextest4                    nvarchar(127),
  indextest5                    nvarchar(127),
  indextest6                    nvarchar(127),
  user_id                       integer not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;
create sequence migtest_e_basic_seq as bigint start with 1;

create table migtest_e_enum (
  id                            integer not null,
  test_status                   nvarchar(1),
  constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I')),
  constraint pk_migtest_e_enum primary key (id)
);
create sequence migtest_e_enum_seq as bigint start with 1;

create table migtest_e_history (
  id                            integer not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_history primary key (id)
);
create sequence migtest_e_history_seq as bigint start with 1;

create table migtest_e_history2 (
  id                            integer not null,
  test_string                   nvarchar(255),
  obsolete_string1              nvarchar(255),
  obsolete_string2              nvarchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);
create sequence migtest_e_history2_seq as bigint start with 1;

create table migtest_e_history3 (
  id                            integer not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);
create sequence migtest_e_history3_seq as bigint start with 1;

create table migtest_e_history4 (
  id                            integer not null,
  test_number                   integer,
  constraint pk_migtest_e_history4 primary key (id)
);
create sequence migtest_e_history4_seq as bigint start with 1;

create table migtest_e_history5 (
  id                            integer not null,
  test_number                   integer,
  constraint pk_migtest_e_history5 primary key (id)
);
create sequence migtest_e_history5_seq as bigint start with 1;

create table migtest_e_history6 (
  id                            integer not null,
  test_number1                  integer,
  test_number2                  integer not null,
  constraint pk_migtest_e_history6 primary key (id)
);
create sequence migtest_e_history6_seq as bigint start with 1;

create table migtest_e_ref (
  id                            integer not null,
  name                          nvarchar(127) not null,
  constraint pk_migtest_e_ref primary key (id)
);
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);
create sequence migtest_e_ref_seq as bigint start with 1;

create table migtest_e_softdelete (
  id                            integer not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);
create sequence migtest_e_softdelete_seq as bigint start with 1;

create table migtest_mtm_c (
  id                            integer not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);
create sequence migtest_mtm_c_seq as bigint start with 1;

create table migtest_mtm_m (
  id                            numeric(19) not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);
create sequence migtest_mtm_m_seq as bigint start with 1;

create table migtest_oto_child (
  id                            integer not null,
  name                          nvarchar(255),
  constraint pk_migtest_oto_child primary key (id)
);
create sequence migtest_oto_child_seq as bigint start with 1;

create table migtest_oto_master (
  id                            numeric(19) not null,
  name                          nvarchar(255),
  constraint pk_migtest_oto_master primary key (id)
);
create sequence migtest_oto_master_seq as bigint start with 1;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade;

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null;

create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);

alter table migtest_e_history2
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
alter table migtest_e_history3
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history3 set (system_versioning = on (history_table=dbo.migtest_e_history3_history));
alter table migtest_e_history4
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history4 set (system_versioning = on (history_table=dbo.migtest_e_history4_history));
alter table migtest_e_history5
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history5 set (system_versioning = on (history_table=dbo.migtest_e_history5_history));
alter table migtest_e_history6
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history6 set (system_versioning = on (history_table=dbo.migtest_e_history6_history));
