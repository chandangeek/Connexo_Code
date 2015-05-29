package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueType extends Entity {

    String getKey();

    String getName();
}
