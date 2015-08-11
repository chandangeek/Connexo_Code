package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Describes a relational table, and its mapping to objects.
 * For all methods, name typically refers to the database name,
 * while fieldName refers to a java instance field.
 */
@ProviderType
public interface Table<T> {

    int NAME_LENGTH = 80;
    int SHORT_DESCRIPTION_LENGTH = 256;
    int DESCRIPTION_LENGTH = 4000;

    // datamodel construction api
    Column.Builder column(String name);
    PrimaryKeyConstraint.Builder primaryKey(String name);
    UniqueConstraint.Builder unique(String name);
    ForeignKeyConstraint.Builder foreignKey(String name);

    Index.Builder index(String name);

    /*
     * adds a column that is incremented on every update
     * that will be used for optimistic lock checks
     */
    Column addVersionCountColumn(String name, String dbType, String fieldName);

    /*
     * adds a discriminator column that is used to map a tuple to the correct implementation class
     */
    Column addDiscriminatorColumn(String name, String dbType);

    /*
     * adds a column that on insert will be set to the create time, and will never be updated
     */
    Column addCreateTimeColumn(String name, String fieldName);

    /*
     * adds a column that on insert and update will be set to the current time
     */
    Column addModTimeColumn(String name, String fieldName);

    /*
     * adds a column that will contain the user name (obtained from ThreadPrincipalService.getPrincipal.getName)
     */
    Column addUserNameColumn(String name, String fieldName);

    /*
     * adds a column named id , database type number mapped to long and
     * back it with a sequence named tablename + id
     */
    Column addAutoIdColumn();

    /*
     * adds a column named position that will automatically be maintained by the orm layer
     */
    Column addPositionColumn();

    /*
     * Adds the following columns:
     *
     * a version count column VERSIONCOUNT mapped to field version
     * a create time column CREATETIME mapped to field createTime
     * a mod time column MODTIME mapped to field modTime
     * a user name column USERNAME mapped to field userName
     *
     */
    List<Column> addAuditColumns();
    /*
     *  Adds three columns to persist a Quantity field
     *
     *  column name + VALUE mapped to field fieldName.value
     *  column name + MULTIPLIER mapped to fieldName.multiplier
     *  column name + UNIT mapped to fieldName.unit
     *
     */
    List<Column> addQuantityColumns(String name, boolean notNull, String fieldName);
    /*
     *  Adds two column to persist a Money field
     *
     *	column name + VALUE mapped to fieldName.value
     *	column name + CURRENCY mapped to fieldName.currency
     */
    List<Column> addMoneyColumns(String name, boolean notNull, String fieldName);
    /*
     *  Adds two column to persist an Interval field
     *
     *	STARTTIME mapped to fieldName.start
     *  ENDTIME mapped to fieldName.end
     *
     */
    List<Column> addIntervalColumns(String fieldName);
    /*
     * Adds four columns to persist a RefAny field
     *
     *  column name + CMP mapped to fieldName.component (component name of the referenced tuple)
     *  column name + TABLE mapped to fieldName.table (table name of the referenced tuple)
     *  column name + KEY mapped to fieldName.key (the json representation of the primary key of the referenced tuple)
     *  column name + ID mapped to fieldName.id (the id of the referenced tuple if it has an auto id column as primary key, NULL otherwise)
     */
    List<Column> addRefAnyColumns(String name, boolean notNull, String fieldName);
    /*
     * by specifying a journal table name, the orm layer will automatically create a journal table to store obsolete tuple versions
     */
    void setJournalTableName(String journalTableName);
    /*
     * activates caching. The cache is shared between all threads of a Java VM,
     * so implementers must make sure the Java type is thread safe
     * Only cache tables with a small number of tuples that are read only most of the time
     *
     */
    void cache();

    void indexOrganized(int compressCount);

    // meta data api
    List<String> getDdl();

    DataModel getDataModel();

    String getSchema();

    String getName();

    String getQualifiedName();

    String getJournalTableName();

    boolean hasJournal();

    boolean isCached();

    boolean isIndexOrganized();
    List<? extends Column> getColumns();
    Optional<? extends Column> getColumn(String name);
    List<? extends TableConstraint> getConstraints();

    boolean isAutoInstall();

    void doNotAutoInstall();

    TableConstraint getPrimaryKeyConstraint();
    List<? extends ForeignKeyConstraint> getForeignKeyConstraints();

    String getComponentName();

    List<? extends Column> getPrimaryKeyColumns();
    Table<T> map(Class<? extends T> implementer);

    Table<T> map(Map<String, Class<? extends T>> implementers);

    boolean maps(Class<?> implementer);

    void partitionOn(Column column);
    void autoPartitionOn(Column column, LifeCycleClass lifeCycleClass);

	void dropJournal(Instant upTo, Logger logger);

	void dropData(Instant upTo, Logger logger);
	LifeCycleClass lifeCycleClass();

}
