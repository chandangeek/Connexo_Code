package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;

@Path("/creationrules")
public class CreationRuleResource extends BaseResource {
    
    private final CreationRuleInfoFactory ruleInfoFactory;
    private final PropertyUtils propertyUtils;

    @Inject
    public CreationRuleResource(CreationRuleInfoFactory ruleInfoFactory, PropertyUtils propertyUtils) {
        this.ruleInfoFactory = ruleInfoFactory;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({ Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_CREATION_RULE })
    public PagedInfoList getCreationRules(@BeanParam JsonQueryParameters queryParams) {
        Query<CreationRule> query = getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules;
        if (queryParams.getStart().isPresent()) {
            int from = queryParams.getStart().get() + 1;
            int to = from + queryParams.getLimit().orElse(0);
            rules = query.select(Condition.TRUE, from, to, Order.ascending("name"));
        } else {
            rules = query.select(Condition.TRUE, Order.ascending("name"));
        }
        List<CreationRuleInfo> infos = rules.stream().map(ruleInfoFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("creationRules", infos, queryParams);
    }

    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({ Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_CREATION_RULE })
    public Response getCreationRule(@PathParam(ID) long id) {
        CreationRule rule = getIssueCreationService().findCreationRuleById(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(ruleInfoFactory.asInfo(rule)).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_CREATION_RULE)
    public Response deleteCreationRule(@PathParam("id") long id, CreationRuleInfo ruleInfo) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule rule = getIssueCreationService().findAndLockCreationRuleByIdAndVersion(id, ruleInfo.version)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
            rule.delete();
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
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule creationRule = getIssueCreationService().findAndLockCreationRuleByIdAndVersion(id, rule.version)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
            CreationRuleUpdater updater = creationRule.startUpdate();
            setBaseFields(rule, updater);
            updater.removeActions();
            setActions(rule, updater);
            setTemplate(rule, updater);
            updater.complete().save();
            context.commit();
        }
        return Response.ok().build();
    }
    
    
    @POST
    @Path("/validateaction")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response validateAction(CreationRuleActionInfo info) {
        CreationRuleActionBuilder actionBuilder = getIssueCreationService().newCreationRule().newCreationRuleAction();
        setAction(info, actionBuilder);
        actionBuilder.complete().validate();
        return Response.ok().build();
    }

    private void setBaseFields(CreationRuleInfo rule, CreationRuleBuilder builder) {
        builder.setName(rule.name)
               .setComment(rule.comment)
               .setDueInTime(DueInType.fromString(rule.dueIn.type), rule.dueIn.number);
        if (rule.issueType != null) {
            getIssueService().findIssueType(rule.issueType.uid).ifPresent(builder::setIssueType);
        }
        if (rule.reason != null) {
            getIssueService().findReason(rule.reason.id).ifPresent(builder::setReason);
        }
    }

    private void setTemplate(CreationRuleInfo rule, CreationRuleBuilder builder) {
        if (rule.template == null) {
            return;
        }
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
        rule.actions.stream().forEach((info) -> setAction(info, builder.newCreationRuleAction()));
    }
    
    private void setAction(CreationRuleActionInfo actionInfo, CreationRuleActionBuilder actionBuilder) {
        if (actionInfo.phase != null) {
            actionBuilder.setPhase(CreationRuleActionPhase.fromString(actionInfo.phase.uuid));
        }
        if (actionInfo.type != null) {
            Optional<IssueActionType> actionType = getIssueActionService().findActionType(actionInfo.type.id);
            if (actionType.isPresent() && actionType.get().createIssueAction().isPresent() && actionInfo.properties != null) {
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
}
