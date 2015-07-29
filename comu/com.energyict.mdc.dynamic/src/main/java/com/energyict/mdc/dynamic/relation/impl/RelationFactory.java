package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;

import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides factory services for {@link Relation}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (17:42)
 */
public final class RelationFactory {

    private static final String OBSOLETEDATE_COLUMN_NAME = "obsoletedate";
    private static final String[] FIXED_COLUMNS = {
            "id",
            "fromdate",
            "todate",
            OBSOLETEDATE_COLUMN_NAME,
            "flags",
            "cre_date",
            "mod_date",
            "creuserid",
            "moduserid"
    };

    private final RelationTypeImpl relationType;
    private final Clock clock;
    private Logger sqlPerformanceLogger;

    public RelationFactory(RelationTypeImpl relationType, Clock clock) {
        super();
        this.relationType = relationType;
        this.sqlPerformanceLogger = Logger.getLogger("com.energyict.mdc.dynamic.relation.impl.RelationType." + relationType.getName());
        this.clock = clock;
    }

    public Relation create(RelationTransaction transaction) throws SQLException, BusinessException {
        RelationImpl relation = new RelationImpl(this.clock, this.relationType, this.getNewId());
        relation.init(transaction);
        this.postNew(relation);
        return relation;
    }

    private int getNewId() {
        try {
            try (PreparedStatement stmnt = newIdSqlBuilder().getStatement(this.getConnection())) {
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

    private SqlBuilder newIdSqlBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder("select ");
        sqlBuilder.append(this.getTableName());
        sqlBuilder.append("id");
        sqlBuilder.append(".nextval from dual");
        return sqlBuilder;
    }

    private void postNew(Relation relation) throws SQLException {
        this.insert(relation, this.getConnection());
    }

    private void insert(Relation relation, Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = this.insertSqlBuilder(relation).prepare(connection)) {
            int updateCount = preparedStatement.executeUpdate();
            if (updateCount != 1) {
                throw new SQLException("inserted zero rows");
            }
        }
    }

    private com.elster.jupiter.util.sql.SqlBuilder insertSqlBuilder(Relation relation) {
        return this.insertSqlBuilder(relation, this.getTableName(), true);
    }

    private com.elster.jupiter.util.sql.SqlBuilder insertSqlBuilder(Relation relation, String tableName, boolean ignoreObsoleteDate) {
        com.elster.jupiter.util.sql.SqlBuilder sqlBuilder = new com.elster.jupiter.util.sql.SqlBuilder("insert into ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" (");
        this.appendColumnsForInsert(sqlBuilder, ignoreObsoleteDate);
        sqlBuilder.append(") values (");
        this.appendValuesForInsert(sqlBuilder, relation, ignoreObsoleteDate);
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private void appendColumnsForInsert(com.elster.jupiter.util.sql.SqlBuilder builder, boolean ignoreObsoleteDate) {
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        List<String> columnNames = this.getColumnNames();
        for (String columnName : columnNames) {
            if (!ignoreObsoleteDate || !OBSOLETEDATE_COLUMN_NAME.equalsIgnoreCase(columnName)) {
                builder.append(separator.get());
                builder.append(columnName);
            }
        }
    }

    private void appendValuesForInsert(com.elster.jupiter.util.sql.SqlBuilder builder, Relation relation, boolean ignoreObsoleteDate) {
        List<String> columnNames = this.getColumnNames();
        if (ignoreObsoleteDate) {
            columnNames.remove(OBSOLETEDATE_COLUMN_NAME);
        }
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        for (String columnName : columnNames) {
            builder.append(separator.get());
            switch (columnName) {
                case "id": {
                    builder.addInt(relation.getId());
                    break;
                }
                case "fromdate": {
                    this.appendPeriodDateForInsert(builder, relation.getFrom());
                    break;
                }
                case "todate": {
                    this.appendPeriodDateForInsert(builder, relation.getTo());
                    break;
                }
                case "flags": {
                    builder.addInt(relation.getFlags());
                    break;
                }
                case OBSOLETEDATE_COLUMN_NAME: {
                    builder.addDate(relation.getObsoleteDate());
                    break;
                }
                case "cre_date":   // fall-through
                case "mod_date": {
                    builder.append("sysdate");
                    break;
                }
                case "creuserid":   // fall-through
                case "moduserid": {
                    builder.append("0");    // DCGroup user
                    break;
                }
                default: {
                    this.bindAttributeValue(builder, this.relationType.getAttributeType(columnName), relation.get(columnName));
                }
            }
        }
    }

    private void bindAttributeValue(com.elster.jupiter.util.sql.SqlBuilder builder, RelationAttributeType attributeType, Object value) {
        // To fix issue #3684 I had to add the second condition (+added the 3rd for completeness):
        if ((value == null)
                || (value instanceof Double && ((Double) value).isNaN())
                || (value instanceof Float && ((Float) value).isNaN())) {

            builder.addNull(attributeType.getJdbcType());
        }
        else {
            attributeType.getValueFactory().bind(builder, value);
        }
    }

    private void appendPeriodDateForInsert(com.elster.jupiter.util.sql.SqlBuilder builder, Instant date) {
        if (!this.relationType.hasTimeResolution()) {
            builder.addDate(date);
        }
        else {
            if (date == null) {
                builder.addNull(Types.BIGINT);
            }
            else {
                builder.addLong(date.getEpochSecond());
            }
        }
    }

    public void updateTo(Relation relation) throws SQLException {
        Instant newTo = relation.getTo();
        try (PreparedStatement statement = this.getUpdateToSqlBuilder(relation).getStatement(this.getConnection())) {
            int rows = statement.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Updated " + rows + " rows");
            }
        }
        if (newTo != null && newTo.equals(relation.getFrom())) {
            this.makeObsolete(relation);
        }
    }

    public SqlBuilder getUpdateToSqlBuilder(Relation relation) {
        Instant newTo = relation.getTo();
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(this.getTableName());
        sqlBuilder.append(" set todate = ? , mod_date = sysdate, moduserid = 0 where id = ?");
        if (this.relationType.hasTimeResolution()) {
            if (newTo == null) {
                sqlBuilder.bindLongToNull();
            }
            else {
                sqlBuilder.bindLong(newTo.getEpochSecond());
            }
        }
        else {
            sqlBuilder.bindDate(newTo);
        }
        sqlBuilder.bindInt(relation.getId());
        return sqlBuilder;
    }

    public void makeObsolete(Relation relation) throws SQLException {
        RelationType relType = this.relationType;
        com.elster.jupiter.util.sql.SqlBuilder insertBuilder = new com.elster.jupiter.util.sql.SqlBuilder("insert into ");
        insertBuilder.append(this.relationType.getObsoleteAttributeTableName());
        insertBuilder.append(" (");
        this.appendColumnsForInsert(insertBuilder, false);
        insertBuilder.append(") select id, fromdate, todate,");
        insertBuilder.addTimestamp(relation.getObsoleteDate());
        insertBuilder.append(", flags, cre_date, sysdate, creuserid, 0");

        for (RelationAttributeType each : this.relationType.getAttributeTypes()) {
            insertBuilder.append(", ");
            insertBuilder.append(each.getName());
        }
        insertBuilder.append(" from ");
        insertBuilder.append(relType.getDynamicAttributeTableName());
        insertBuilder.append(" where id = ");
        insertBuilder.addInt(relation.getId());
        try (PreparedStatement preparedStatement = insertBuilder.prepare(this.getConnection())) {
            preparedStatement.execute();
        }

        // delete the version
        SqlBuilder deleteBuilder = new SqlBuilder("delete from ");
        deleteBuilder.append(relType.getDynamicAttributeTableName());
        deleteBuilder.append(" where id = ?");
        deleteBuilder.bindInt(relation.getId());
        try (PreparedStatement preparedStatement = deleteBuilder.getStatement(this.getConnection())) {
            preparedStatement.execute();
        }
    }

    public void updateFlags (Relation relation, int flags) throws SQLException {
        try (PreparedStatement preparedStatement = this.getUpdateFlagsSqlBuilder(relation, flags).getStatement(this.getConnection())) {
            int updateCount = preparedStatement.executeUpdate();
            if (updateCount != 1) {
                throw new SQLException("updated zero rows");
            }
        }
    }

    private SqlBuilder getUpdateFlagsSqlBuilder (Relation relation, int flags) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(this.getTableName());
        sqlBuilder.append(" set flags = ? where id = ?");
        sqlBuilder.bindInt(flags);
        sqlBuilder.bindInt(relation.getId());
        return sqlBuilder;
    }

    public void delete (Relation relation) throws SQLException {
        try (PreparedStatement preparedStatement = this.deleteSqlBuilder(relation).getStatement(this.getConnection())) {
            preparedStatement.executeUpdate();
        }
    }

    private SqlBuilder deleteSqlBuilder(Relation relation) {
        SqlBuilder builder = new SqlBuilder("delete from ");
        builder.append(this.getTableName());
        builder.append(" where id = ?");
        builder.bindInt(relation.getId());
        return builder;
    }

    public Relation find(int id) {
        List<Relation> result = this.fetch(this.findByIdSqlBuilder(id));
        if (result.isEmpty()) {
            return null;
        }
        else {
            return result.get(0);
        }
    }

    private SqlBuilder findByIdSqlBuilder(int id) {
        SqlBuilder sqlBuilder = new SqlBuilder(this.selectClause());
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("id = ? ");
        sqlBuilder.bindInt(id);
        return sqlBuilder;
    }

    public List<Relation> findByFilter(RelationSearchFilter filter) {
        SqlBuilder builder = new SqlBuilder(selectClause());
        filter.appendWhereClause(builder);
        return this.fetch(builder);
    }

    public Relation findByPrimaryKey(Serializable key) {
        if (key instanceof RelationImpl.SerializebleRelationKey) {
            RelationImpl.SerializebleRelationKey tuple = (RelationImpl.SerializebleRelationKey) key;
            return this.relationType.findByPrimaryKey(tuple.getRelationId());
        }
        return this.find(((Integer) key).intValue());
    }

    public boolean hasAny() throws SQLException {
        SqlBuilder builder = new SqlBuilder("select * from ");
        builder.append(this.getTableName());
        builder.append(" where rownum = 1");
        try (PreparedStatement stmnt = builder.getStatement(this.getConnection())) {
            try (ResultSet rs = stmnt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Relation> findByRelationType() {
        List<Relation> result;
        try {
            try (PreparedStatement statement = this.findByRelationTypeSqlBuilder().getStatement(this.getConnection())) {
                result = this.fetch(statement);
            }
            for (Relation aResult : result) {
                RelationImpl each = (RelationImpl) aResult;
                each.doSetRelationType(relationType);
            }
            return result;
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private SqlBuilder findByRelationTypeSqlBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder(this.selectClause());
        sqlBuilder.append(" order by fromdate");
        return sqlBuilder;
    }

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType) {
        return this.findByParticipantAndAttributeType(participant, attribType, false);
    }

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType, boolean includeObsolete) {
        return this.findByParticipantAndAttributeType(participant, attribType, includeObsolete, null);
    }

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType, boolean includeObsolete, Instant date) {
        return this.findByParticipantAndAttributeType(participant, attribType, date, includeObsolete, 0, 0);
    }

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType, Instant when, boolean includeObsolete, int fromRow, int toRow) {
        List<Relation> result = new ArrayList<>();
        if (this.relationType.isActive()) {
            SqlBuilder builder = this.findByParticipantAndAttributeTypeSqlBuilder(includeObsolete, participant, attribType, when);
            result = this.fetch(builder.asPageSqlBuilder(fromRow, toRow));
            for (Relation aResult : result) {
                RelationImpl each = (RelationImpl) aResult;
                each.doSetRelationType(this.relationType);
            }
        }
        return result;
    }

    private SqlBuilder findByParticipantAndAttributeTypeSqlBuilder(boolean includeObsolete, RelationParticipant participant, RelationAttributeType attribType, Instant when) {
        SqlBuilder builder;
        if (includeObsolete) {
            builder = new SqlBuilder(this.selectUnionClause());
        }
        else {
            builder = new SqlBuilder(this.selectClause());
        }
        builder.appendWhereOrAnd();
        builder.append(attribType.getName());
        builder.append(" = ? ");
        this.bindParticipantIdentifier(participant, builder);
        if (when != null) {
            builder.append(" and fromdate <= ? and (todate is null or todate > ?) ");
            if (this.relationType.hasTimeResolution()) {
                builder.bindUtc(when);
                builder.bindUtc(when);
            }
            else {
                builder.bindTimestamp(when);
                builder.bindTimestamp(when);
            }
        }
        return builder;
    }

    private void bindParticipantIdentifier(RelationParticipant participant, SqlBuilder builder) {
        if (participant instanceof IdBusinessObject) {
            builder.bindInt(((IdBusinessObject) participant).getId());
        }
        else {
            builder.bindLong(((HasId) participant).getId());
        }
    }

    /**
     * Finds {@link Relation}s that are affected by the execution of a {@link RelationTransaction}.
     *
     * @param transaction The RelationTransaction
     * @param constraints The Constraint
     * @return The List of Relation
     */
    public List<Relation> affectedBy(RelationTransaction transaction, List<Constraint> constraints) {
        SqlBuilder builder = new SqlBuilder(selectClause());
        builder.appendWhereOrAnd();
        if (transaction.getTo() != null) {
            builder.append("fromdate <= ? ");
            builder.appendWhereOrAnd();
            if (relationType.hasTimeResolution()) {
                builder.bindUtc(transaction.getTo());
            }
            else {
                builder.bindTimestamp(transaction.getTo());
            }
        }
        builder.append("(todate is null or todate >= ?) ");
        if (relationType.hasTimeResolution()) {
            builder.bindUtc(transaction.getFrom());
        }
        else {
            builder.bindTimestamp(transaction.getFrom());
        }
        builder.append(" and (");
        boolean first = true;
        for (Constraint constraint : constraints) {
            SqlBuilder constraintBuilder = new SqlBuilder();
            if (constraint.appendAttributeSql(constraintBuilder, transaction)) {
                if (!first) {
                    builder.append(" or ");
                }
                builder.append(constraintBuilder);
                first = false;
            }
        }
        if (first) { // nothing added, return empty list
            return Collections.emptyList();
        }
        builder.append(") order by fromdate");

        List<Relation> result = fetch(builder);
        for (Relation relation : result) {
            RelationImpl each = (RelationImpl) relation;
            each.doSetRelationType(this.relationType);
        }
        return result;
    }

    private String selectClause() {
        StringBuilder builder = new StringBuilder("select ");
        appendBasicSelectClause(builder, this.getTableName() + ".");
        builder.append(" from ");
        builder.append(this.getTableName());
        return builder.toString();
    }

    private void appendBasicSelectClause(StringBuilder builder, String columnPrefix) {
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        List<String> columnNames = this.getColumnNames();
        for (String columnName : columnNames) {
            builder.append(separator.get());
            if (columnName.equalsIgnoreCase(OBSOLETEDATE_COLUMN_NAME)) {
                builder.append("null ");
                builder.append(columnName);
            }
            else {
                builder.append(columnPrefix);
                builder.append(columnName);
            }
        }
    }

    private String selectUnionClause() {
        StringBuilder builder = new StringBuilder("select ");
        List<String> columnNames = this.getColumnNames();
        String tableNameDot = "r.";
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        for (String columnName : columnNames) {
            builder.append(separator.get());
            builder.append(tableNameDot);
            builder.append(columnName);
        }
        builder.append(" from ");
        this.unionSql(builder);
        builder.append(" r");
        return builder.toString();
    }

    private void unionSql(StringBuilder builder) {
        builder.append("( select ");
        this.appendBasicSelectClause(builder, "");
        builder.append(" from ");
        builder.append(this.relationType.getDynamicAttributeTableName());
        builder.append(" union all ");
        builder.append("select ");
        this.appendBasicSelectClause(builder, "");
        builder.append(" from ");
        builder.append(this.relationType.getObsoleteAttributeTableName());
        builder.append(")");
    }

    private String getTableName() {
        return this.relationType.getDynamicAttributeTableName();
    }

    private List<String> getColumnNames() {
        List<RelationAttributeType> attribs = this.relationType.getAttributeTypes();
        List<String> columns = new ArrayList<>(Arrays.asList(FIXED_COLUMNS));
        for (RelationAttributeType rat : attribs) {
            columns.add(rat.getName());
        }
        return columns;
    }

    private List<Relation> fetch(SqlBuilder builder) {
        long start = System.currentTimeMillis();
        try {
            try (PreparedStatement stmnt = builder.getStatement(this.getConnection())) {
                return this.fetch(stmnt);
            }
            finally {
                if (sqlPerformanceLogger.isLoggable(Level.FINE)) {
                    sqlPerformanceLogger.fine("Fetched " + builder.expandedText() + " in " + (System.currentTimeMillis() - start) + " ms");
                }
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private List<Relation> fetch(PreparedStatement preparedStatement) {
        long start = System.currentTimeMillis();
        try {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return this.fetch(resultSet);
            }
            finally {
                if (sqlPerformanceLogger.isLoggable(Level.FINE)) {
                    sqlPerformanceLogger.fine("Fetched " + preparedStatement.toString() + " in " + (System.currentTimeMillis() - start) + " ms");
                }
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private List<Relation> fetch(ResultSet resultSet) throws SQLException {
        List<Relation> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(new RelationImpl(this.clock, this.relationType, resultSet));
        }
        return result;
    }

    private Connection getConnection() {
        return this.relationType.getConnection();
    }

}