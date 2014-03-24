package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionFailInfo;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.issue.share.entity.OperationResult;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignIssueTransaction  implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final AssignIssueRequest request;
    private final User author;

    public AssignIssueTransaction(AssignIssueRequest request, IssueService issueService, User author){
        this.request = request;
        this.issueService = issueService;
        this.author = author;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        IssueAssigneeType newAssigneeType = getAssigneeForIssue();
        if (request.getIssues() != null && newAssigneeType != null) {
            List<IssueShortInfo> success = new ArrayList<>();
            Map<String, ActionFailInfo> allFails = new HashMap<>();
            for (EntityReference issue : request.getIssues()) {
                OperationResult<String, String[]> result = issueService.assignIssue(issue.getId(),
                        issue.getVersion(), newAssigneeType, request.getAssignee().getId(), request.getComment(), author);
                checkForFails(result, allFails, issue);
                checkForSuccess(result, success, issue);
            }
            buildResponse(response, success, allFails);
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }

    private IssueAssigneeType getAssigneeForIssue(){
        if (request.getAssignee() != null){
            return IssueAssigneeType.fromString(request.getAssignee().getType());
        }
        return null;
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
