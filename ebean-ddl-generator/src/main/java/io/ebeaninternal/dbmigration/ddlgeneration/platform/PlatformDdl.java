package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbDefaultValue;
import io.ebean.config.dbplatform.DbIdentity;
import io.ebean.config.dbplatform.IdType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.dbmigration.ddlgeneration.*;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.VowelRemover;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Controls the DDL generation for a specific database platform.
 */
public class PlatformDdl {

  protected final DatabasePlatform platform;

  protected PlatformHistoryDdl historyDdl = new NoHistorySupportDdl();

  /**
   * Converter for logical/standard types to platform specific types. (eg. clob -> text)
   */
  private final PlatformTypeConverter typeConverter;

  /**
   * For handling support of sequences and autoincrement.
   */
  private final DbIdentity dbIdentity;

  /**
   * Set to true if table and column comments are included inline with the create statements.
   */
  protected boolean inlineComments;

  /**
   * Default assumes if exists is supported.
   */
  protected String dropTableIfExists = "drop table if exists ";

  protected String dropTableCascade = "";

  /**
   * Default assumes if exists is supported.
   */
  protected String dropSequenceIfExists = "drop sequence if exists ";

  protected String foreignKeyOnDelete = "on delete";
  protected String foreignKeyOnUpdate = "on update";

  protected String identitySuffix = " auto_increment";
  protected String identityStartWith = "start with";
  protected String identityIncrementBy = "increment by";
  protected String identityCache = "cache";
  protected String sequenceStartWith = "start with";
  protected String sequenceIncrementBy = "increment by";
  protected String sequenceCache = "cache";

  protected String alterTableIfExists = "";

  protected String dropConstraintIfExists = "drop constraint if exists";

  protected String dropIndexIfExists = "drop index if exists ";

  protected String alterColumn = "alter column";

  protected String alterColumnSuffix = "";

  protected String dropUniqueConstraint = "drop constraint";

  protected String addConstraint = "add constraint";

  protected String addColumn = "add column";

  protected String addColumnSuffix = "";

  protected String columnSetType = "";

  protected String columnSetDefault = "set default";

  protected String columnDropDefault = "drop default";

  protected String columnSetNotnull = "set not null";

  protected String columnSetNull = "set null";

  protected String updateNullWithDefault = "update ${table} set ${column} = ${default} where ${column} is null";

  protected String createTable = "create table";

  protected String dropColumn = "drop column";

  protected String dropColumnSuffix = "";

  protected String addForeignKeySkipCheck = "";

  protected String uniqueIndex = "unique";
  protected String indexConcurrent = "";
  protected String createIndexIfNotExists = "";

  /**
   * Set false for MsSqlServer to allow multiple nulls for OneToOne mapping.
   */
  protected boolean inlineUniqueWhenNullable = true;

  protected DbConstraintNaming naming;

  /**
   * Generally not desired as then they are not named (used with SQLite).
   */
  protected boolean inlineForeignKeys;

  protected boolean includeStorageEngine;

  protected final DbDefaultValue dbDefaultValue;

  protected String fallbackArrayType = "varchar(1000)";

  public PlatformDdl(DatabasePlatform platform) {
    this.platform = platform;
    this.dbIdentity = platform.getDbIdentity();
    this.dbDefaultValue = platform.getDbDefaultValue();
    this.typeConverter = new PlatformTypeConverter(platform.getDbTypeMap());
  }

  /**
   * Set configuration options.
   */
  public void configure(DatabaseConfig config) {
    historyDdl.configure(config, this);
    naming = config.getConstraintNaming();
  }

  /**
   * Create a DdlHandler for the specific database platform.
   */
  public DdlHandler createDdlHandler(DatabaseConfig config) {
    return new BaseDdlHandler(config, this);
  }

  /**
   * Return the identity type to use given the support in the underlying database
   * platform for sequences and identity/autoincrement.
   */
  public IdType useIdentityType(IdType modelIdentity) {
    if (modelIdentity == null) {
      // use the default
      return dbIdentity.getIdType();
    }
    return identityType(modelIdentity, dbIdentity.getIdType(), dbIdentity.isSupportsSequence(), dbIdentity.isSupportsIdentity());
  }

