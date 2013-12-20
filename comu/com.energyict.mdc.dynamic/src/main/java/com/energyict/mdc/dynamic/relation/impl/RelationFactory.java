package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.PrimaryKeyExternalRepresentationConvertor;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import org.joda.time.DateTimeConstants;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

    private static final String[] FIXED_COLUMNS = {
            "id",
            "fromdate",
            "todate",
            "obsoletedate",
            "flags",
            "cre_date",
            "mod_date",
            "creuserid",
            "moduserid"
    };

    private RelationType relationType;
    private Logger sql_perf_logger;

    public RelationFactory(RelationType relationType) {
        super();
        this.relationType = relationType;
        this.sql_perf_logger = Logger.getLogger("com.energyict.mdc.dynamic.relation.impl.RelationType." + relationType.getName());
    }

    public Relation create(RelationTransaction transaction) throws SQLException, BusinessException {
        RelationImpl relation = new RelationImpl(this.relationType, this.getNewId());
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
        try (PreparedStatement preparedStatement = this.insertSqlBuilder(relation).getStatement(connection)) {
            int updateCount = preparedStatement.executeUpdate();
            if (updateCount != 1) {
                throw new SQLException("inserted zero rows");
            }
        }
    }

    private SqlBuilder insertSqlBuilder(Relation relation) {
        return this.insertSqlBuilder(relation, this.getTableName(), true);
    }

    private SqlBuilder insertSqlBuilder(Relation relation, String tableName, boolean ignoreObsoleteDate) {
        SqlBuilder sqlBuilder = new SqlBuilder("insert into ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" (");
        this.appendColumnsForInsert(sqlBuilder, ignoreObsoleteDate);
        sqlBuilder.append(") values (");
        this.appendValuesForInsert(sqlBuilder, relation, ignoreObsoleteDate);
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private void appendColumnsForInsert(SqlBuilder builder, boolean ignoreObsoleteDate) {
        List<String> columnNames = this.getColumnNames();
        ListAppendMode appendMode = ListAppendMode.FIRST;
        for (String columnName : columnNames) {
            if (!ignoreObsoleteDate || !columnName.equalsIgnoreCase("obsoletedate")) {
                appendMode.startOn(builder);
                builder.append(columnName);
                appendMode = ListAppendMode.REMAINING;
            }
        }
    }

    private void appendValuesForInsert(SqlBuilder builder, Relation relation, boolean ignoreObsoleteDate) {
        List<String> columnNames = this.getColumnNames();
        if (ignoreObsoleteDate) {
            columnNames.remove("obsoletedate");
        }
        ListAppendMode appendMode = ListAppendMode.FIRST;
        for (String columnName : columnNames) {
            appendMode.startOn(builder);
            switch (columnName) {
                case "id": {
                    builder.append("?");
                    builder.bindInt(relation.getId());
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
                    builder.append("?");
                    builder.bindInt(relation.getFlags());
                    break;
                }
                case "obsoletedate": {
                    builder.bindDate(relation.getObsoleteDate());
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
                    builder.append("?");
                    this.bindAttributeValue(builder, this.relationType.getAttributeType(columnName), relation.get(columnName));
                }
            }
            appendMode = ListAppendMode.REMAINING;
        }
    }

    private void bindAttributeValue(SqlBuilder builder, RelationAttributeType attributeType, Object value) {
        // To fix issue #3684 I had to add the second condition (+added the 3rd for completeness):
        if ((value == null)
                || (value instanceof Double && ((Double) value).isNaN())
                || (value instanceof Float && ((Float) value).isNaN())) {
            String structType = attributeType.getStructType();
            if (structType == null) {
                builder.bindNull(attributeType.getJdbcType());
            }
            else {
                builder.bindNull(attributeType.getJdbcType(), structType);
            }
        }
        else {
            attributeType.getValueFactory().bind(builder, value);
        }
    }

    private void appendPeriodDateForInsert(SqlBuilder builder, Date date) {
        builder.append("?");
        if (!this.relationType.hasTimeResolution()) {
            builder.bindDate(asDate(date));
        }
        else {
            if (date == null) {
                builder.bindLongToNull();
            }
            else {
                builder.bindLong(asSeconds(date));
            }
        }
    }

    private java.sql.Date asDate(java.util.Date in) {
        if (in == null) {
            return null;
        }
        else {
            return new java.sql.Date(in.getTime());
        }
    }

    private long asSeconds(Date date) {
        if (date == null) {
            return 0;
        }
        else {
            return date.getTime() / DateTimeConstants.MILLIS_PER_SECOND;
        }
    }

    public void updateTo(Relation relation) throws SQLException {
        Date newTo = relation.getTo();
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
        Date newTo = relation.getTo();
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(this.getTableName());
        sqlBuilder.append(" set todate = ? , mod_date = sysdate, moduserid = 0 where id = ?");
        if (this.relationType.hasTimeResolution()) {
            if (newTo == null) {
                sqlBuilder.bindLongToNull();
            }
            else {
                sqlBuilder.bindLong(asSeconds(newTo));
            }
        }
        else {
            sqlBuilder.bindDate(asDate(newTo));
        }
        sqlBuilder.bindInt(relation.getId());
        return sqlBuilder;
    }

    public void makeObsolete(Relation relation) throws SQLException {
        RelationType relType = this.relationType;
        SqlBuilder insertBuilder = new SqlBuilder("insert into ");
        insertBuilder.append(this.relationType.getObsoleteAttributeTableName());
        insertBuilder.append(" (");
        this.appendColumnsForInsert(insertBuilder, false);
        insertBuilder.append(") select id, fromdate, todate, ?, flags, cre_date, sysdate, creuserid, 0");
        insertBuilder.bindTimestamp(relation.getObsoleteDate());
        for (RelationAttributeType each : this.relationType.getAttributeTypes()) {
            insertBuilder.append(", ");
            insertBuilder.append(each.getName());
        }
        insertBuilder.append(" from ");
        insertBuilder.append(relType.getDynamicAttributeTableName());
        insertBuilder.append(" where id = ?");
        insertBuilder.bindInt(relation.getId());
        try (PreparedStatement preparedStatement = insertBuilder.getStatement(this.getConnection())) {
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
        builder.append("where id = ?");
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

    public Relation findByHandle(byte[] handle) {
        return this.find(PrimaryKeyExternalRepresentationConvertor.intFromBytes(handle));
    }

    public List<Relation> findModifiedSince(Date since) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(selectClause());
        builder.appendWhereOrAnd();
        builder.append(" mod_date >= ?");
        builder.bindTimestamp(since);
        List<Relation> modifiedSince = fetch(builder);
        for (Relation aResult : modifiedSince) {
            RelationImpl each = (RelationImpl) aResult;
            each.doSetRelationType(this.relationType);
        }
        return modifiedSince;
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

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType, boolean includeObsolete, Date date) {
        return this.findByParticipantAndAttributeType(participant, attribType, date, includeObsolete, 0, 0);
    }

    public List<Relation> findByParticipantAndAttributeType(RelationParticipant participant, RelationAttributeType attribType, Date date, boolean includeObsolete, int fromRow, int toRow) {
        List<Relation> result = new ArrayList<>();
        if (this.relationType.isActive()) {
            SqlBuilder builder = this.findByParticipantAndAttributeTypeSqlBuilder(includeObsolete, (IdBusinessObject) participant, attribType, date);
            result = this.fetch(builder.asPageSqlBuilder(fromRow, toRow));
            for (Relation aResult : result) {
                RelationImpl each = (RelationImpl) aResult;
                each.doSetRelationType(this.relationType);
            }
        }
        return result;
    }

    private SqlBuilder findByParticipantAndAttributeTypeSqlBuilder(boolean includeObsolete, IdBusinessObject participant, RelationAttributeType attribType, Date date) {
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
        builder.bindInt(participant.getId());
        if (date != null) {
            builder.append(" and fromdate <= ? and (todate is null or todate > ?) ");
            if (this.relationType.hasTimeResolution()) {
                builder.bindUtc(date);
                builder.bindUtc(date);
            }
            else {
                builder.bindTimestamp(date);
                builder.bindTimestamp(date);
            }
        }
        return builder;
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
        List<String> columnNames = this.getColumnNames();
        ListAppendMode appendMode = ListAppendMode.FIRST;
        for (String columnName : columnNames) {
            appendMode.startOn(builder);
            if (columnName.equalsIgnoreCase("obsoletedate")) {
                builder.append("null ");
                builder.append(columnName);
            }
            else {
                builder.append(columnPrefix);
                builder.append(columnName);
            }
            appendMode = ListAppendMode.REMAINING;
        }
    }

    private String selectUnionClause() {
        StringBuilder builder = new StringBuilder("select ");
        List<String> columnNames = this.getColumnNames();
        String tableNameDot = "r.";
        ListAppendMode appendMode = ListAppendMode.FIRST;
        for (String columnName : columnNames) {
            appendMode.startOn(builder);
            builder.append(tableNameDot);
            builder.append(columnName);
            appendMode = ListAppendMode.REMAINING;
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
                if (sql_perf_logger.isLoggable(Level.FINE)) {
                    sql_perf_logger.fine("Fetched " + builder.expandedText() + " in " + (System.currentTimeMillis() - start) + " ms");
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
                if (sql_perf_logger.isLoggable(Level.FINE)) {
                    sql_perf_logger.fine("Fetched " + preparedStatement.toString() + " in " + (System.currentTimeMillis() - start) + " ms");
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
            result.add(new RelationImpl(this.relationType, resultSet));
        }
        return result;
    }

    private Connection getConnection() {
        return Environment.DEFAULT.get().getConnection();
    }

}