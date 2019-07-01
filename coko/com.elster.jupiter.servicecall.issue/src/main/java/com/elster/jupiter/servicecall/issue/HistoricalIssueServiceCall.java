/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HistoricalIssueServiceCall extends HistoricalIssue, ServiceCallIssue {

}
