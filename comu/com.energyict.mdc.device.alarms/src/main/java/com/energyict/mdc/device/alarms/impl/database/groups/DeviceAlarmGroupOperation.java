/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmGroupImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DeviceAlarmGroupOperation {
    protected static final Logger LOG = Logger.getLogger(DeviceAlarmGroupOperation.class.getName());

    static final String GROUP_KEY = "key0";
    static final String GROUP_TITLE = "title0";
    static final String GROUP_COUNT = "count0";

    private final DataModel dataModel;
    private IssueGroupFilter filter;
    private Thesaurus thesaurus;

    public static DeviceAlarmGroupOperation from(IssueGroupFilter filter, DataModel dataModel, Thesaurus thesaurus) {
        if (dataModel == null) {
            throw new IllegalArgumentException("Data model can't be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("You must specify the correct filter instance");
        }
        Optional<DeviceAlarmGroupRealization> groupByRealizationRef = DeviceAlarmGroupRealization.of(filter.getGroupBy());
        if (!groupByRealizationRef.isPresent()) {
            throw new IllegalArgumentException("We can't group issues by this column: " + filter.getGroupBy());
        }
        DeviceAlarmGroupOperation operation = groupByRealizationRef.get().getOperation(dataModel, thesaurus);
        operation.setFilter(filter);
        return operation;
    }

    DeviceAlarmGroupOperation(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    protected IssueGroupFilter getFilter() {
        return filter;
    }

    private void setFilter(IssueGroupFilter filter) {
        this.filter = filter;
    }

    public List<IssueGroup> execute() {
        List<IssueGroup> groups = new LinkedList<>();
        SqlBuilder sql = buildSQL();
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement groupingStatement = buildStatement(conn, sql);
            try (ResultSet rs = groupingStatement.executeQuery()) {
                while (rs.next()) {
                    groups.add(new DeviceAlarmGroupImpl(this.thesaurus)
                            .init(rs.getObject(GROUP_KEY), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
                }
            }
        } catch (SQLException sqlEx) {
            LOG.log(Level.SEVERE, "Unable to retrieve grouped list from database", sqlEx);
        }
        return groups;
    }

    protected abstract SqlBuilder buildSQL();

    private PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Connection can't be null");
        }
        return sql.prepare(connection);
    }

    String getStatusCondition() {
        StringBuilder builder = new StringBuilder();
        for (String status : getFilter().getStatuses()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            builder.append("isu." + DatabaseConst.ISSUE_COLUMN_STATUS_ID).append(" = '").append(status).append("'");
        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    String getReasonCondition() {
        StringBuilder builder = new StringBuilder();

        for (String reason : getFilter().getReasons()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            builder.append("isu." + DatabaseConst.ISSUE_COLUMN_REASON_ID).append(" = '").append(reason).append("'");
        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }
}
