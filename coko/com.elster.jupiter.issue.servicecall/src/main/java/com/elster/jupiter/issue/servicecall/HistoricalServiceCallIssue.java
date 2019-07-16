/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HistoricalServiceCallIssue extends HistoricalIssue, ServiceCallIssue {

}
