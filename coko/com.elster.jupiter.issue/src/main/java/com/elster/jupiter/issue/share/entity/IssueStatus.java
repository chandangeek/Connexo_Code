/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueStatus extends Entity {

    String OPEN = "status.open";
    String RESOLVED = "status.resolved";
    String WONT_FIX = "status.wont.fix";
    String IN_PROGRESS = "status.in.progress";
    String SNOOZED = "status.snoozed";
    String FORWARDED = "status.forwarded";

    String getKey();

    String getName();

    boolean isHistorical();
}
