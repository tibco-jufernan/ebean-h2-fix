package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.List;

/**
 * Uses DB triggers to maintain a history table.
 */
public class PostgresHistoryDdl extends DbTriggerBasedHistoryDdl {

  PostgresHistoryDdl() {
    this.now = "current_timestamp";
    this.sysPeriodEndValue = "current_timestamp";
  }

  /**
   * Use Postgres create table like to create the history table.
   */
  @Override
  protected void createHistoryTable(DdlBuffer apply, MTable table) {
    apply.append("create table ").append(table.getName()).append(historySuffix)
      .append("(like ").append(table.getName()).append(")").endOfStatement();
  }

  /**
   * Use Postgres range type rather than start and end timestamps.
   */
  @Override
  protected void addSysPeriodColumns(DdlBuffer apply, String baseTableName, String whenCreatedColumn) {
    apply
      .append("alter table ").append(baseTableName)
      .append(" add column ").append(sysPeriod).append(" tstzrange not null default tstzrange(").append(now).append(", null)")
      .endOfStatement();

    if (whenCreatedColumn != null) {
      apply.append("update ").append(baseTableName).append(" set ")
        .append(sysPeriod).append(" = tstzrange(").append(whenCreatedColumn).append(", null)").endOfStatement();
    }
  }

  @Override
  protected void appendSysPeriodColumns(DdlBuffer apply, String prefix) {
    appendColumnName(apply, prefix, sysPeriod);
  }

  @Override
  protected void dropSysPeriodColumns(DdlBuffer buffer, String baseTableName) {
    buffer.append("alter table ").append(baseTableName).append(" drop column ").append(sysPeriod).endOfStatement();
  }

  @Override
  protected void createTriggers(DdlWrite writer, MTable table) {
    String baseTableName = table.getName();
    String procedureName = procedureName(baseTableName);
    String triggerName = triggerName(baseTableName);

    DdlBuffer apply = writer.applyHistoryTrigger();
    apply
      .append("create trigger ").append(triggerName).newLine()
      .append("  before update or delete on ").append(baseTableName).newLine()
      .append("  for each row execute procedure ").append(procedureName).append("();").newLine().newLine();
  }

  @Override
  protected void dropTriggers(DdlBuffer buffer, String baseTable) {
    // rollback trigger then function
    buffer.append("drop trigger if exists ").append(triggerName(baseTable)).append(" on ").append(baseTable).append(" cascade").endOfStatement();
    buffer.append("drop function if exists ").append(procedureName(baseTable)).append("()").endOfStatement();
    buffer.end();
  }

  protected void createOrReplaceFunction(DdlBuffer apply, String procedureName, String historyTable, List<String> includedColumns) {
    apply
      .append("create or replace function ").append(procedureName).append("() returns trigger as $$").newLine();

    apply.append("declare").newLine()
      .append("  lowerTs timestamptz;").newLine()
      .append("  upperTs timestamptz;").newLine();

    apply.append("begin").newLine()
      .append("  lowerTs = lower(OLD.sys_period);").newLine()
      .append("  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);").newLine();

    apply
      .append("  if (TG_OP = 'UPDATE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
      .append("    NEW.").append(sysPeriod).append(" = tstzrange(upperTs,null);").newLine()
      .append("    return new;").newLine();
    apply
      .append("  elsif (TG_OP = 'DELETE') then").newLine();
    appendInsertIntoHistory(apply, historyTable, includedColumns);
    apply
      .append("    return old;").newLine();
    apply
      .append("  end if;").newLine()
      .append("end;").newLine()
      .append("$$ LANGUAGE plpgsql;").newLine();

    apply.end();
  }

  @Override
  protected void createStoredFunction(DdlWrite writer, MTable table) {
    String procedureName = procedureName(table.getName());
    String historyTable = historyTableName(table.getName());

    List<String> columnNames = columnNamesForApply(table);
    createOrReplaceFunction(writer.applyHistoryTrigger(), procedureName, historyTable, columnNames);
  }

  @Override
  protected void updateHistoryTriggers(DbTriggerUpdate update) {
    String procedureName = procedureName(update.getBaseTable());
    recreateHistoryView(update);
    createOrReplaceFunction(update.historyTriggerBuffer(), procedureName, update.getHistoryTable(), update.getColumns());
  }

  @Override
  protected void appendInsertIntoHistory(DdlBuffer buffer, String historyTable, List<String> columns) {
    buffer.append("    insert into ").append(historyTable).append(" (").append(sysPeriod).append(",");
    appendColumnNames(buffer, columns, "");
    buffer.append(") values (tstzrange(lowerTs,upperTs), ");
    appendColumnNames(buffer, columns, "OLD.");
    buffer.append(");").newLine();
  }

}
