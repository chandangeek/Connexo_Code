package com.elster.jupiter.issue.rest.impl.resource;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.util.conditions.Condition;

@Path("/rules")
public class RuleResource extends BaseResource {
    
    private IssueAssignmentService issueAssignmentService;

    @Inject
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @GET
    @Path("/assign")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_ASSIGNMENT_RULE)
    public Response getAssignmentRules(){
        List<AssignmentRule> assignmentRules = issueAssignmentService.getAssignmentRuleQuery().select(Condition.TRUE);
        return entity(assignmentRules, AssignmentRuleInfo.class).build();
    }

    @GET
    @Path("/templates")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_CREATION_RULE,Privileges.VIEW_CREATION_RULE})
    public Response getCreationRulesTemplates(@BeanParam StandardParametersBean params){
        validateMandatory(params, ISSUE_TYPE);

        List<CreationRuleTemplate> templates = getIssueCreationService().getCreationRuleTemplates();
        List<CreationRuleTemplate> filteredTemplates = new ArrayList<>();
        String expectedIssueType = params.getFirst(ISSUE_TYPE);
        for (CreationRuleTemplate template : templates) {
            if (template.getIssueType().getKey().equals(expectedIssueType)){
                filteredTemplates.add(template);
            }
        }
        return entity(filteredTemplates, CreationRuleTemplateInfo.class).build();
    }

    @GET
    @Path("/templates/{name}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_CREATION_RULE})
    public Response getTemplate(@PathParam("name") String name) {
        CreationRuleTemplate template = getIssueCreationService().findCreationRuleTemplate(name)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return entity(new CreationRuleTemplateInfo(template)).build();
    }
}
