package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.rules.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.rules.AssignmentRuleListInfo;
import com.elster.jupiter.issue.share.entity.Rule;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rules")
public class RuleResource extends BaseResource{

    @GET
    @Path("/assign")
    public Response getAssignmentRules(@BeanParam StandardParametersBean params){
        List<Rule> rulesFromDB = getIssueMainService().query(Rule.class)
                .select(Condition.TRUE, params.getFrom(), params.getTo(), params.getOrder());
        List<AssignmentRuleInfo> ruleInfoList = new ArrayList<>();
        for (Rule rule : rulesFromDB) {
            AssignmentRuleInfo ruleInfo = new AssignmentRuleInfo(rule);
            ruleInfo.setAssignee(new IssueAssigneeInfo(getIssueService().getAssigneeFromRule(rule)));
            ruleInfoList.add(ruleInfo);
        }

        return Response.ok(new AssignmentRuleListInfo(ruleInfoList, params.getStart(), params.getLimit())).build();
    }

}
