package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueGroup {
    
    Object getGroupKey();

    String getGroupName();

    long getCount();

}
