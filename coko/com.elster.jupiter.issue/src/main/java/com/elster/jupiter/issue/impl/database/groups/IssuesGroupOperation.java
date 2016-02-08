package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.records.IssueGroupImpl;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class IssuesGroupOperation {
    protected static final Logger LOG = Logger.getLogger(IssuesGroupOperation.class.getName());

    protected static final String GROUP_KEY = "key0";
    protected static final String GROUP_TITLE = "title0";
    protected static final String GROUP_COUNT = "count0";

    private final DataModel dataModel;
    private final MeteringGroupsService meteringGroupsService;
    private IssueGroupFilter filter;
    private Thesaurus thesaurus;

    public static IssuesGroupOperation from(IssueGroupFilter filter, DataModel dataModel, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus) {
        if (dataModel == null){
            throw new IllegalArgumentException("Data model can't be null");
        }
        if (filter == null){
            throw new IllegalArgumentException("You must specify the correct filter instance");
        }
        Optional<IssueGroupRealization> groupByRealizationRef = IssueGroupRealization.of(filter.getGroupBy());
        if (!groupByRealizationRef.isPresent()){
            throw new IllegalArgumentException("We can't group issues by this column: " + filter.getGroupBy());
        }
        IssuesGroupOperation operation = groupByRealizationRef.get().getOperation(dataModel, thesaurus, meteringGroupsService);
        operation.setFilter(filter);
        return operation;
    }

    protected IssuesGroupOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService){
        this.dataModel = dataModel;
        this.meteringGroupsService = meteringGroupsService;
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
                    groups.add(new IssueGroupImpl(this.thesaurus)
                            .init(rs.getObject(GROUP_KEY), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
                }
            }
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to retrieve grouped list from database", sqlEx);
        }
        return groups;
    }

    protected abstract SqlBuilder buildSQL();

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        if (connection == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Connection can't be null");
        }
        PreparedStatement statement = sql.prepare(connection);
        return statement;
    }

    protected String getTableName() {
        if (getFilter().getSourceClass().equals(Issue.class)) {
            return TableSpecs.ISU_ISSUE_ALL.name();
        }
        if (getFilter().getSourceClass().equals(OpenIssue.class)) {
            return TableSpecs.ISU_ISSUE_OPEN.name();
        }
        if (getFilter().getSourceClass().equals(HistoricalIssue.class)) {
            return TableSpecs.ISU_ISSUE_HISTORY.name();
        }
        return TableSpecs.ISU_ISSUE_ALL.name();
    }

    protected String getIssueTypeCondition(){
        if (getFilter().getIssueTypes() != null){
            StringBuilder builder = new StringBuilder();
            for (String issueType : getFilter().getIssueTypes()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("reason." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE).append(" = '").append(issueType).append("'");
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return  "";
    }

    protected String getStatusCondition(){
        if (getFilter().getStatuses() != null) {
            StringBuilder builder = new StringBuilder();
            for (String status : getFilter().getStatuses()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_STATUS_ID).append(" = '").append(status).append("'");
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return "";
    }

    protected String getMeterCondition() {
        if (getFilter().getMeterMrid() != null){
            StringBuilder builder = new StringBuilder();
            builder.append("device.MRID = '").append(getFilter().getMeterMrid()).append("'");
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    protected String getAssigneeCondition(){
        if (getFilter().getAssignees() != null) {
            StringBuilder builder = new StringBuilder();
            for (IssueGroupFilter.AssigneeDetails assigneeDetails : getFilter().getAssignees()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                AssigneeType type = AssigneeType.fromString(assigneeDetails.getAssigneeType());
                if (type != null) {
                    builder.append(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE).append(" = ").append(type.ordinal());
                    builder.append(" AND isu.");
                    builder.append(type.getColumnName());
                    builder.append(" = ").append(assigneeDetails.getAssigneeId());
                } else if (assigneeDetails.getAssigneeId() == -1L) {
                    builder.append(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE + " IS NULL AND isu.ASSIGNEE_USER_ID IS NULL");
                }
            }
            if (builder.length() != 0) {
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return "";
    }

    protected String getDueDateCondition() {
        if (getFilter().getDueDates() != null) {
            StringBuilder builder = new StringBuilder();
            for (IssueGroupFilter.DueDateRange dueDateRange : getFilter().getDueDates()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("isu.");
                builder.append(DatabaseConst.ISSUE_COLUMN_DUE_DATE);
                builder.append(" >= ").append(dueDateRange.getStartTime());
                builder.append(" AND isu.");
                builder.append(DatabaseConst.ISSUE_COLUMN_DUE_DATE);
                builder.append(" < ").append(dueDateRange.getEndTime());
            }
            if (builder.length() != 0) {
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return "";
    }

    protected void appendDeviceGroupCondition(SqlBuilder sqlBuilder) {
        if(getFilter().getDeviceGroups() != null && !getFilter().getDeviceGroups().isEmpty() ) {
            sqlBuilder.append(" AND (1!=1");
            for(Long deviceGroupId : getFilter().getDeviceGroups()) {
                EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(deviceGroupId).orElse(null);
                sqlBuilder.append(" OR isu.DEVICE_ID IN (");
                if (endDeviceGroup != null && endDeviceGroup.isDynamic()) {
                    sqlBuilder.add(((QueryEndDeviceGroup) endDeviceGroup).toFragment());
                    sqlBuilder.append(") ");
                } else {
                    sqlBuilder.append("select ENDDEVICE_ID from MTG_ENUM_ED_IN_GROUP where GROUP_ID = ");
                    sqlBuilder.append(deviceGroupId + ") ");
                }
                sqlBuilder.append(") ");
            }
        }
    }

    protected DataModel getDataModel() {
        return dataModel;
    }
}