package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.Priority;
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
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.cert.CertPathValidatorException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/creationrules")
public class CreationRuleResource extends BaseResource {

    private final CreationRuleInfoFactory ruleInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public CreationRuleResource(CreationRuleInfoFactory ruleInfoFactory, PropertyValueInfoService propertyValueInfoService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.ruleInfoFactory = ruleInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public PagedInfoList getCreationRules(@BeanParam JsonQueryParameters queryParams) {
        IssueType alarmType = getIssueService().findIssueType("devicealarm").orElse(null);
        List<IssueReason> issueReasons = getIssueService().query(IssueReason.class)
                .select(where(ISSUE_TYPE).isEqualTo(alarmType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query =
                getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules;
        Condition conditionIssue = where("reason").in(issueReasons).not();
        if (queryParams.getStart().isPresent()) {
            int from = queryParams.getStart().get() + 1;
            int to = from + queryParams.getLimit().orElse(0);
            rules = query.select(conditionIssue, from, to, Order.ascending("name"));
        } else {
            rules = query.select(conditionIssue, Order.ascending("name"));
        }
        List<CreationRuleInfo> infos = rules.stream().map(ruleInfoFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("creationRules", infos, queryParams);
    }


    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public Response getCreationRule(@PathParam(ID) long id) {
        CreationRule rule =
                getIssueCreationService().findCreationRuleById(id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return
                Response.ok(ruleInfoFactory.asInfo(rule)).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    public Response deleteCreationRule(@PathParam("id") long id, CreationRuleInfo info) {
        info.id = id;
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule rule = findAndLockCreationRule(info);
            rule.delete();
            context.commit();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public CreationRule findAndLockCreationRule(CreationRuleInfo info) {
        return getIssueCreationService().findAndLockCreationRuleByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getIssueCreationService().findCreationRuleById(info.id).map(CreationRule::getVersion).orElse(null))
                        .supplier());
    }

    @POST
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response addCreationRule(CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
            setBaseFields(rule, builder);
            setActions(rule, builder);
            setTemplate(rule, builder);
            builder.complete();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response editCreationRule(@PathParam("id") long id, CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule creationRule = findAndLockCreationRule(rule);
            CreationRuleUpdater updater = creationRule.startUpdate();
            setBaseFields(rule, updater);
            updater.removeActions();
            setActions(rule, updater);
            setTemplate(rule, updater);
            updater.complete();
            context.commit();
        }
        return Response.ok().build();
    }


    @POST
    @Path("/validateaction")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response validateAction(CreationRuleActionInfo info) {
        CreationRuleActionBuilder actionBuilder = getIssueCreationService().newCreationRule().newCreationRuleAction();
        setAction(info, actionBuilder);
        actionBuilder.complete().validate();
        return Response.ok().build();
    }

    private void setBaseFields(CreationRuleInfo rule, CreationRuleBuilder builder) {
        builder.setName(rule.name)
                .setComment(rule.comment)
                .setDueInTime(DueInType.fromString(rule.dueIn.type), rule.dueIn.number)
                .setPriority(Priority.get(rule.priority.urgency, rule.priority.impact));
        if (rule.issueType != null) {
            getIssueService().findIssueType(rule.issueType.uid).ifPresent(builder::setIssueType);
            if (rule.reason != null) {
                builder.setReason(getIssueService().findOrCreateReason(rule.reason.id, getIssueService().findIssueType(rule.issueType.uid)
                        .get()));
            }
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
                Object value = propertyValueInfoService.findPropertyValue(propertySpec, rule.properties);
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
                    Object value = propertyValueInfoService.findPropertyValue(propertySpec, actionInfo.properties);
                    if (value != null) {
                        actionBuilder.addProperty(propertySpec.getName(), value);
                    }
                }
                actionBuilder.complete();
            }
        }
    }
}
