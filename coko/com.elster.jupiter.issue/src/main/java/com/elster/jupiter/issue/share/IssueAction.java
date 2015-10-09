package com.elster.jupiter.issue.share;

import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;
import com.elster.jupiter.users.User;

@ConsumerType
public interface IssueAction extends HasDynamicPropertiesWithValues {
    
    String getDisplayName();
    
    boolean isApplicable(Issue issue);

    boolean isApplicableForUser(User user);
    
    IssueAction initAndValidate(Map<String, Object> properties);
    
    IssueActionResult execute(Issue issue);

}
