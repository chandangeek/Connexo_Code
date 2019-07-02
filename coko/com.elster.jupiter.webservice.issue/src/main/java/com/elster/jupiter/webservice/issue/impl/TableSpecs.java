/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.webservice.issue.WebServiceHistoricalIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssue;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceHistoricalIssueImpl;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceIssueImpl;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceOpenIssueImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    WSI_ISSUE_OPEN {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceOpenIssue> table = dataModel.addTable(name(), WebServiceOpenIssue.class);
            table.map(WebServiceOpenIssueImpl.class);

            Column issueColRef = table.column(WebServiceIssueImpl.Fields.BASE_ISSUE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();
            Column wscoColumn = table.column(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();

            table.addAuditColumns();
            table.primaryKey("WSI_ISSUE_OPEN_PK").on(issueColRef).add();
            table.foreignKey("WSI_ISSUE_OPEN_FK_TO_ISSUE")
                    .on(issueColRef)
                    .references(OpenIssue.class)
                    .map(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName())
                    .add();
            table.foreignKey("WSI_ISSUE_OPEN_FK_TO_WSCO")
                    .on(wscoColumn)
                    .references(WebServiceCallOccurrence.class)
                    .map(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    WSI_ISSUE_HISTORY {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceHistoricalIssue> table = dataModel.addTable(name(), WebServiceHistoricalIssue.class);
            table.map(WebServiceHistoricalIssueImpl.class);

            Column issueColRef = table.column(WebServiceIssueImpl.Fields.BASE_ISSUE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();
            Column wscoColumn = table.column(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();

            table.addAuditColumns();
            table.primaryKey("WSI_ISSUE_HIST_PK").on(issueColRef).add();
            table.foreignKey("WSI_ISSUE_HIST_FK_TO_ISSUE")
                    .on(issueColRef)
                    .references(HistoricalIssue.class)
                    .map(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName())
                    .add();
            table.foreignKey("WSI_ISSUE_HIST_FK_TO_WSCO")
                    .on(wscoColumn)
                    .references(WebServiceCallOccurrence.class)
                    .map(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    WSI_ISSUE_ALL {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceIssue> table = dataModel.addTable(name(), WebServiceIssue.class);
            table.map(WebServiceIssueImpl.class);
            table.doNotAutoInstall(); // because it is mapped to view

            Column issueColRef = table.column(WebServiceIssueImpl.Fields.BASE_ISSUE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();
            Column wscoColumn = table.column(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .notNull()
                    .add();

            table.addAuditColumns();
            table.primaryKey("WSI_ISSUE_PK").on(issueColRef).add();
            table.foreignKey("WSI_ISSUE_FK_TO_ISSUE")
                    .on(issueColRef)
                    .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                    .map(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName())
                    .add();
            table.foreignKey("WSI_ISSUE_FK_TO_WSCO")
                    .on(wscoColumn)
                    .references(WebServiceCallOccurrence.class)
                    .map(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
