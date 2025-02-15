-- Migrationscripts for ebean unittest
-- apply changes
alter table migtest_e_basic drop column description_file;

alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

alter table migtest_e_history2 drop column obsolete_string1;

alter table migtest_e_history2 drop column obsolete_string2;

drop table migtest_e_ref cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_ref_seq';
exception
  when expected_error then null;
end;
$$;
