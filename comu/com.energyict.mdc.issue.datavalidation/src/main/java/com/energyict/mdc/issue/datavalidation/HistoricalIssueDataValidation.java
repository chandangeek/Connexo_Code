/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HistoricalIssueDataValidation extends HistoricalIssue, IssueDataValidation {

}
