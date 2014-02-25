package com.elster.jupiter.issue.module;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.orm.DataModel;

public class Installer {
    /*
    If you want to add EventService see @com.elster.jupiter.parties.impl.Installer class file
     */
    private final DataModel dataModel;
    private final IssueService issueService;

    public Installer (IssueService issueService, DataModel dataModel) {
        this.issueService = issueService;
        this.dataModel = dataModel;
    }

    public void install(boolean executeDDL, boolean store) {
        dataModel.install(executeDDL, store);
        setDefaultReasons();
        setDefaultStatuses();
    }

    private void setDefaultReasons(){
        this.issueService.createIssueReason("Unable to connect");
        this.issueService.createIssueReason("Failed to communicate");
        this.issueService.createIssueReason("Connection lost");
        this.issueService.createIssueReason("Web import service down");
    }

    private void setDefaultStatuses(){
        this.issueService.createIssueStatus("Open");
        this.issueService.createIssueStatus("Closed");
        this.issueService.createIssueStatus("Rejected");
        this.issueService.createIssueStatus("In progress");
    }
}
