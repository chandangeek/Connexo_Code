package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.orm.DataModel;

public enum IssueGroupColumns {
    REASON {
        @Override
        public String getDisplayName() {
            return "Reason";
        }

        @Override
        GroupIssuesOperation getOperationImplementer(DataModel dataModel) {
            return new GroupByReasonImplementer(dataModel);
        }
    };

    public abstract String getDisplayName();

    abstract GroupIssuesOperation getOperationImplementer(DataModel dataModel);

    public static IssueGroupColumns fromString(String text) {
        if (text != null) {
            for (IssueGroupColumns column : IssueGroupColumns.values()) {
                if (column.getDisplayName().equalsIgnoreCase(text)) {
                    return column;
                }
            }
        }
        return null;
    }
}
