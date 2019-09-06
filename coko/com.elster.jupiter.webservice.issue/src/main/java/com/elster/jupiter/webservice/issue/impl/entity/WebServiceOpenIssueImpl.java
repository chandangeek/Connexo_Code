/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.webservice.issue.WebServiceHistoricalIssue;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;

import javax.inject.Inject;

public final class WebServiceOpenIssueImpl extends WebServiceIssueImpl implements WebServiceOpenIssue {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public WebServiceOpenIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    @Override
    public void setWebServiceCallOccurrence(WebServiceCallOccurrence webServiceCallOccurrence) {
        super.setWebServiceCallOccurrence(webServiceCallOccurrence);
    }

    @Override
    public WebServiceHistoricalIssue close(IssueStatus status) {
        WebServiceHistoricalIssueImpl webServiceHistoricalIssue = getDataModel().getInstance(WebServiceHistoricalIssueImpl.class);
        webServiceHistoricalIssue.copy(this);
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        webServiceHistoricalIssue.setIssue(historicalBaseIssue);
        webServiceHistoricalIssue.save();
        return webServiceHistoricalIssue;
    }
}
