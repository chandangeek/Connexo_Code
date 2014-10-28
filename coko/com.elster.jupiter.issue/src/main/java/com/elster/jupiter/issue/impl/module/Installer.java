package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;

import java.util.logging.Logger;

public class Installer {
    private static final Logger LOG = Logger.getLogger("IssueInstaller");

    private final DataModel dataModel;
    private final IssueService issueService;

    public Installer(DataModel dataModel, IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    public void install(boolean executeDDL) {
        run(() -> dataModel.install(executeDDL, false),
                "database schema. Execute command 'ddl " + IssueService.COMPONENT_NAME + "' and apply the sql script manually");
        run(this::createViews, "view for all issues");
        run(this::setDefaultStatuses, "default statuses");
    }

    private void createViews(){
        new CreateIssueViewOperation(dataModel).execute();
    }

    private void setDefaultStatuses(){
        issueService.createStatus(IssueStatus.OPEN, false, MessageSeeds.ISSUE_STATUS_OPEN);
        issueService.createStatus(IssueStatus.IN_PROGRESS, false, MessageSeeds.ISSUE_STATUS_IN_PROGRESS);
        issueService.createStatus(IssueStatus.RESOLVED, true, MessageSeeds.ISSUE_STATUS_RESOLVED);
        issueService.createStatus(IssueStatus.WONT_FIX, true, MessageSeeds.ISSUE_STATUS_WONT_FIX);
    }

    public static void run(Runnable runnable, String explanation){
        try {
            runnable.run();
        } catch (Exception stEx){
            LOG.warning("[" + IssueService.COMPONENT_NAME + "] Unable to install " + explanation);
        }
    }
}
