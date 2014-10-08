package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.orm.DataModel;

public enum IssueGroupBy {
    REASON {
        @Override
        public String getDisplayName() {
            return "Reason";
        }

        @Override
        IssuesGroupOperation getOperationImplementer(DataModel dataModel) {
            return new GroupByReasonImpl(dataModel);
        }
    };

    public abstract String getDisplayName();

    abstract IssuesGroupOperation getOperationImplementer(DataModel dataModel);

    public static IssueGroupBy fromString(String text) {
        if (text != null) {
            for (IssueGroupBy column : IssueGroupBy.values()) {
                if (column.getDisplayName().equalsIgnoreCase(text)) {
                    return column;
                }
            }
        }
        return null;
    }
}
