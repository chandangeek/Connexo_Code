package com.elster.jupiter.issue.rest.impl.resource;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;

@Path("/creationrules")
public class CreationRuleResource extends BaseResource {
    
    PropertyUtils propertyUtils = new PropertyUtils();

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({ Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_CREATION_RULE })
    public Response getCreationRules(@BeanParam StandardParametersBean params, @QueryParam("start") Integer start) {
        Query<CreationRule> query = getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules = start != null ? query.select(Condition.TRUE, params.getFrom(), params.getTo()) : query.select(Condition.TRUE);
        return entity(rules, CreationRuleInfo.class, params.getStart(), params.getLimit()).build();
    }

    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({ Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_CREATION_RULE })
    public Response getCreationRule(@PathParam(ID) long id) {
        CreationRule rule = getIssueCreationService().findCreationRuleById(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return entity(new CreationRuleInfo(rule)).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    public Response deleteCreationRule(@PathParam("id") long id, CreationRuleInfo ruleInfo) {
        CreationRule rule = getIssueCreationService().findAndLockCreationRuleByIdAndVersion(id, ruleInfo.version)
                .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        try (TransactionContext context = getTransactionService().getContext()) {
            rule.delete();
            getIssueCreationService().reReadRules();
            context.commit();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response addCreationRule(CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
            setBaseFields(rule, builder);
            setActions(rule, builder);
            setTemplate(rule, builder);
            builder.complete().save();
            
            getIssueCreationService().reReadRules();
            
            context.commit();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response editCreationRule(@PathParam("id") long id, CreationRuleInfo rule){
        CreationRule creationRule = getIssueCreationService().findAndLockCreationRuleByIdAndVersion(id, rule.version)
                .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRuleUpdater updater = creationRule.startUpdate();
            setBaseFields(rule, updater);
            updater.removeActions();
            setActions(rule, updater);
            setTemplate(rule, updater);
            updater.complete().save();
            
            getIssueCreationService().reReadRules();
            
            context.commit();
        }
        return Response.ok().build();
    }

    private void setTemplate(CreationRuleInfo rule, CreationRuleBuilder builder) {
        Optional<CreationRuleTemplate> template = getIssueCreationService().findCreationRuleTemplate(rule.template.name);
        if (template.isPresent()) {
            builder.setTemplate(template.get().getName());
            Map<String, Object> properties = new LinkedHashMap<>();
            for (PropertySpec propertySpec : template.get().getPropertySpecs()) {
                Object value = propertyUtils.findPropertyValue(propertySpec, rule.properties);
                if (value != null) {
                    properties.put(propertySpec.getName(), value);
                }
            }
            builder.setProperties(properties);
        }
    }

    private void setActions(CreationRuleInfo rule, CreationRuleBuilder builder) {
        for (CreationRuleActionInfo actionInfo : rule.actions) {
            CreationRuleActionBuilder actionBuilder = builder.newCreationRuleAction();
            actionBuilder.setPhase(CreationRuleActionPhase.fromString(actionInfo.phase.uuid));
            Optional<IssueActionType> actionType = getIssueActionService().findActionType(actionInfo.type.id);
            if (actionType.isPresent() && actionType.get().createIssueAction().isPresent()) {
                actionBuilder.setActionType(actionType.get());
                for (PropertySpec propertySpec : actionType.get().createIssueAction().get().getPropertySpecs()) {
                    Object value = propertyUtils.findPropertyValue(propertySpec, actionInfo.properties);
                    if (value != null) {
                        actionBuilder.addProperty(propertySpec.getName(), value);
                    }
                }
                actionBuilder.complete();
            }
        }
    }

    private void setBaseFields(CreationRuleInfo rule, CreationRuleBuilder builder) {
        builder.setName(rule.name)
               .setComment(rule.comment)
               .setDueInTime(DueInType.fromString(rule.dueIn.type), rule.dueIn.number);
        
        getIssueService().findReason(rule.reason.id).ifPresent(reason -> builder.setReason(reason));
    }
}
