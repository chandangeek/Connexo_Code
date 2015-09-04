package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.coordinates.SpatialCoordinates;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.exceptions.RelationTypeDDLException;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RelationTypeDdlGenerator {

    private final DataModel dataModel;
    private Thesaurus thesaurus;
    private RelationType relationType;
    private Map<RelationAttributeType, String> names = new HashMap<>();
    private boolean dontCheckExistence = true;
    private Logger logger = Logger.getLogger(RelationTypeDdlGenerator.class.getName());

    public RelationTypeDdlGenerator(DataModel dataModel, RelationType relationType, Thesaurus thesaurus, boolean checkExistence) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.relationType = relationType;
        this.dontCheckExistence = !checkExistence;
    }

    public void execute() throws SQLException {
        generateNames();
        createDynamicAttributeTable();
        createObsoleteAttributeTable();
        createDynamicAttributeSequence();
        addMetaData();
        createAttributeIndexes();
    }

    protected void addMetaData() throws SQLException {
        for (RelationAttributeType each : getAttributeTypes()) {
            if (each.getDbType().equals(SpatialCoordinates.SQL_TYPE_NAME)) {
                addMetaData(each);
            }
        }
    }

    protected void addMetaData(RelationAttributeType each) throws SQLException {
        this.addMetaData(each, this.getDynamicAttributeTableName());
        this.addMetaData(each, this.getObsoleteAttributeTableName());
    }

    protected void addMetaData(RelationAttributeType each, String tableName) throws SQLException {
        StringBuilder builder = new StringBuilder();
        if (tableExists(tableName)) {
            builder.append("INSERT INTO user_sdo_geom_metadata VALUES ('");
            builder.append(tableName).append("', '").append(each.getName()).append("', ");
            builder.append("MDSYS.SDO_DIM_ARRAY (");
            builder.append("SDO_DIM_ELEMENT ('LONGITUDE',-180,180,1),");
            builder.append("SDO_DIM_ELEMENT ('LATITUDE',-90,90,1)),8307)");
            logger.info(builder.toString());
            executeDdl(builder.toString());
        }
    }

    protected void executeDelete(RelationAttributeType attributeType) {
        RelationType type = attributeType.getRelationType();
        logger.info("Start removing " + attributeType.getName() + " of " + type.getName());
        try {
            dropAttributeColumn(attributeType);
        }
        catch (SQLException e) {
            throw new RelationTypeDDLException(e, this.relationType.getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }


    protected List<RelationAttributeType> getAttributeTypes() {
        return relationType.getAttributeTypes();
    }

    protected String getDynamicAttributeTableName() {
        return relationType.getDynamicAttributeTableName();
    }

    protected String getObsoleteAttributeTableName() {
        return relationType.getObsoleteAttributeTableName();
    }

    protected String getDynamicAttributeSequenceName() {
        return ((RelationTypeImpl) relationType).getDynamicAttributeSequenceName();
    }

    protected void createDynamicAttributeTable() throws SQLException {
        if (dontCheckExistence || !tableExists(getDynamicAttributeTableName())) {
            executeDdl(getCreateDynamicAttributeTableSql());
            logger.info("Created table " + getDynamicAttributeTableName());
        }
    }

    protected void createObsoleteAttributeTable() throws SQLException {
        if (dontCheckExistence || !tableExists(getObsoleteAttributeTableName())) {
            executeDdl(getCreateObsoleteAttributeTableSql());
            logger.info("Created table " + getObsoleteAttributeTableName());
        }
    }

    protected void createAttributeIndexes() throws SQLException {
        for (RelationAttributeType mdwAttributeType : getAttributeTypes()) {
            createAttributeIndex(mdwAttributeType);
        }
    }

    private void createAttributeIndex(RelationAttributeType attType) throws SQLException {
        if (attType.requiresIndex()) {
            String dynAttIdx = getDynamicAttributeIndexName(attType);
            String obsAttIdx = getObsoleteAttributeIndexName(attType);
            if (tableExists(getDynamicAttributeTableName()) && !indexExists(getDynamicAttributeTableName(), attType.getName())) {
                executeDdl(getCreateDynamicAttributeIndexSql(attType));
                this.logCreatedIndex(dynAttIdx);
            }
            if (tableExists(getObsoleteAttributeTableName()) && !indexExists(getObsoleteAttributeTableName(), attType.getName())) {
                executeDdl(getCreateObsoleteAttributeIndexSql(attType));
                this.logCreatedIndex(obsAttIdx);
            }
        }
    }

    private void logCreatedIndex(String indexName) {
        logger.info("Created index " + indexName);
    }

    protected void dropAttributeIndexes(RelationAttributeType attributeType) throws SQLException {
        if (attributeType.requiresIndex()) {
            String dynAttIdx = getDynamicAttributeIndexName(attributeType);
            String obsAttIdx = getObsoleteAttributeIndexName(attributeType);
            if (indexExists(getDynamicAttributeTableName(), attributeType.getName())) {
                executeDdl(getDropDynamicAttributeIndexSql(attributeType));
                this.logIndexDropped(dynAttIdx);
            }
            if (indexExists(getObsoleteAttributeTableName(), attributeType.getName())) {
                executeDdl(getDropObsoleteAttributeIndexSql(attributeType));
                this.logCreatedIndex(obsAttIdx);
            }
        }
    }

    private void logIndexDropped(String indexName) {
        logger.info("Dropped index " + indexName);
    }

    protected void createDynamicAttributeSequence() throws SQLException {
        if (!sequenceExists(getDynamicAttributeSequenceName())) {
            executeDdl(getCreateDynamicAttributeSequenceSql());
            logger.info("created sequence " + getDynamicAttributeSequenceName());
        }
    }

    protected String getDropDynamicAttributeIndexSql(RelationAttributeType attributeType) {
        return "drop index " + getDynamicAttributeIndexName(attributeType);
    }

    protected String getCreateDynamicAttributeIndexSql(RelationAttributeType attributeType) {
        return this.getCreateAttributeIndexSql(attributeType, this.getDynamicAttributeTableName(), this.getDynamicAttributeIndexName(attributeType));
    }

    protected String getCreateObsoleteAttributeIndexSql(RelationAttributeType attributeType) {
        return this.getCreateAttributeIndexSql(attributeType, this.getObsoleteAttributeTableName(), this.getObsoleteAttributeIndexName(attributeType));
    }

    protected String getCreateAttributeIndexSql(RelationAttributeType attributeType, String tableName, String indexName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("create index ");
        buffer.append(indexName);
        buffer.append(" on ");
        buffer.append(tableName);
        buffer.append("(");
        buffer.append(attributeType.getName());
        buffer.append(")");
        String indexType = attributeType.getIndexType();
        if (!Checks.is(indexType).emptyOrOnlyWhiteSpace()) {
            buffer.append(" indextype is ").append(indexType);
        }
        logger.info(buffer.toString());
        return buffer.toString();
    }

    protected String getDropObsoleteAttributeIndexSql(RelationAttributeType attributeType) {
        return "drop index " + getObsoleteAttributeIndexName(attributeType);
    }

    protected String getCreateDynamicAttributeTableSql() {
        return this.getCreateAttributeTableSql(this.getDynamicAttributeTableName(), true);
    }

    protected String getCreateObsoleteAttributeTableSql() {
        return this.getCreateAttributeTableSql(this.getObsoleteAttributeTableName(), false);
    }

    protected String getCreateAttributeTableSql(String tableName, boolean checkRequired) {
        StringBuilder buffer = new StringBuilder("create table ");
        buffer.append(tableName);
        buffer.append(" ( id number(10) ");
        String timeType;
        if (this.relationType.hasTimeResolution()) {
            timeType = "integer";
        }
        else {
            timeType = "date";
        }
        buffer.append(", fromdate ");
        buffer.append(timeType);
        buffer.append(" not null ");
        buffer.append(", todate ");
        buffer.append(timeType);
        buffer.append(", obsoletedate date, cre_date date default sysdate not null, mod_date date default sysdate not null, creuserid number(10) not null, moduserid number(10) not null, flags number(10) ");
        for (RelationAttributeType each : getAttributeTypes()) {
            buffer.append(" , ");
            buffer.append(each.getName());
            buffer.append(" ");
            buffer.append(each.getDbType());
            if (checkRequired && each.isRequired()) {
                buffer.append(" not null ");
            }
        }
        buffer.append(" , constraint pk_");
        buffer.append(tableName);
        buffer.append(" primary key (id) ");
        buffer.append(")");
        return buffer.toString();
    }

    public void addAttributeColumn(RelationAttributeType attributeType) {
        if (!names.containsKey(attributeType)) {
            generateNames();
        }
        try {
            if (tableExists(getDynamicAttributeTableName()) && !columnExists(getDynamicAttributeTableName(), attributeType.getName())) {
                executeDdl(getAddColumnSql(getDynamicAttributeTableName(), attributeType.getName(), attributeType.getDbType(), attributeType.isRequired()));
            }
            if (tableExists(getObsoleteAttributeTableName()) && !columnExists(getObsoleteAttributeTableName(), attributeType.getName())) {
                executeDdl(getAddColumnSql(getObsoleteAttributeTableName(), attributeType.getName(), attributeType.getDbType(), false));
            }
            if (attributeType.getDbType().equals(SpatialCoordinates.SQL_TYPE_NAME)) {
                addMetaData(attributeType);
            }
            createAttributeIndex(attributeType);
        }
        catch (SQLException e) {
            throw new RelationTypeDDLException(e, this.relationType.getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }

    public void dropAttributeColumn(RelationAttributeType attributeType) throws SQLException {
        if (!names.containsKey(attributeType)) {
            generateNames();
        }
        if (attributeType.getDbType().equals(SpatialCoordinates.SQL_TYPE_NAME)) {
            dropMetaData(attributeType);
        }
        dropAttributeIndexes(attributeType);
        if (columnExists(getObsoleteAttributeTableName(), attributeType.getName())) {
            executeDdl(getDropColumnSql(getObsoleteAttributeTableName(), attributeType.getName()));
        }
        if (columnExists(getDynamicAttributeTableName(), attributeType.getName())) {
            executeDdl(getDropColumnSql(getDynamicAttributeTableName(), attributeType.getName()));
        }
    }

    public void alterAttributeColumnRequired(RelationAttributeType attributeType, boolean isRequired) {
        try {
            if (columnExists(getObsoleteAttributeTableName(), attributeType.getName())) {
                // Remark: from now on the ORU-tables are always created with nullable columns; required attribute or not.
                // The following instructions are kept to make the columns nullable in case they aren't (due to legacy creation)
                if (!columnIsNullable(getObsoleteAttributeTableName(), attributeType.getName())) {
                    executeDdl(getAlterAttributeColumnRequired(getObsoleteAttributeTableName(),
                            attributeType.getName(), false));
                }
            }
            if (columnExists(getDynamicAttributeTableName(), attributeType.getName())) {
                if (isRequired == columnIsNullable(getDynamicAttributeTableName(), attributeType.getName())) {
                    executeDdl(getAlterAttributeColumnRequired(getDynamicAttributeTableName(),
                            attributeType.getName(), isRequired));
                }
            }
        }
        catch (SQLException e) {
            throw new RelationTypeDDLException(e, this.relationType.getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }

    private String getAlterAttributeColumnRequired(String tableName, String fieldName, boolean isRequired) {
        return "ALTER TABLE " + tableName + " MODIFY " + fieldName + this.isRequiredSqlClause(isRequired);
    }

    private String getAddColumnSql(String tableName, String fieldName, String dbType, boolean isRequired) {
        return "ALTER TABLE  " + tableName + " ADD " + fieldName + " " + dbType + this.isRequiredSqlClause(isRequired);
    }

    private String isRequiredSqlClause(boolean isRequired) {
        if (isRequired) {
            return " NOT NULL";
        }
        else {
            return " NULL";
        }
    }

    private String getDropColumnSql(String tableName, String fieldName) {
        return "ALTER TABLE  " + tableName + " DROP COLUMN " + fieldName;
    }

    protected String getCreateDynamicAttributeSequenceSql() {
        return "create sequence " + getDynamicAttributeSequenceName() + " start with " + (getMaxId() + 1);
    }

    protected void generateNames() {
        Map<String, Integer> foreignKeyNames = new HashMap<>();
        for (RelationAttributeType mdwAttributeType : getAttributeTypes()) {
            RelationAttributeTypeImpl each = (RelationAttributeTypeImpl) mdwAttributeType;
            if (each.requiresIndex()) {
                String name = relationType.getName() + "_" + each.getName();
                if (name.length() > 24) {
                    name = name.substring(0, 21);
                    Integer count = foreignKeyNames.get(name.toUpperCase());
                    if (count == null) {
                        count = 0;
                    }
                    foreignKeyNames.put(name.toUpperCase(), count + 1);
                    name = getForeignKeyName(name, count);
                }
                names.put(each, name);
            }
        }
    }

    protected String getForeignKeyName(String baseName, Integer count) {
        StringBuilder buffer = new StringBuilder(baseName);
        for (int i = count.toString().length(); i < 3; i++) {
            buffer.append("0");
        }
        buffer.append(count);
        return buffer.toString();
    }

    protected String getDynamicAttributeIndexName(RelationAttributeType attributeType) {
        String tableName = getDynamicAttributeTableName();
        String prefix = tableName.substring(0, 3);
        return "IX_" + prefix + names.get(attributeType);
    }

    protected String getObsoleteAttributeIndexName(RelationAttributeType attributeType) {
        String tableName = getObsoleteAttributeTableName();
        String prefix = tableName.substring(0, 3);
        return "IX_" + prefix + names.get(attributeType);
    }

    protected boolean indexExists(String tableName, String col) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from USER_IND_COLUMNS ");
        builder.append("where upper(TABLE_NAME) = upper(?) and upper(COLUMN_NAME) = upper(?) ");
        builder.append("and COLUMN_POSITION = 1");
        builder.bindString(tableName);
        builder.bindString(col);
        return hasRows(builder);
    }

    protected boolean tableExists(String name) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from USER_TABLES where upper(TABLE_NAME) = upper(?)");
        builder.bindString(name);
        return hasRows(builder);
    }

    protected boolean columnExists(String tableName, String col) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from USER_TAB_COLUMNS where upper(TABLE_NAME) = upper(?) and upper(COLUMN_NAME) = upper(?)");
        builder.bindString(tableName);
        builder.bindString(col);
        return hasRows(builder);
    }

    protected boolean sequenceExists(String name) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from USER_SEQUENCES where upper(SEQUENCE_NAME) = upper(?)");
        builder.bindString(name);
        return hasRows(builder);
    }

    protected boolean columnIsNullable(String tableName, String col) throws SQLException {
        SqlBuilder builder = new SqlBuilder("select nullable from USER_TAB_COLUMNS where upper(TABLE_NAME) = upper(?) and upper(COLUMN_NAME) = upper(?)");
        builder.bindString(tableName);
        builder.bindString(col);
        return isNullable(builder);
    }

    protected boolean hasRows(SqlBuilder builder) throws SQLException {
        try (PreparedStatement statement = builder.getStatement(getConnection())) {
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    protected boolean isNullable(SqlBuilder builder) throws SQLException {
        try (PreparedStatement statement = builder.getStatement(getConnection())) {
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && "Y".equals(rs.getString(1));
            }
        }
    }

    protected int getMaxId() {
        return Math.max(getMaxDynamicAttributeId(), getMaxObsoleteAttributeId());
    }

    protected int getMaxDynamicAttributeId() {
        try {
            SqlBuilder builder = new SqlBuilder("select nvl(max(id),0) from " + getDynamicAttributeTableName());
            try (PreparedStatement stmnt = builder.getStatement(getConnection())) {
                try (ResultSet resultSet = stmnt.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    protected int getMaxObsoleteAttributeId() {
        try {
            if (tableExists(getObsoleteAttributeTableName())) {
                SqlBuilder builder = new SqlBuilder("select nvl(max(id),0) from " + getObsoleteAttributeTableName());
                try (PreparedStatement stmnt = builder.getStatement(getConnection())) {
                    try (ResultSet resultSet = stmnt.executeQuery()) {
                        resultSet.next();
                        return resultSet.getInt(1);
                    }
                }
            }
            return 0;
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    protected void executeDdl(String sqlStatement) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sqlStatement);
        }
    }

    protected Connection getConnection() throws SQLException {
        return this.dataModel.getConnection(true);
    }

    public void dropMetaData() throws SQLException {
        for (RelationAttributeType each : getAttributeTypes()) {
            if (each.getValueType().equals(SpatialCoordinates.class)) {
                dropMetaData(each);
            }
        }
    }

    protected void dropMetaData(RelationAttributeType each) throws SQLException {
        StringBuffer buffer;
        if (tableExists(getDynamicAttributeTableName())) {
            buffer = new StringBuffer("delete from mdsys.user_sdo_geom_metadata where table_name = '");
            buffer.append(getDynamicAttributeTableName().toUpperCase()).append("'");
            buffer.append(" and column_name = '").append(each.getName().toUpperCase()).append("'");
            logger.info(buffer.toString());
            executeDdl(buffer.toString());
        }

        if (tableExists(getObsoleteAttributeTableName())) {
            buffer = new StringBuffer("delete from mdsys.user_sdo_geom_metadata where table_name = '");
            buffer.append(getObsoleteAttributeTableName().toUpperCase()).append("'");
            buffer.append(" and column_name = '").append(each.getName().toUpperCase()).append("'");
            logger.info(buffer.toString());
            executeDdl(buffer.toString());
        }
    }

}