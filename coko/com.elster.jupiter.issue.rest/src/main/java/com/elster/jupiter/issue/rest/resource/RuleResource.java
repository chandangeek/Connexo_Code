package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.rest.response.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfo;
import com.elster.jupiter.issue.rest.response.cep.ParameterInfo;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssignmentRules(){
        List<AssignmentRule> assignmentRules = issueAssignmentService.getAssignmentRuleQuery().select(Condition.TRUE);
        return ok(assignmentRules, AssignmentRuleInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getruletemplates">Get rule templates</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/templates")
    @Produces(MediaType.APPLICATION_JSON)
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
        return ok(filteredTemplates, CreationRuleTemplateInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getruletemplate">Get rule template</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/templates/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTemplate(@PathParam(ID) String id){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return ok(new CreationRuleTemplateInfo(template.get())).build();
    }


    @PUT
    @Path("/templates/{" + ID + "}/parameters")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllParametersValues(@PathParam(ID) String id, Map<String, Object> paramValues){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<ParameterDefinition> parameters = new ArrayList<>();
        for (ParameterDefinition parameter : template.get().getParameterDefinitions().values()) {
            if (parameter.isDependent()) {
                parameters.add(parameter.getValue(paramValues));
            }
        }
        return ok(parameters, ParameterInfo.class).build();
    }


    @PUT
    @Path("/templates/{" + ID + "}/parameters/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleParametersValues(@PathParam(ID) String id, @PathParam(KEY) String key, Map<String, Object> paramValues){
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(id);
        if (!template.isPresent()){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ParameterDefinition parameter = template.get().getParameterDefinitions().get(key);
        if (parameter == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return ok(new ParameterInfo(parameter.getValue(paramValues))).build();
    }
}
