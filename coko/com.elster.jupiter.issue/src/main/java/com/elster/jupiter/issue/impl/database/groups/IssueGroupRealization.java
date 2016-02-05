package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
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
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService) {
            return new GroupByReasonImpl(dataModel, thesaurus, meteringGroupsService);
        }
    },
    STATUS {
        @Override
        public String getKey() {
            return "status";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService) {
            return new GroupByStatusImpl(dataModel, thesaurus, meteringGroupsService);
        }
    },
    ASSIGNEE {
        @Override
        public String getKey() {
            return "assignee";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService) {
            return new GroupByAssigneeImpl(dataModel, thesaurus, meteringGroupsService);
        }
    },
    TYPE {
        @Override
        public String getKey() {
            return "issueType";
        }

        @Override
        IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService) {
            return new GroupByIssueTypeImpl(dataModel, thesaurus, meteringGroupsService);
        }
    };

    abstract String getKey();

    abstract IssuesGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService);

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
