package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionFailInfo;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OperationResult;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseIssuesTransaction implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final IssueMainService issueMainService;
    private final CloseIssueRequest request;
    private final User author;


    public CloseIssuesTransaction(CloseIssueRequest request, IssueService issueService, IssueMainService issueMainService, User author) {
        this.request = request;
        this.issueService = issueService;
        this.issueMainService = issueMainService;
        this.author = author;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();

        IssueStatus newIssueStatus = new IssueStatus();
        newIssueStatus.setName(request.getStatus());
        newIssueStatus.setFinal(true);
        newIssueStatus = issueMainService.searchFirst(newIssueStatus).orNull();
        if (request.getIssues() != null && newIssueStatus != null) {
            List<IssueShortInfo> success = new ArrayList<>();
            Map<String, ActionFailInfo> allFails = new HashMap<>();
            for (EntityReference issue : request.getIssues()) {
                OperationResult<String, String[]> result = issueService.closeIssue(issue.getId(), issue.getVersion(), newIssueStatus, request.getComment(), author);
                checkForFails(result, allFails, issue);
                checkForSuccess(result, success, issue);
            }
            buildResponse(response, success, allFails);
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }


    private void checkForFails(OperationResult<String, String[]> result,  Map<String, ActionFailInfo> allFails, EntityReference issue){
        if (result != null && result.isFailed()){
            String failReason = result.getFailReason()[0];
            String issueTitle = result.getFailReason()[1];

            ActionFailInfo failsWithSameReason = allFails.get(failReason);
            if (failsWithSameReason == null) {
                failsWithSameReason = new ActionFailInfo();
                failsWithSameReason.setReason(failReason);
                allFails.put(failReason, failsWithSameReason);
            }
            IssueShortInfo issueFail = new IssueShortInfo(issue.getId(), issueTitle);
            failsWithSameReason.getIssues().add(issueFail);
        }
    }

    private void checkForSuccess(OperationResult<String, String[]> result,  List<IssueShortInfo> success, EntityReference issue){
        if (result != null && !result.isFailed()){
            success.add(new IssueShortInfo(issue.getId()));
        }
    }

    private void buildResponse(ActionInfo response, List<IssueShortInfo> success, Map<String, ActionFailInfo> allFails){
        if (response != null){
            response.setSuccess(success);
            response.setFailure(new ArrayList<ActionFailInfo>(allFails.values()));
        }
    }
}
