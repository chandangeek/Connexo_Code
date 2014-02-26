package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.IssueAssigneeType;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.OperationResult;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionRequestFail;
import com.elster.jupiter.issue.rest.response.BaseActionResponse;
import com.elster.jupiter.issue.rest.response.IssueShortInfo;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignIssueTransaction  implements Transaction<BaseActionResponse> {
    private final IssueService issueService;
    private AssignIssueRequest request;

    public AssignIssueTransaction(AssignIssueRequest request, IssueService issueService){
        this.request = request;
        this.issueService = issueService;
    }

    @Override
    public BaseActionResponse perform() {
        BaseActionResponse response = new BaseActionResponse();

        IssueAssigneeType newAssigneeType = IssueAssigneeType.fromString(
            request.getAssignee() != null ? request.getAssignee().getType() : null);
        if (request.getIssues() != null && newAssigneeType != null) {
            List<IssueShortInfo> success = new ArrayList<>();
            Map<String, ActionRequestFail> allAssignFails = new HashMap<>();
            for (EntityReference issueForAssign : request.getIssues()) {
                OperationResult<String, String[]> result = issueService.assignIssue(issueForAssign.getId(),
                    issueForAssign.getVersion(), newAssigneeType, request.getAssignee().getId(), request.getComment());
                if (result.isFailed()) {
                    String failReason = result.getFailReason()[0];
                    String issueTitle = result.getFailReason()[1];

                    ActionRequestFail failsWithSameReason = allAssignFails.get(failReason);
                    if (failsWithSameReason == null) {
                        failsWithSameReason = new ActionRequestFail();
                        failsWithSameReason.setReason(failReason);
                        allAssignFails.put(failReason, failsWithSameReason);
                    }
                    IssueShortInfo issueFail = new IssueShortInfo(issueForAssign.getId(), issueTitle);
                    failsWithSameReason.getIssues().add(issueFail);
                } else {
                    success.add(new IssueShortInfo(issueForAssign.getId()));
                }
            }
            response.setSuccess(success);
            response.setFailure(new ArrayList<ActionRequestFail>(allAssignFails.values()));
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return response;
    }
}
