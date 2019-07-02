/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceIssue extends Issue {
    WebServiceCallOccurrence getWebServiceCallOccurrence();
}
