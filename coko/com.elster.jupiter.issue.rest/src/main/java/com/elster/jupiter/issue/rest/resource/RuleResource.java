package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.rules.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.rules.AssignmentRuleListInfo;
import com.elster.jupiter.issue.share.entity.Rule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rules")
public class RuleResource extends BaseResource{
    private IssueAssignmentService issueAssignmentService;

    @Inject
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @GET
    @Path("/assign")
    public Response getAssignmentRules(@BeanParam StandardParametersBean params){
        List<Rule> rulesFromDB = issueAssignmentService.getAssignmentRuleQuery().select(Condition.TRUE);
        List<AssignmentRuleInfo> ruleInfoList = new ArrayList<>();
        for (Rule rule : rulesFromDB) {
            AssignmentRuleInfo ruleInfo = new AssignmentRuleInfo(rule);
            ruleInfo.setAssignee(new IssueAssigneeInfo(rule.getAssignee()));
            ruleInfoList.add(ruleInfo);
        }

        return Response.ok(new AssignmentRuleListInfo(ruleInfoList, params.getStart(), params.getLimit())).build();
    }

}
