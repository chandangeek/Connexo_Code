package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.records.IssueGroupImpl;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.OpenIssue;
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

    static final String GROUP_KEY = "key0";
    static final String GROUP_TITLE = "title0";
    static final String GROUP_COUNT = "count0";

    private final DataModel dataModel;
    private IssueGroupFilter filter;
    private Thesaurus thesaurus;

    public static IssuesGroupOperation from(IssueGroupFilter filter, DataModel dataModel, Thesaurus thesaurus) {
        if (dataModel == null) {
            throw new IllegalArgumentException("Data model can't be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("You must specify the correct filter instance");
        }
        Optional<IssueGroupRealization> groupByRealizationRef = IssueGroupRealization.of(filter.getGroupBy());
        if (!groupByRealizationRef.isPresent()) {
            throw new IllegalArgumentException("We can't group issues by this column: " + filter.getGroupBy());
        }
        IssuesGroupOperation operation = groupByRealizationRef.get().getOperation(dataModel, thesaurus);
        operation.setFilter(filter);
        return operation;
    }

    IssuesGroupOperation(DataModel dataModel, Thesaurus thesaurus) {
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
        try (Connection conn = dataModel.getConnection(false);
             PreparedStatement groupingStatement = buildStatement(conn, sql);
             ResultSet rs = groupingStatement.executeQuery()) {
            while (rs.next()) {
                groups.add(new IssueGroupImpl(this.thesaurus)
                        .init(rs.getObject(GROUP_KEY), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
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

    protected String getIssueIdColumnName(String tableName) {
        if (tableName.equals("ISU_ISSUE_HISTORY")) {
            return "ISU_HIST_ISSUE_ID";
        } else {
            return "ID";
        }
    }

    String getIssueTypeCondition() {
        StringBuilder builder = new StringBuilder();
        for (String issueType : getFilter().getIssueTypes()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            builder.append("reason." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE)
                    .append(" = '")
                    .append(issueType)
                    .append("'");
        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
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

    String getClearedStatuses() {
        StringBuilder builder = new StringBuilder();
        for (String cleared : getFilter().getClearedStatuses()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            if (cleared.toLowerCase().equals("yes")) {
                builder.append("dal.CLEARED_STATUS").append(" = '").append("Y").append("'")
                        .append(" OR ").append("dalH.CLEARED_STATUS").append(" = '").append("Y").append("'");
            }
            if (cleared.toLowerCase().equals("no")) {
                builder.append("dal.CLEARED_STATUS").append(" = '").append("N").append("'")
                        .append(" OR ").append("dalH.CLEARED_STATUS").append(" = '").append("N").append("'");
            }

        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    String getMeterCondition() {
        if (getFilter().getMeterName() != null) {
            return " AND (device.name = '" + getFilter().getMeterName() + "') ";
        }
        return "";
    }

    String getIdCondition() {
        if (getFilter().getId() != null) {
            return " AND (isu.ID = " + getFilter().getId() + ") ";
        }
        return "";
    }

    String getUserAssigneeCondition() {
        StringBuilder builder = new StringBuilder();
        for (Long id : getFilter().getUserAssignees()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            if (id < 0) {
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_USER_ID).append(" is ").append("null");
            } else {
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_USER_ID).append(" = '").append(id).append("'");
            }
        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    String getWorkGroupCondition() {
        StringBuilder builder = new StringBuilder();
        for (Long id : getFilter().getWorkGroupAssignees()) {
            if (builder.length() != 0) {
                builder.append(" OR ");
            }
            if (id < 0) {
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_USER_ID).append(" is ").append("null");
            } else {
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_WORKGROUP_ID).append(" = '").append(id).append("'");
            }
        }
        if (builder.length() != 0) {
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    String getDueDateCondition() {
        StringBuilder builder = new StringBuilder();
        for (DueDateRange dueDateRange : getFilter().getDueDates()) {
            if (builder.length() != 0) {
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
        return "";
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

}