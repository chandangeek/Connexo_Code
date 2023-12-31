/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_ISSUES("issue.issues", "Issues"),
    RESOURCE_ISSUES_DESCRIPTION("issue.issues.description", "Manage issues"),
    RESOURCE_ISSUES_CONFIGURATION("issueConfiguration.issueConfigurations", "Issues configuration"),
    RESOURCE_ISSUES_CONFIGURATION_DESCRIPTION("issueConfiguration.issueConfigurations.description", "Manage issues configuration"),

    //Privileges
    VIEW_ISSUE(Constants.VIEW_ISSUE, "View"),
    COMMENT_ISSUE(Constants.COMMENT_ISSUE, "Comment"),
    CLOSE_ISSUE(Constants.CLOSE_ISSUE, "Close"),
    ASSIGN_ISSUE(Constants.ASSIGN_ISSUE, "Assign"),
    ACTION_ISSUE(Constants.ACTION_ISSUE, "Action"),
    CREATE_ISSUE(Constants.CREATE_ISSUE, "Create"),
    VIEW_CREATION_RULE(Constants.VIEW_CREATION_RULE, "View creation rules"),
    ADMINISTRATE_CREATION_RULE(Constants.ADMINISTRATE_CREATION_RULE, "Administrate creation rules"),
    VIEW_ASSIGNMENT_RULE(Constants.VIEW_ASSIGNMENT_RULE, "View assignment rules");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String VIEW_ISSUE = "privilege.view.issue";
        String COMMENT_ISSUE = "privilege.comment.issue";
        String CLOSE_ISSUE = "privilege.close.issue";
        String ASSIGN_ISSUE = "privilege.assign.issue";
        String ACTION_ISSUE = "privilege.action.issue";
        String VIEW_CREATION_RULE= "privilege.view.creationRule";
        String ADMINISTRATE_CREATION_RULE= "privilege.administrate.creationRule";
        String VIEW_ASSIGNMENT_RULE= "privilege.view.assignmentRule";
        String CREATE_ISSUE = "privilege.create.issue";
    }
}

