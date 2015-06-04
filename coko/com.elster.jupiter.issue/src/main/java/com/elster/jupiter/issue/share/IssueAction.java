package com.elster.jupiter.issue.share;

import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

@ConsumerType
public interface IssueAction extends HasDynamicPropertiesWithValues {
    
    String getDisplayName();
    
    boolean isApplicable(Issue issue);
    
    IssueAction initAndValidate(Map<String, Object> properties);
    
    IssueActionResult execute(Issue issue);

}
