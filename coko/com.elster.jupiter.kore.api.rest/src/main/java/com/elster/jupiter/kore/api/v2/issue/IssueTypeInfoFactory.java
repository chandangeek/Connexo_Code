/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


import com.elster.jupiter.issue.share.entity.IssueType;

public class IssueTypeInfoFactory {

    public IssueTypeInfo asInfo(IssueType type) {
        IssueTypeInfo info = new IssueTypeInfo();
        info.uid = type.getKey();
        info.name = type.getName();
        return info;
    }
}
