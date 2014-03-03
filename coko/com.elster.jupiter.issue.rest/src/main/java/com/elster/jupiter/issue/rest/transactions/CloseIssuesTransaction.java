package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.issue.OperationResult;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionFailInfo;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseIssuesTransaction implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final CloseIssueRequest request;

    public CloseIssuesTransaction(CloseIssueRequest request, IssueService issueService){
        this.request = request;
        this.issueService = issueService;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        IssueStatus newIssueStatus = issueService.getIssueStatusFromString(request.getStatus());
        if (request.getIssues() != null && newIssueStatus != null) {
            List<IssueShortInfo> success = new ArrayList<>();
            Map<String, ActionFailInfo> allCloseFails = new HashMap<>();
            for (EntityReference issueForClose : request.getIssues()) {
                OperationResult<String, String[]> result = issueService.closeIssue(issueForClose.getId(), issueForClose.getVersion(), newIssueStatus, request.getComment());
                if (result.isFailed()) {
                    String failReason = result.getFailReason()[0];
                    String issueTitle = result.getFailReason()[1];

                    ActionFailInfo failsWithSameReason = allCloseFails.get(failReason);
                    if (failsWithSameReason == null) {
                        failsWithSameReason = new ActionFailInfo();
                        failsWithSameReason.setReason(failReason);
                        allCloseFails.put(failReason, failsWithSameReason);
                    }
                    IssueShortInfo issueFail = new IssueShortInfo(issueForClose.getId(), issueTitle);
                    failsWithSameReason.getIssues().add(issueFail);
                } else {
                    success.add(new IssueShortInfo(issueForClose.getId()));
                }
            }
            response.setSuccess(success);
            response.setFailure(new ArrayList<ActionFailInfo>(allCloseFails.values()));
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return response;
    }
}
