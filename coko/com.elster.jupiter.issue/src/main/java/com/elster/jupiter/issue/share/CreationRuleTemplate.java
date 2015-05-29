package com.elster.jupiter.issue.share;

import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.properties.HasDynamicProperties;

@ConsumerType
public interface CreationRuleTemplate extends HasDynamicProperties, HasTranslatableNameAndProperties {

    String getName();
    
    String getDescription();

    String getContent();

    IssueType getIssueType();

    Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event);

    Optional<? extends Issue> resolveIssue(IssueEvent event);

}
