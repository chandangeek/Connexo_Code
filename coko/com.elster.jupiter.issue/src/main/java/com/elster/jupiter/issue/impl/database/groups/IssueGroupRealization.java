package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;

public enum IssueGroupRealization {
    REASON {
        @Override
        public String getKey() {
            return "reason";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByReasonImpl(dataModel, thesaurus);
        }
    },
    STATUS {
        @Override
        public String getKey() {
            return "status";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByStatusImpl(dataModel, thesaurus);
        }
    },
    ASSIGNEE {
        @Override
        public String getKey() {
            return "userAssignee";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByAssigneeImpl(dataModel, thesaurus);
        }
    },
    WORKGROUP {
        @Override
        public String getKey() {
            return "workgroupAssignee";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByWorkGroupAssigneeImpl(dataModel, thesaurus);
        }
    },
    TYPE {
        @Override
        public String getKey() {
            return "issueType";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByIssueTypeImpl(dataModel, thesaurus);
        }
    };

    abstract String getKey();

    abstract IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus);

    public static Optional<IssueGroupRealization> of(String text) {
        if (text != null) {
            for (IssueGroupRealization groupByRealization : IssueGroupRealization.values()) {
                if (groupByRealization.getKey().equalsIgnoreCase(text)) {
                    return Optional.of(groupByRealization);
                }
            }
        }
        return Optional.empty();
    }
}
