/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

public class IssuesCollectionInfo {
    public long total;
    public IssuesCollectionFilterInfo filter;
    public List<IssueInfo> topMyIssues;
}