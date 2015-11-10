package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.search.sqlbuilder.DeviceSearchSqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link Finder} interface
 * for {@link Device}s that is backed by a custom built sql query.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-29 (09:51)
 */
public class DeviceFinder implements Finder<Device> {

    private static Logger LOGGER = Logger.getLogger(DeviceFinder.class.getName());

    private final DataModel dataModel;
    private final DeviceSearchSqlBuilder sqlBuilder;
    private Pager pager = new NoPaging();
    private List<Order> orders;

    public DeviceFinder(DeviceSearchSqlBuilder sqlBuilder, DataModel dataModel) {
        super();
        this.sqlBuilder = sqlBuilder;
        this.dataModel = dataModel;
        this.orders = new ArrayList<>();
        this.orders.add(Order.ascending("mRID"));
    }

    @Override
    public List<Device> find() {
        SqlBuilder sqlBuilder = this.sqlBuilder.toSqlBuilder();
        sqlBuilder.append(" ORDER BY " + this.orders.stream()
                .map(order -> order.getClause(order.getName()))
                .collect(Collectors.joining(", ")));
        sqlBuilder = this.pager.addPaging(sqlBuilder);
        try (Fetcher<Device> fetcher = this.dataModel.mapper(Device.class).fetcher(sqlBuilder)) {
            List<Device> matchingDevices = new ArrayList<>();
            Iterator<Device> deviceIterator = fetcher.iterator();
            while (deviceIterator.hasNext()) {
                matchingDevices.add(deviceIterator.next());
            }
            return matchingDevices;
        }
    }

    @Override
    public Finder<Device> paged(int from, int pageSize) {
        this.pager = new WithPaging(from, from + pageSize);
        return this;
    }

    @Override
    public Finder<Device> sorted(String s, boolean b) {
        orders.add(b ? Order.ascending(s) : Order.descending(s));
        return this;
    }

    @Override
    public Subquery asSubQuery(String... strings) {
        return () -> asFragment(strings);
    }

    @Override
    public SqlBuilder asFragment(String... strings) {
        SqlBuilder sqlBuilder = new SqlBuilder("select " + Stream.of(strings).collect(Collectors.joining(", ")) + " from ");
        sqlBuilder.openBracket();
        sqlBuilder.add(this.sqlBuilder.toSqlBuilder());
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public int count() {
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement statement = asFragment("count(*)").prepare(conn);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return 0;
    }

    private interface Pager {
        /**
         * Wraps the specified SqlBuilder with one that
         * takes the paging settings of this Pager into account.
         *
         * @param sqlBuilder The SqlBuilder
         * @return A new SqlBuilder with the paging settings
         */
        SqlBuilder addPaging(SqlBuilder sqlBuilder);
    }

    private class NoPaging implements Pager {
        @Override
        public SqlBuilder addPaging(SqlBuilder sqlBuilder) {
            return sqlBuilder;
        }
    }

    private class WithPaging implements Pager {
        private final int from;
        private final int to;

        private WithPaging(int from, int to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        public SqlBuilder addPaging(SqlBuilder sqlBuilder) {
            return sqlBuilder.asPageBuilder(this.from, this.to + 1);
        }
    }
}