package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.share.entity.Issue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PrintAction implements IssueAction {
    private static final Logger LOG = Logger.getLogger(PrintAction.class.getName());

    public static String getActionName() {
        return "Print Action";
    }

    private Issue issue;

    public PrintAction(){}


    @Override
    public void execute() {
        if (issue != null) {
            LOG.log(Level.INFO, "Created issue: '" + issue.getTitle() + "', id = " + issue.getId());
        }
    }

    @Override
    public void setIssue(Issue issue) {
        this.issue = issue;
    }
}
