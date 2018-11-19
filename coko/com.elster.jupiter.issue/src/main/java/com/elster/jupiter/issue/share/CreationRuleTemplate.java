/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.properties.HasDynamicProperties;

import java.util.Optional;

@ConsumerType
public interface CreationRuleTemplate extends HasDynamicProperties {

    String getName();
    
    String getDisplayName();
    
    String getDescription();

    String getContent();

    IssueType getIssueType();

    OpenIssue createIssue(OpenIssue baseIssue, IssueEvent event);

    default void updateIssue(OpenIssue openIssue, IssueEvent event) {
        event.apply(openIssue);
        openIssue.update();
    }

    Optional<? extends Issue> resolveIssue(IssueEvent event);

}
