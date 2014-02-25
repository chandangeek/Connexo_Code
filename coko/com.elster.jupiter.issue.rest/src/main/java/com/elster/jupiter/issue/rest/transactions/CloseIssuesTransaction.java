package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.issue.OperationResult;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionRequestFail;
import com.elster.jupiter.issue.rest.response.BaseActionResponse;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseIssuesTransaction implements Transaction<BaseActionResponse> {
    private final IssueService issueService;
    private CloseIssueRequest request;

    public CloseIssuesTransaction(CloseIssueRequest request, IssueService issueService){
        this.request = request;
        this.issueService = issueService;
    }

    @Override
    public BaseActionResponse perform() {
        BaseActionResponse response = new BaseActionResponse();
        IssueStatus newIssueStatus = issueService.getIssueStatusById(request.getStatus()).orNull();
        if (request.getIssues() != null && newIssueStatus != null) {
            List<Long> success = new ArrayList<>();
            Map<String, ActionRequestFail> allCloseFails = new HashMap<>();
            for (EntityReference issueForClose : request.getIssues()) {
                OperationResult<String, String[]> result = issueService.closeIssue(issueForClose.getId(), issueForClose.getVersion(), newIssueStatus, request.getComment(), request.isForce());
                if (result.isFailed()) {
                    String failReason = result.getFailReason()[0];
                    String issueTitle = result.getFailReason()[1];

                    ActionRequestFail failsWithSameReason = allCloseFails.get(failReason);
                    if (failsWithSameReason == null) {
                        failsWithSameReason = new ActionRequestFail();
                        failsWithSameReason.setReason(failReason);
                        allCloseFails.put(failReason, failsWithSameReason);
                    }
                    ActionRequestFail.IssueFailInfo issueFail = new ActionRequestFail.IssueFailInfo(issueForClose.getId(), issueTitle);
                    failsWithSameReason.getIssues().add(issueFail);
                } else {
                    success.add(issueForClose.getId());
                }
            }
            response.setSuccess(success);
            response.setFailure(new ArrayList<ActionRequestFail>(allCloseFails.values()));
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return response;
    }
}
