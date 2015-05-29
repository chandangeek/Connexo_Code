package com.elster.jupiter.issue.share;

import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.properties.HasDynamicProperties;

@ConsumerType
public interface IssueAction extends HasDynamicProperties, HasTranslatableNameAndProperties {
    
    boolean isApplicable(Issue issue);
    
    IssueAction initAndValidate(Map<String, Object> properties);
    
    IssueActionResult execute(Issue issue);

}
