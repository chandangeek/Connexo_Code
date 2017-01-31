/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import java.util.Optional;

import com.elster.jupiter.issue.share.IssueAction;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueActionType extends Entity {

    CreationRuleActionPhase getPhase();

    Optional<IssueAction> createIssueAction();

    IssueReason getIssueReason();

    IssueType getIssueType();

    String getFactoryId();

    String getClassName();

}
