package com.elster.jupiter.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @SuppressWarnings("unused")
    public void addNull(int sqlType) {
        add(new NullFragment(sqlType));
    }

    @SuppressWarnings("unused")
    public void addTimestamp(Date date) {
        add(new TimestampFragment(date));
    }

    @SuppressWarnings("unused")
    public void addDate(Date date) {
        add(new DateFragment(date));
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
        private final Date date;

        public TimestampFragment(Date date) {
            this.date = date;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            Timestamp ts = null;
            if (date != null) {
                if (date instanceof Timestamp) {
                    ts = (Timestamp) date;
                } else {
                    ts = new Timestamp(date.getTime());
                }
            }
            statement.setTimestamp(position, ts);
            return position + 1;
        }
    }

    private static class DateFragment extends SimpleFragment {
        private final Date date;

        public DateFragment(Date date) {
            this.date = date;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            java.sql.Date sqlDate;
            if (date instanceof java.sql.Date) {
                sqlDate = (java.sql.Date) date;
            } else {
                sqlDate = new java.sql.Date(date.getTime());
            }
            statement.setDate(position, sqlDate);
            return position + 1;
        }
    }
}
