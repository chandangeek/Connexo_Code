package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueReason extends Entity {
    
    String getKey();

    String getName();

    IssueType getIssueType();
}
