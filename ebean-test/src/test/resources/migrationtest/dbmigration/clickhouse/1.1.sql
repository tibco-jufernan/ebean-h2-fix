-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_e_user (
  id                            UInt32
) ENGINE = Log();

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              UInt32,
  migtest_mtm_m_id              UInt64
) ENGINE = Log();

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              UInt64,
  migtest_mtm_c_id              UInt32
) ENGINE = Log();

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              UInt64,
  value                         String
) ENGINE = Log();

alter table migtest_ckey_detail add column one_key UInt32;
alter table migtest_ckey_detail add column two_key String;

alter table migtest_ckey_parent add column assoc_id UInt32;


update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic alter column status set default 'A';
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic alter column status2 String;
alter table migtest_e_basic alter column status2 drop default;
alter table migtest_e_basic alter column status2 set null;

-- rename all collisions;
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic alter column user_id set null;
alter table migtest_e_basic add column new_string_field String default 'foo''bar';
alter table migtest_e_basic add column new_boolean_field UInt8 default true;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add column new_boolean_field2 UInt8 default true;
alter table migtest_e_basic add column progress UInt32 default 0;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add column new_integer UInt32 default 42;

alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint uq_migtest_e_basic_status_indextest1 unique  (status,indextest1);
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
alter table migtest_e_history alter column test_string UInt64;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 String;
alter table migtest_e_history2 add column test_string3 String default 'unknown';
alter table migtest_e_history2 add column new_column String;

alter table migtest_e_history4 alter column test_number UInt64;
alter table migtest_e_history5 add column test_boolean UInt8 default false;


-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 set null;
alter table migtest_e_softdelete add column deleted UInt8 default false;

alter table migtest_oto_child add column master_id UInt64;

