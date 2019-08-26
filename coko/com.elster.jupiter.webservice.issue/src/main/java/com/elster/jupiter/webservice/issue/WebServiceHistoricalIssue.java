/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceHistoricalIssue extends HistoricalIssue, WebServiceIssue {

}
