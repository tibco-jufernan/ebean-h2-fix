package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

/**
 * Defines the implementation for adding history support to a table.
 */
public interface PlatformHistoryDdl {

  /**
   * Configure typically reading the necessary parameters from DatabaseConfig and Platform.
   */
  void configure(DatabaseConfig config, PlatformDdl platformDdl);

  /**
   * Creates a new table and add history support to the table using platform specific mechanism.
   */
  void createWithHistory(DdlWrite writer, MTable table);

  /**
   * Drop history support for the given table.
   */
  void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable);

  /**
   * Add history support to the given table.
   */
  void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable);

  /**
   * Regenerate the history triggers/stored function due to column added/dropped/included or excluded.
   */
  void updateTriggers(DdlWrite writer, HistoryTableUpdate baseTable);
}
