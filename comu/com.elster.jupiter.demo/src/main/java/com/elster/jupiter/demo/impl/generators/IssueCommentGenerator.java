package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class IssueCommentGenerator {

    private final Store store;
    private final UserService userService;


    private String user;
    private String comment;
    private Issue issue;

    @Inject
    public IssueCommentGenerator(Store store, UserService userService) {
        this.store = store;
        this.userService = userService;
    }

    public IssueCommentGenerator withUser(String userName){
        this.user = userName;
        return this;
    }

    public IssueCommentGenerator withComment(String body){
        this.comment = body;
        return this;
    }

    public IssueCommentGenerator withIssue(Issue issue){
        this.issue = issue;
        return this;
    }

    public void create(){
        System.out.println(" ==> Creating comment for issue " + issue.getTitle() + "...");
        issue.addComment(comment, userService.findUser(user).orElseThrow(() -> new UnableToCreate("Unable to find user " + user)));
    }
}
