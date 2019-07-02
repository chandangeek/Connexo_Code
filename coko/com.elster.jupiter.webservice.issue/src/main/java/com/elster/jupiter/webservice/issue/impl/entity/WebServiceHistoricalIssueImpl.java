/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.webservice.issue.WebServiceHistoricalIssue;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;

import javax.inject.Inject;

public final class WebServiceHistoricalIssueImpl extends WebServiceIssueImpl implements WebServiceHistoricalIssue {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();
    
    @Inject
    public WebServiceHistoricalIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(WebServiceOpenIssue webServiceOpenIssue) {
        setWebServiceCallOccurrence(webServiceOpenIssue.getWebServiceCallOccurrence());
    }
}
