package com.elster.jupiter.util.sql;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class that assists in building sql statements.
 */
public final class SqlBuilder implements SqlFragment {

    private final StringBuilder builder;
    private final List<SqlFragment> fragments = new ArrayList<>();

    /**
     * Creates a new empty instance.
     */
    public SqlBuilder() {
        this.builder = new StringBuilder();
    }

    /**
     * Creates a new instance initialized with the given base.
     *
     * @param base : base sql string
     */
    public SqlBuilder(String base) {
        this.builder = new StringBuilder(base);
    }

    public void add(SqlFragment fragment) {
        builder.append(fragment.getText());
        fragments.add(fragment);
    }

    public void addObject(Object value) {
        add(new ObjectFragment(value));
    }

    public void addInt(int value) {
        add(new IntFragment(value));
    }

    public void addLong(long value) {
        add(new LongFragment(value));
    }

    public void addNull(int sqlType) {
        add(new NullFragment(sqlType));
    }

    public void addTimestamp(Instant date) {
        add(new TimestampFragment(date));
    }

    @Deprecated
    public void addTimestamp(java.util.Date date) {
    	addTimestamp(date == null ? null : date.toInstant());
    }
    
    public void addDate(Instant date) {
        add(new DateFragment(date));
    }
    
    @Deprecated
    public void addDate(java.util.Date date) {
    	addDate(date == null ? null : date.toInstant());
    }

    public StringBuilder getBuffer() {
        return builder;
    }

    public PreparedStatement prepare(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(builder.toString());
        boolean failed = true;
        try {
            bind(statement, 1);
            failed = false;
        } finally {
            if (failed) {
                statement.close();
            }
        }
        return statement;
    }

    public <T> Fetcher<T> fetcher(Connection connection, TupleParser<T> tupleParser) throws SQLException {
        PreparedStatement statement = prepare(connection);
        try {
            ResultSet resultSet = statement.executeQuery();
            return new FetcherImpl<>(resultSet, tupleParser);
        } catch (SQLException ex) {
            statement.close();
            throw ex;
        }
    }

    public void space() {
        builder.append(" ");
    }

    public void insertAt(int offset, String string) {
        builder.insert(offset, string);
    }

    public void append(String string) {
        builder.append(string);
    }

    public void spaceOpenBracket() {
        space();
        openBracket();
    }

    public void openBracket() {
        builder.append("(");
    }

    public void closeBracketSpace() {
        closeBracket();
        space();
    }

    public void closeBracket() {
        builder.append(")");
    }


    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    public int bind(PreparedStatement statement, int position) throws SQLException {
        int pos = position;
        for (SqlFragment each : fragments) {
            pos = each.bind(statement, pos);
        }
        return pos;
    }

    @Override
    public String getText() {
        return builder.toString();
    }

    public SqlBuilder asPageBuilder(int from, int to) {
        SqlBuilder result = new SqlBuilder("select * from (select x.*, ROWNUM rnum from (");
        result.add(this);
        result.append(") x where ROWNUM <= ");
        result.addInt(to);
        result.append(") where rnum >= ");
        result.addInt(from);
        return result;
    }

    public SqlBuilder asPageBuilder(String field, int from, int to) {
        SqlBuilder result = new SqlBuilder("select ");
        result.append(field);
        result.append(" from (select x.*, ROWNUM rnum from (");
        result.add(this);
        result.append(") x where ROWNUM <= ");
        result.addInt(to);
        result.append(") where rnum >= ");
        result.addInt(from);
        return result;
    }
    
    public SqlBuilder asTop(int n) {
    	SqlBuilder result = new SqlBuilder("select * from (");
        result.add(this);
        result.append(") where ROWNUM <= ");
        result.addInt(n);
        return result;
    }

    public boolean add(String field, Range<Instant> range, String lead) {
        boolean result = false;
        Objects.requireNonNull(range);
        if (range.hasLowerBound()) {
            space();
            append(lead);
            space();
            append(field);
            append(" >");
            if (range.lowerBoundType() == BoundType.CLOSED) {
                append("=");
            }
            addLong(range.lowerEndpoint().toEpochMilli());
            result = true;
        }
        if (range.hasUpperBound()) {
            space();
            append(result ? "AND" : lead);
            space();
            append(field);
            append(" <");
            if (range.upperBoundType() == BoundType.CLOSED) {
                append("=");
            }
            addLong(range.upperEndpoint().toEpochMilli());
            result = true;
        }
        return result;
    }

    private abstract static class SimpleFragment implements SqlFragment {
        SimpleFragment() {
        }

        @Override
        public final String getText() {
            return " ? ";
        }
    }

    private static class ObjectFragment extends SimpleFragment {
        private final Object value;

        ObjectFragment(Object value) {
            this.value = value;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            statement.setObject(position, value);
            return position + 1;

        }

    }

    private static class IntFragment extends SimpleFragment {
        private final int value;

        IntFragment(int value) {
            this.value = value;
        }


        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            statement.setInt(position, value);
            return position + 1;
        }

    }

    private static class LongFragment extends SimpleFragment {
        private final long value;

        LongFragment(long value) {
            this.value = value;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            statement.setLong(position, value);
            return position + 1;
        }
    }

    private static class NullFragment extends SimpleFragment {
        private final int sqlType;

        private NullFragment(int sqlType) {
            this.sqlType = sqlType;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            statement.setNull(position, this.sqlType);
            return position + 1;
        }
    }

    private static class TimestampFragment extends SimpleFragment {
        private final Instant instant;

        public TimestampFragment(Instant instant) {
            this.instant = instant;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            Timestamp ts = instant == null ? null : new Timestamp(instant.toEpochMilli());
            statement.setTimestamp(position, ts);
            return position + 1;
        }
    }

    private static class DateFragment extends SimpleFragment {
        private final Instant instant;

        public DateFragment(Instant instant) {
            this.instant = instant;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            Date sqlDate = instant == null ? null : new Date(instant.toEpochMilli());
            statement.setDate(position, sqlDate);
            return position + 1;
        }
    }
}
