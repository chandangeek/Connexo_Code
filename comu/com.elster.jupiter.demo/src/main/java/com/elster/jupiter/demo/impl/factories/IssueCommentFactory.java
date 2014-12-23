package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Optional;

public class IssueCommentFactory implements Factory<IssueComment>{
    private final UserService userService;

    private String user;
    private String comment;
    private Issue issue;

    @Inject
    public IssueCommentFactory(UserService userService) {
        this.userService = userService;
    }

    public IssueCommentFactory withUser(String userName){
        this.user = userName;
        return this;
    }

    public IssueCommentFactory withComment(String body){
        this.comment = body;
        return this;
    }

    public IssueCommentFactory withIssue(Issue issue){
        this.issue = issue;
        return this;
    }

    public IssueComment get(){
        Log.write(this);
        Optional<IssueComment> issueComment = issue.addComment(comment, userService.findUser(user).orElseThrow(() -> new UnableToCreate("Unable to find user " + user)));
        return issueComment.get();
    }
}
