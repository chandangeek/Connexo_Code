/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

final class AdHocDeviceGroupImpl implements AdHocDeviceGroup {

    private String ADHOC_GROUP_NAME_PREFIX = "__##SEARCH_RESULTS##__";
    private static final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static final int ROWCOUNT_ITEMS = 100;

    private long id;
    @Size(max=NAME_LENGTH)
    private String name;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;

    private List<AdHocEntryImpl> entries = new ArrayList<>();
    private final DataModel dataModel;

    @Inject
    private AdHocDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static AdHocDeviceGroupImpl from(DataModel dataModel, long id, List<Long> devices) {
        return dataModel.getInstance(AdHocDeviceGroupImpl.class).init(id, devices);
    }

    private AdHocDeviceGroupImpl init(long id, List<Long> devices) {
        this.id = id;
        this.name = String.format(ADHOC_GROUP_NAME_PREFIX + "%d", id);
        entries.addAll(devices.stream().map(deviceId -> AdHocEntryImpl.from(dataModel, id, deviceId)).collect(Collectors.toList()));
        return this;
    }

    static AdHocDeviceGroupImpl from(DataModel dataModel) {
        return dataModel.getInstance(AdHocDeviceGroupImpl.class);
    }

    void purgeAdHocSearch(int lastDays) {
        Instant instant = Instant.now();
        instant = instant.minusSeconds(lastDays * SECONDS_IN_DAY);
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement statement = buildStatement(conn, buildCreateSQL(instant));
            int removedItems;
            do {
                removedItems = statement.executeUpdate();
            } while (removedItems > 0);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private SqlBuilder buildCreateSQL(Instant instant) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("delete from " + TableSpecs.YFN_ADHOC_DG);
        builder.append(" where CREATETIME < " + instant.getEpochSecond() * 1000 + " and rownum < " + ROWCOUNT_ITEMS);
        return builder;
    }

    private PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection can't be null");
        }
        return sql.prepare(connection);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    static class AdHocEntryImpl {
        private long groupId;
        private long deviceId;

        AdHocEntryImpl init(long groupId, long deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            return this;
        }

        static AdHocEntryImpl from(DataModel dataModel, long groupId, long deviceId) {
            return dataModel.getInstance(AdHocEntryImpl.class).init(groupId, deviceId);
        }

        long getDeviceId() {
            return deviceId;
        }

        public long getGroupId() {
            return groupId;
        }

        public void setGroupId(long id) {
            this.groupId = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AdHocEntryImpl entry = (AdHocEntryImpl) o;

            return groupId == entry.getGroupId() && deviceId == entry.getDeviceId();

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, deviceId);
        }
    }

    public void save() {
        factory().persist(this);
        this.name = String.format(ADHOC_GROUP_NAME_PREFIX + "%d", id);
        factory().update(this);
        for (AdHocEntryImpl entry : entries) {
            entry.setGroupId(id);
        }

        List<AdHocEntryImpl> result = entries.stream().collect(Collectors.toList());
        entryFactory().persist(result);
    }

    private DataMapper<AdHocDeviceGroupImpl> factory() {
        return dataModel.mapper(AdHocDeviceGroupImpl.class);
    }

    private DataMapper<AdHocEntryImpl> entryFactory() {
        return dataModel.mapper(AdHocEntryImpl.class);
    }

    List<AdHocEntryImpl> getEntries() {
        return this.entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdHocDeviceGroupImpl)) {
            return false;
        }
        AdHocDeviceGroupImpl that = (AdHocDeviceGroupImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}