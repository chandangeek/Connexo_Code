/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueStatus extends Entity {

    public static final String OPEN = "status.open";
    public static final String RESOLVED = "status.resolved";
    public static final String WONT_FIX = "status.wont.fix";
    public static final String IN_PROGRESS = "status.in.progress";

    String getKey();

    String getName();

    boolean isHistorical();
}
