/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.LegacyConstraintViolationException;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/creationrules")
public class    CreationRuleResource extends BaseResource {

    private final CreationRuleInfoFactory ruleInfoFactory;
    private final CreationRuleActionInfoFactory actionFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Clock clock;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public CreationRuleResource(CreationRuleInfoFactory ruleInfoFactory, CreationRuleActionInfoFactory actionFactory, PropertyValueInfoService propertyValueInfoService, ConcurrentModificationExceptionFactory conflictFactory, Clock clock, MeteringGroupsService meteringGroupsService) {
        this.ruleInfoFactory = ruleInfoFactory;
        this.actionFactory = actionFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public PagedInfoList getCreationRules(@BeanParam JsonQueryParameters queryParams, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        List<IssueReason> issueReasons = new ArrayList<>();

        if (appKey != null && !appKey.isEmpty() && appKey.equalsIgnoreCase("INS")) {
            issueReasons = new ArrayList<>(getIssueService().query(IssueReason.class)
                    .select(where(ISSUE_TYPE).in(new ArrayList<IssueType>() {{
                        add(getIssueService().findIssueType(IssueTypes.USAGEPOINT_DATA_VALIDATION.getName()).get());
                    }})));
        } else if (appKey != null && !appKey.isEmpty() && appKey.equalsIgnoreCase("MDC")) {
            issueReasons = new ArrayList<>(getIssueService().query(IssueReason.class)
                    .select(where(ISSUE_TYPE).in(new ArrayList<IssueType>() {{
                        add(getIssueService().findIssueType(IssueTypes.DATA_COLLECTION.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.DATA_VALIDATION.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.DEVICE_LIFECYCLE.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.SERVICE_CALL_ISSUE.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.TASK.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.WEB_SERVICE.getName()).get());
                    }})));
        }

        Query<CreationRule> query =
                getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules;
        Condition conditionIssue = where("reason").in(issueReasons);
        if (queryParams.getStart().isPresent()) {
            int from = queryParams.getStart().get() + 1;
            int to = from + queryParams.getLimit().orElse(0);
            rules = query.select(conditionIssue, from, to, Order.ascending("name"));
        } else {
            rules = query.select(conditionIssue, Order.ascending("name"));
        }
        List<CreationRuleInfo> infos = rules.stream().map(ruleInfoFactory::asShortInfo).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("creationRules", infos, queryParams);
    }


    @GET
    @Path("/device/{deviceId}/excludedfromautoclosurerules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public PagedInfoList getAutocloseExclusions(@BeanParam JsonQueryParameters queryParams,
            @PathParam("deviceId") String deviceId) {
        List<CreationRuleInfo> infos = new ArrayList<>();
        final Optional<EndDevice> endDeviceOptional = getMeteringService().findEndDeviceByName(deviceId);
        if (endDeviceOptional.isPresent()) {
            final EndDevice endDevice = endDeviceOptional.get();
            final Instant now = clock.instant();
            final List<EndDeviceGroup> endDeviceGroups = getMeteringGroupsService().findEndDeviceGroups();
            if (endDeviceGroups != null) {
                final List<EndDeviceGroup> groups = endDeviceGroups.stream()
                        .filter(group -> group.isMember(endDevice, now)).collect(Collectors.toList());
                if (groups != null && !groups.isEmpty()) {
                    final List<String> groupIdsList = groups.stream().map(e -> String.valueOf(e.getId()))
                            .collect(Collectors.toList());
                    List<CreationRuleAction> actions = getIssueCreationService()
                            .findActionsByMultiValueProperty(
                                    Arrays.asList(IssueTypes.DATA_COLLECTION, IssueTypes.DATA_VALIDATION,
                                            IssueTypes.DEVICE_LIFECYCLE),
                                    "CloseIssueAction.excludedGroups", groupIdsList);
                    infos = actions.stream().map(action -> action.getRule()).map(ruleInfoFactory::asShortInfo)
                            .collect(Collectors.toList());
                }
            }
        }
        return PagedInfoList.fromCompleteList("creationRules", infos, queryParams);
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
        //TODO - merge with CXO - 4420 and filter only for alarm creation rules
        return getIssueCreationService().findAndLockCreationRuleByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getIssueCreationService().findCreationRuleById(info.id).map(CreationRule::getVersion).orElse(null))
                        .supplier());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    public Response addCreationRule(CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
            setBaseFields(rule, builder);
            setActions(rule, builder);
            setTemplate(rule, builder);
            setExcludedDeviceGroups(rule, builder);
            builder.complete();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    public Response editCreationRule(@PathParam("id") long id, CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule creationRule = findAndLockCreationRule(rule);
            CreationRuleUpdater updater = creationRule.startUpdate();
            setBaseFields(rule, updater);
            updater.removeActions();
            setActions(rule, updater);
            setTemplate(rule, updater);
            setExcludedDeviceGroups(rule, updater);
            updater.complete();
            context.commit();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/activate")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    public Response activateRule(@PathParam("id") long ruleId, CreationRuleInfo info) {
        CreationRule creationRule = findAndLockCreationRule(info);
        if (!creationRule.isActive()) {
            try (TransactionContext context = getTransactionService().getContext()) {
                CreationRuleUpdater updater = creationRule.startUpdate();
                updater.activate();
                updater.complete();
                context.commit();
            }
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_CREATION_RULE)
    public Response deactivateRule(@PathParam("id") long ruleId, CreationRuleInfo info) {
        CreationRule creationRule = findAndLockCreationRule(info);
        if (creationRule.isActive()) {
            try (TransactionContext context = getTransactionService().getContext()) {
                CreationRuleUpdater updater = creationRule.startUpdate();
                updater.deactivate();
                updater.complete();
                context.commit();
            }
        }
        return Response.ok().build();
    }


    @POST
    @Path("/validateaction")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response validateAction(@QueryParam("reason_id") String reasonKey, CreationRuleActionInfo info) {
        CreationRuleActionBuilder actionBuilder = getIssueCreationService().newCreationRule().newCreationRuleAction();
        setAction(info, actionBuilder, reasonKey);
        CreationRuleAction ruleAction = actionBuilder.complete();
        ruleAction.validate();
        return Response.ok(actionFactory.asInfo(ruleAction)).build();
    }


    private void setBaseFields(CreationRuleInfo rule, CreationRuleBuilder builder) {
        builder.setName(rule.name)
                .setComment(rule.comment)
                .setDueInTime(DueInType.fromString(rule.dueIn.type), rule.dueIn.number)
                .setPriority(Priority.get(rule.priority.urgency, rule.priority.impact));
        builder = rule.active ? builder.activate() : builder.deactivate();
        if (rule.issueType != null) {
            getIssueService().findIssueType(rule.issueType.uid).ifPresent(builder::setIssueType);
            if (rule.reason != null) {
                try {
                    builder.setReason(getIssueService().findOrCreateReason(rule.reason.id.equals("12222e48-9afb-4c76-a41e-d3c40f16ac76") ? rule.reason.name : rule.reason.id, getIssueService()
                            .findIssueType(rule.issueType.uid)
                            .get()));
                } catch (VerboseConstraintViolationException ex) {
                    Map<String, String> map = new HashMap<>();
                    map.put("key", "reason");
                    throw new LegacyConstraintViolationException(ex, map);
                }
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

    private void setExcludedDeviceGroups(CreationRuleInfo rule, CreationRuleBuilder builder) {
        if (rule.exclGroups != null) {
            List<EndDeviceGroup> groupList = rule.exclGroups.stream().map(exclGroupInfo -> {
                return meteringGroupsService.findEndDeviceGroup(exclGroupInfo.deviceGroupId).orElse(null);
            }).filter(elem -> elem != null).collect(Collectors.toList());
            builder.setExcludedDeviceGroups(groupList);
        } else {
            builder.setExcludedDeviceGroups(new ArrayList<>());
        }
    }

    private void setActions(CreationRuleInfo rule, CreationRuleBuilder builder) {
        rule.actions.forEach((info) -> setAction(info, builder.newCreationRuleAction(), null));
    }

    private void setAction(CreationRuleActionInfo actionInfo, CreationRuleActionBuilder actionBuilder, String reasonKey) {
        if (actionInfo.phase != null) {
            actionBuilder.setPhase(CreationRuleActionPhase.fromString(actionInfo.phase.uuid));
        }
        if (actionInfo.type != null) {
            Optional<IssueActionType> actionType = getIssueActionService().findActionType(actionInfo.type.id);
            if (actionType.isPresent() && actionType.get().createIssueAction().isPresent() && actionInfo.properties != null) {
                actionBuilder.setActionType(actionType.get());
                for (PropertySpec propertySpec : actionType.get().createIssueAction().get().setReasonKey(reasonKey).getPropertySpecs()) {
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
