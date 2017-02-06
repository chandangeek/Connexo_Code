package com.elster.jupiter.kore.api.v2;


import com.elster.jupiter.issue.share.entity.IssueType;

public class IssueTypeInfoFactory {

    public IssueTypeInfo asInfo(IssueType type) {
        IssueTypeInfo info = new IssueTypeInfo();
        info.uid = type.getKey();
        info.name = type.getName();
        return info;
    }
}