  /**
   * Determine the id type to use based on requested identityType and
   * the support for that in the database platform.
   */
  private IdType identityType(IdType modelIdentity, IdType platformIdType, boolean supportsSequence, boolean supportsIdentity) {
    switch (modelIdentity) {
      case GENERATOR:
        return IdType.GENERATOR;
      case EXTERNAL:
        return IdType.EXTERNAL;
      case SEQUENCE:
        return supportsSequence ? IdType.SEQUENCE : platformIdType;
      case IDENTITY:
        return supportsIdentity ? IdType.IDENTITY : platformIdType;
      default:
        return platformIdType;
    }
  }

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
    return columnDefn + identitySuffix;
  }

  /**
   * SQl2003 standard identity definition.
   */
  protected String asIdentityStandardOptions(String columnDefn, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder(columnDefn.length() + 60);
    sb.append(columnDefn).append(identity.optionGenerated());
    sb.append(identity.identityOptions(identityStartWith, identityIncrementBy, identityCache));
    return sb.toString();
  }

  /**
   * Return true if the table and column comments are included inline.
   */
  public boolean isInlineComments() {
    return inlineComments;
  }

  /**
   * Return true if the platform includes storage engine clause.
   */
  public boolean isIncludeStorageEngine() {
    return includeStorageEngine;
  }

  /**
   * Return true if foreign key reference constraints need to inlined with create table.
   * Ideally we don't do this as then the constraints are not named. Do this for SQLite.
   */
  public boolean isInlineForeignKeys() {
    return inlineForeignKeys;
  }

  /**
   * By default this does nothing returning null / no lock timeout.
   */
  public String setLockTimeout(int lockTimeoutSeconds) {
    return null;
  }

  /**
   * Write all the table columns converting to platform types as necessary.
   */
  public void writeTableColumns(DdlBuffer apply, List<Column> columns, DdlIdentity identity) {
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        apply.append(",");
      }
      apply.newLine();
      writeColumnDefinition(apply, columns.get(i), identity);
    }

    for (Column column : columns) {
      String checkConstraint = column.getCheckConstraint();
      if (hasValue(checkConstraint)) {
        checkConstraint = createCheckConstraint(maxConstraintName(column.getCheckConstraintName()), checkConstraint);
        if (hasValue(checkConstraint)) {
          apply.append(",").newLine();
          apply.append(checkConstraint);
        }
      }
    }
  }

  /**
   * Write the column definition to the create table statement.
   */
  protected void writeColumnDefinition(DdlBuffer buffer, Column column, DdlIdentity identity) {

    String columnDefn = convert(column.getType());
    if (identity.useIdentity() && isTrue(column.isPrimaryKey())) {
      columnDefn = asIdentityColumn(columnDefn, identity);
    }

    buffer.append("  ");
    buffer.append(lowerColumnName(column.getName()), 29);
    buffer.append(columnDefn);
    if (!Boolean.TRUE.equals(column.isPrimaryKey())) {
      String defaultValue = convertDefaultValue(column.getDefaultValue());
      if (defaultValue != null) {
        buffer.append(" default ").append(defaultValue);
      }
    }
    if (isTrue(column.isNotnull()) || isTrue(column.isPrimaryKey())) {
      writeColumnNotNull(buffer);
    }

    // add check constraints later as we really want to give them a nice name
    // so that the database can potentially provide a nice SQL error
  }

  /**
   * Allow for platform overriding (e.g. ClickHouse).
   */
  protected void writeColumnNotNull(DdlBuffer buffer) {
    buffer.append(" not null");
  }

  /**
   * Returns the check constraint.
   */
  public String createCheckConstraint(String ckName, String checkConstraint) {
    return "  constraint " + ckName + " " + checkConstraint;
  }

  /**
   * Convert the DB column default literal to platform specific.
   */
  public String convertDefaultValue(String dbDefault) {
    return dbDefaultValue.convert(dbDefault);
  }

  /**
   * Return the drop foreign key clause.
   */
  public String alterTableDropForeignKey(String tableName, String fkName) {
    return "alter table " + alterTableIfExists + tableName + " " + dropConstraintIfExists + " " + maxConstraintName(fkName);
  }

  /**
   * Convert the standard type to the platform specific type.
   */
  public String convert(String type) {
    if (type == null) {
      return null;
    }
    type = extract(type);
    if (type.contains("[]")) {
      return convertArrayType(type);
    }
    return typeConverter.convert(type);
  }

  // if columnType is different for different platforms, use pattern
  // @Column(columnDefinition = PLATFORM1;DEFINITION1;PLATFORM2;DEFINITON2;DEFINITON-DEFAULT)
  // e.g. @Column(columnDefinition = "db2;blob(64M);sqlserver,h2;varchar(227);varchar(127)")
  private String extract(String type) {
    String[] tmp = type.split(";");
    if (tmp.length % 2 == 0) {
      throw new IllegalArgumentException("You need an odd number of arguments. See Issue #2559 for details");
    }
    for (int i = 0; i < tmp.length - 2; i += 2) {
      String[] platforms = tmp[i].split(",");
      for (String plat : platforms) {
        if (platform.isPlatform(Platform.valueOf(plat.toUpperCase(Locale.ENGLISH)))) {
          return tmp[i + 1];
        }
      }
    }
    return tmp[tmp.length - 1]; // else
  }

  /**
   * Convert the logical array type to a db platform specific type to support the array data.
   */
  protected String convertArrayType(String logicalArrayType) {
    if (logicalArrayType.endsWith("]")) {
      return fallbackArrayType;
    }
    int colonPos = logicalArrayType.lastIndexOf(']');
    return "varchar" + logicalArrayType.substring(colonPos + 1);
  }

  /**
   * Add history support to this table using the platform specific mechanism.
   */
  public void createWithHistory(DdlWrite writer, MTable table) {
    historyDdl.createWithHistory(writer, table);
  }

  /**
   * Drop history support for a given table.
   */
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    historyDdl.dropHistoryTable(writer, dropHistoryTable);
  }

  /**
   * Add history support to an existing table.
   */
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    historyDdl.addHistoryTable(writer, addHistoryTable);
  }

  /**
   * Regenerate the history triggers (or function) due to a column being added/dropped/excluded or included.
   */
  public void regenerateHistoryTriggers(DdlWrite writer, HistoryTableUpdate update) {
    historyDdl.updateTriggers(writer, update);
  }

  /**
   * Generate and return the create sequence DDL.
   */
  public String createSequence(String sequenceName, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(sequenceName);
    sb.append(identity.sequenceOptions(sequenceStartWith, sequenceIncrementBy, sequenceCache));
    sb.append(";");
    return sb.toString();
  }

  /**
   * Return the drop sequence statement (potentially with if exists clause).
   */
  public String dropSequence(String sequenceName) {
    return dropSequenceIfExists + sequenceName;
  }

  /**
   * Return the drop table statement (potentially with if exists clause).
   */
  public String dropTable(String tableName) {
    return dropTableIfExists + tableName + dropTableCascade;
  }

  /**
   * Return the drop index statement for known non concurrent index.
   */
  public String dropIndex(String indexName, String tableName) {
    return dropIndex(indexName, tableName, false);
  }

  /**
   * Return the drop index statement.
   */
  public String dropIndex(String indexName, String tableName, boolean concurrent) {
    return dropIndexIfExists + maxConstraintName(indexName);
  }

  public String createIndex(WriteCreateIndex create) {
    if (create.useDefinition()) {
      return create.getDefinition();
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append("create ");
    if (create.isUnique()) {
      buffer.append(uniqueIndex).append(" ");
    }
    buffer.append("index ");
    if (create.isConcurrent()) {
      buffer.append(indexConcurrent);
    }
    if (create.isNotExistsCheck()) {
      buffer.append(createIndexIfNotExists);
    }
    buffer.append(maxConstraintName(create.getIndexName())).append(" on ").append(create.getTableName());
    appendColumns(create.getColumns(), buffer);
    return buffer.toString();
  }

  /**
   * Return the foreign key constraint when used inline with create table.
   */
  public String tableInlineForeignKey(WriteForeignKey request) {

    StringBuilder buffer = new StringBuilder(90);
    buffer.append("foreign key");
    appendColumns(request.cols(), buffer);
    buffer.append(" references ").append(lowerTableName(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    return buffer.toString();
  }

  /**
   * Add foreign key.
   */
  public String alterTableAddForeignKey(DdlOptions options, WriteForeignKey request) {

    StringBuilder buffer = new StringBuilder(90);
    buffer
      .append("alter table ").append(lowerTableName(request.table()))
      .append(" add constraint ").append(maxConstraintName(request.fkName()))
      .append(" foreign key");
    appendColumns(request.cols(), buffer);
    buffer
      .append(" references ")
      .append(lowerTableName(request.refTable()));
    appendColumns(request.refCols(), buffer);
    appendForeignKeySuffix(request, buffer);
    if (options.isForeignKeySkipCheck()) {
      buffer.append(addForeignKeySkipCheck);
    }
    return buffer.toString();
  }

  protected void appendForeignKeySuffix(WriteForeignKey request, StringBuilder buffer) {
    appendForeignKeyOnDelete(buffer, withDefault(request.onDelete()));
    appendForeignKeyOnUpdate(buffer, withDefault(request.onUpdate()));
  }

  protected ConstraintMode withDefault(ConstraintMode mode) {
    return (mode == null) ? ConstraintMode.RESTRICT : mode;
  }

  protected void appendForeignKeyOnDelete(StringBuilder buffer, ConstraintMode mode) {
    appendForeignKeyMode(buffer, foreignKeyOnDelete, mode);
  }

  protected void appendForeignKeyOnUpdate(StringBuilder buffer, ConstraintMode mode) {
    appendForeignKeyMode(buffer, foreignKeyOnUpdate, mode);
  }

  protected void appendForeignKeyMode(StringBuilder buffer, String onMode, ConstraintMode mode) {
    buffer.append(" ").append(onMode).append(" ").append(translate(mode));
  }

  protected String translate(ConstraintMode mode) {
    switch (mode) {
      case SET_NULL:
        return "set null";
      case SET_DEFAULT:
        return "set default";
      case RESTRICT:
        return "restrict";
      case CASCADE:
        return "cascade";
      default:
        throw new IllegalStateException("Unknown mode " + mode);
    }
  }

  /**
   * Drop a unique constraint from the table (Sometimes this is an index).
   */
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return "alter table " + tableName + " " + dropUniqueConstraint + " " + maxConstraintName(uniqueConstraintName);
  }

  /**
   * Drop a unique constraint from the table.
   */
  public String alterTableDropConstraint(String tableName, String constraintName) {
    return "alter table " + tableName + " " + dropConstraintIfExists + " " + maxConstraintName(constraintName);
  }

  /**
   * Add a unique constraint to the table.
   * <p>
   * Overridden by MsSqlServer for specific null handling on unique constraints.
   */
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {

    StringBuilder buffer = new StringBuilder(90);
    buffer.append("alter table ").append(tableName).append(" add constraint ").append(maxConstraintName(uqName)).append(" unique ");
    appendColumns(columns, buffer);
    return buffer.toString();
  }

  public void alterTableAddColumn(DdlBuffer buffer, String tableName, Column column, boolean onHistoryTable, String defaultValue) {

    String convertedType = convert(column.getType());

    buffer.append("alter table ").append(tableName)
      .append(" ").append(addColumn).append(" ").append(column.getName())
      .append(" ").append(convertedType);

    // Add default value also to history table if it is not excluded
    if (defaultValue != null) {
      if (!onHistoryTable || !isTrue(column.isHistoryExclude())) {
        buffer.append(" default ");
        buffer.append(defaultValue);
      }
    }

    if (!onHistoryTable) {
      if (isTrue(column.isNotnull())) {
        writeColumnNotNull(buffer);
      }
      buffer.append(addColumnSuffix);
      buffer.endOfStatement();

      // check constraints cannot be added in one statement for h2
      if (!StringHelper.isNull(column.getCheckConstraint())) {
        String ddl = alterTableAddCheckConstraint(tableName, column.getCheckConstraintName(), column.getCheckConstraint());
        if (hasValue(ddl)) {
          buffer.append(ddl).endOfStatement();
        }
      }
    } else {
      buffer.append(addColumnSuffix);
      buffer.endOfStatement();
    }

  }

  public void alterTableDropColumn(DdlBuffer buffer, String tableName, String columnName) {
    buffer.append("alter table ").append(tableName).append(" ").append(dropColumn).append(" ").append(columnName)
      .append(dropColumnSuffix).endOfStatement();
  }

  /**
   * Return true if unique constraints for nullable columns can be inlined as normal.
   * Returns false for MsSqlServer and DB2 due to it's not possible to to put a constraint
   * on a nullable column
   */
  public boolean isInlineUniqueWhenNullable() {
    return inlineUniqueWhenNullable;
  }

  /**
   * Alter a column type.
   * <p>
   * Note that that MySql and SQL Server instead use alterColumnBaseAttributes()
   * </p>
   */
  public String alterColumnType(String tableName, String columnName, String type) {
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + columnSetType + convert(type) + alterColumnSuffix;
  }

  /**
   * Alter a column adding or removing the not null constraint.
   * <p>
   * Note that that MySql, SQL Server, and HANA instead use alterColumnBaseAttributes()
   * </p>
   */
  public String alterColumnNotnull(String tableName, String columnName, boolean notnull) {
    String suffix = notnull ? columnSetNotnull : columnSetNull;
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix + alterColumnSuffix;
  }

  /**
   * Alter table adding the check constraint.
   */
  public String alterTableAddCheckConstraint(String tableName, String checkConstraintName, String checkConstraint) {
    return "alter table " + tableName + " " + addConstraint + " " + maxConstraintName(checkConstraintName) + " " + checkConstraint;
  }

  /**
   * Alter column setting the default value.
   */
  public String alterColumnDefaultValue(String tableName, String columnName, String defaultValue) {
    String suffix = DdlHelp.isDropDefault(defaultValue) ? columnDropDefault : columnSetDefault + " " + convertDefaultValue(defaultValue);
    return "alter table " + tableName + " " + alterColumn + " " + columnName + " " + suffix + alterColumnSuffix;
  }

  /**
   * Alter column setting both the type and not null constraint.
   * <p>
   * Used by MySql, SQL Server, and HANA as these require both column attributes to be set together.
   * </p>
   */
  public String alterColumnBaseAttributes(AlterColumn alter) {
    // by default do nothing, only used by mysql, sql server, and HANA as they can only
    // modify the column with the full column definition
    return null;
  }

  protected void appendColumns(String[] columns, StringBuilder buffer) {
    buffer.append(" (");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append(lowerColumnName(columns[i].trim()));
    }
    buffer.append(")");
  }

  /**
   * Convert the table to lower case.
   * <p>
   * Override as desired. Generally lower case with underscore is a good cross database
   * choice for column/table names.
   */
  protected String lowerTableName(String name) {
    return naming.lowerTableName(name);
  }

  /**
   * Convert the column name to lower case.
   * <p>
   * Override as desired. Generally lower case with underscore is a good cross database
   * choice for column/table names.
   */
  protected String lowerColumnName(String name) {
    return naming.lowerColumnName(name);
  }

  public DatabasePlatform getPlatform() {
    return platform;
  }

  public String getUpdateNullWithDefault() {
    return updateNullWithDefault;
  }

  /**
   * Return true if null or trimmed string is empty.
   */
  protected boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Null safe Boolean true test.
   */
  protected boolean isTrue(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  /**
   * Add an inline table comment to the create table statement.
   */
  public void inlineTableComment(DdlBuffer apply, String tableComment) {
    // do nothing by default (MySql only)
  }

  /**
   * Add an table storage engine to the create table statement.
   */
  public void tableStorageEngine(DdlBuffer apply, String storageEngine) {
    // do nothing by default
  }

  /**
   * Add table comment as a separate statement (from the create table statement).
   */
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    if (DdlHelp.isDropComment(tableComment)) {
      tableComment = "";
    }
    apply.append(String.format("comment on table %s is '%s'", tableName, tableComment)).endOfStatement();
  }

  /**
   * Add column comment as a separate statement.
   */
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    if (DdlHelp.isDropComment(comment)) {
      comment = "";
    }
    apply.append(String.format("comment on column %s.%s is '%s'", table, column, comment)).endOfStatement();
  }

  /**
   * Use this to generate a prolog for each script (stored procedures)
   */
  public void generateProlog(DdlWrite writer) {

  }

  /**
   * Use this to generate an epilog. Will be added at the end of script
   */
  public void generateEpilog(DdlWrite writer) {

  }

  /**
   * Shortens the given name to the maximum constraint name length of the platform in a deterministic way.
   * <p>
   * First, all vowels are removed, If the string is still to long, 31 bits are taken from the hash code
   * of the string and base36 encoded (10 digits and 26 chars) string.
   * <p>
   * As 36^6 > 31^2, the resulting string is never longer as 6 chars.
   */
  protected String maxConstraintName(String name) {
    if (name.length() > platform.getMaxConstraintNameLength()) {
      int hash = name.hashCode() & 0x7FFFFFFF;
      name = VowelRemover.trim(name, 4);
      if (name.length() > platform.getMaxConstraintNameLength()) {
        return name.substring(0, platform.getMaxConstraintNameLength() - 7) + "_" + Integer.toString(hash, 36);
      }
    }
    return name;
  }

  /**
   * Mysql-specific: Locks all tables for triggers that have to be updated.
   */
  public void lockTables(DdlBuffer buffer, Collection<String> tables) {

  }

  /**
   * Mysql-specific: Unlocks all tables for triggers that have to be updated.
   */
  public void unlockTables(DdlBuffer buffer, Collection<String> tables) {

  }

  /**
   * Returns the database-specific "create table" command prefix. For HANA this is
   * either "create column table" or "create row table", for all other databases
   * it is "create table".
   *
   * @return The "create table" command prefix
   */
  public String getCreateTableCommandPrefix() {
    return createTable;
  }

  public boolean suppressPrimaryKeyOnPartition() {
    return false;
  }

  public void addTablePartition(DdlBuffer apply, String partitionMode, String partitionColumn) {
    // only supported by postgres initially
  }
}
