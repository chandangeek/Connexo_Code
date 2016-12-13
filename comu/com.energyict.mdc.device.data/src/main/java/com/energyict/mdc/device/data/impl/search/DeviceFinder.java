package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.search.sqlbuilder.DeviceSearchSqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        this.orders.add(Order.ascending("name"));
    }

    @Override
    public List<Device> find() {
        SqlBuilder sqlBuilder = this.sqlBuilder.toSqlBuilder();
        sqlBuilder.append(" ORDER BY " + this.orders.stream()
                .map(order -> order.getClause(order.getName()))
                .collect(Collectors.joining(", ")));
        final SqlBuilder finalBuilder = this.pager.finalize(sqlBuilder, "id");
        QueryExecutor<Device> query = this.dataModel.query(Device.class, DeviceConfiguration.class, DeviceType.class, Batch.class);
        return query.select(ListOperator.IN.contains(() -> finalBuilder, "id"), this.orders.toArray(new Order[orders.size()]));
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
    public SqlFragment asFragment(String... strings) {
        Subquery subquery = () -> new NoPaging().finalize(sqlBuilder.toSqlBuilder(), "id");
        return dataModel.query(Device.class)
                .asFragment(ListOperator.IN.contains(subquery, "id"), strings);
    }

    @Override
    public int count() {
        try (Connection conn = dataModel.getConnection(false)) {
            try (PreparedStatement statement = new NoPaging()
                    .finalize(sqlBuilder.toSqlBuilder(), "count(*)")
                    .prepare(conn)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private interface Pager {
        /**
         * Wraps the specified SqlBuilder with one that
         * takes the paging settings of this Pager into account.
         *
         * @param sqlBuilder The SqlBuilder
         * @return A new SqlBuilder with the paging settings
         */
        SqlBuilder finalize(SqlBuilder sqlBuilder, String field);
    }

    private class NoPaging implements Pager {
        @Override
        public SqlBuilder finalize(SqlBuilder sqlBuilder, String field) {
            SqlBuilder builder = new SqlBuilder("select " + field + " from (");
            builder.add(sqlBuilder);
            builder.append(")");
            return builder;
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
        public SqlBuilder finalize(SqlBuilder sqlBuilder, String field) {
            return sqlBuilder.asPageBuilder(field, this.from + 1, this.to + 1);
        }
    }
}
