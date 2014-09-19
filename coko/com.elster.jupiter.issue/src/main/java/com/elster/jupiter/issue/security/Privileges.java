package com.elster.jupiter.issue.security;

public interface Privileges {

    String VIEW_ISSUE = "privilege.view.issue";
    String COMMENT_ISSUE = "privilege.comment.issue";
    String CLOSE_ISSUE = "privilege.close.issue";
    String ASSIGN_ISSUE = "privilege.assign.issue";
    String ACTION_ISSUE = "privilege.action.issue";

    String VIEW_CREATION_RULE= "privilege.view.creationRule";
    String ADMINISTRATE_CREATION_RULE= "privilege.administrate.creationRule";

    String VIEW_ASSIGNMENT_RULE= "privilege.view.assignmentRule";

}
