package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Device;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link Finder} interface
 * for {@link Device}s that is backed by a custom built sql query.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-29 (09:51)
 */
public class DeviceFinder implements Finder<Device> {

    private final DataModel dataModel;
    private final DeviceSearchSqlBuilder sqlBuilder;
    private Pager pager = new NoPaging();

    public DeviceFinder(DeviceSearchSqlBuilder sqlBuilder, DataModel dataModel) {
        super();
        this.sqlBuilder = sqlBuilder;
        this.dataModel = dataModel;
    }

    @Override
    public List<Device> find() {
        SqlBuilder sqlBuilder = this.pager.addPaging(this.sqlBuilder.toSqlBuilder());
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
    public Finder<Device> paged(int from, int to) {
        this.pager = new WithPaging(from, to);
        return this;
    }

    @Override
    public Finder<Device> sorted(String s, boolean b) {
        throw new UnsupportedOperationException("Sorting is not implemented yet");
    }

    @Override
    public Subquery asSubQuery(String... strings) {
        throw new UnsupportedOperationException("Too hard for now, maybe in a future release");
    }

    @Override
    public SqlFragment asFragment(String... strings) {
        throw new UnsupportedOperationException("Too hard for now, maybe in a future release");
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
            return sqlBuilder.asPageBuilder(this.from, this.to+1);
        }
    }

}