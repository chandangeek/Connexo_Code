package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.response.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfo;
import com.elster.jupiter.issue.rest.response.cep.ParameterInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/rules")
public class RuleResource extends BaseResource{
    private IssueAssignmentService issueAssignmentService;

    @Inject
    public void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getassignmentrules">Get assignment rules</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/assign")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_ASSIGNMENT_RULE)
    public Response getAssignmentRules(){
        List<AssignmentRule> assignmentRules = issueAssignmentService.getAssignmentRuleQuery().select(Condition.TRUE);
        return entity(assignmentRules, AssignmentRuleInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getruletemplates">Get rule templates</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'<br />
     * <b>Optional parameters</b>: none<br />
     */
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
            if (template.getIssueType().equals(expectedIssueType)){
                filteredTemplates.add(template);
            }
        }
        return entity(filteredTemplates, CreationRuleTemplateInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getruletemplate">Get rule template</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/templates/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_CREATION_RULE,Privileges.VIEW_CREATION_RULE})
    public Response getTemplate(@PathParam(ID) String id){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return entity(new CreationRuleTemplateInfo(template.get())).build();
    }


    @PUT
    @Path("/templates/{" + ID + "}/parameters")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    public Response getAllParametersValues(@PathParam(ID) String id, Map<String, Object> paramValues){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<ParameterDefinition> parameters = new ArrayList<>();
        if (template.get().getParameterDefinitions() != null) {
            for (ParameterDefinition parameter : template.get().getParameterDefinitions().values()) {
                parameters.add(parameter.getValue(paramValues));
            }
        }
        return entity(parameters, ParameterInfo.class).build();
    }


    @PUT
    @Path("/templates/{" + ID + "}/parameters/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    public Response getSingleParametersValues(@PathParam(ID) String id, @PathParam(KEY) String key, Map<String, Object> paramValues){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (template.get().getParameterDefinitions() == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ParameterDefinition parameter = template.get().getParameterDefinitions().get(key);
        if (parameter == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return entity(new ParameterInfo(parameter.getValue(paramValues))).build();
    }
}
