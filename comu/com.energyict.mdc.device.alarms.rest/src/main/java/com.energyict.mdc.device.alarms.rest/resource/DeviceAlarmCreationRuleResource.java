package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.security.Privileges;

import com.google.common.collect.Range;

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
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/creationrules")
public class DeviceAlarmCreationRuleResource extends BaseAlarmResource {

    private final CreationRuleInfoFactory ruleInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final TimeService timeService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceAlarmCreationRuleResource(CreationRuleInfoFactory ruleInfoFactory, PropertyValueInfoService propertyValueInfoService, ConcurrentModificationExceptionFactory conflictFactory, TimeService timeService, Clock clock, Thesaurus thesaurus) {
        this.ruleInfoFactory = ruleInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
        this.timeService = timeService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE, Privileges.Constants.VIEW_ALARM_CREATION_RULE})
    public PagedInfoList getCreationRules(@BeanParam JsonQueryParameters queryParams) {
        IssueType alarmType = getIssueService().findIssueType("devicealarm").orElse(null);
        List<IssueReason> alarmReasons = getIssueService().query(IssueReason.class)
                .select(where(ISSUE_TYPE).isEqualTo(alarmType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query =
                getIssueService().getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules;
        Condition conditionIssue = where("reason").in(alarmReasons);
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE, Privileges.Constants.VIEW_ALARM_CREATION_RULE})
    public Response getCreationRule(@PathParam(ID) long id) {
        CreationRule rule =
                getIssueService().getIssueCreationService().findCreationRuleById(id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return
                Response.ok(ruleInfoFactory.asInfo(rule)).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE)
    public Response deleteCreationRule(@PathParam("id") long id, CreationRuleInfo info) {
        info.id = id;
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRule rule = findAndLockCreationRule(info);
            rule.delete();
            context.commit();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response addCreationRule(CreationRuleInfo rule) {
        try (TransactionContext context = getTransactionService().getContext()) {
            CreationRuleBuilder builder = getIssueService().getIssueCreationService().newCreationRule();
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
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ALARM_CREATION_RULE)
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

    @GET
    @Path("/relativeperiods")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.issue.security.Privileges.Constants.ADMINISTRATE_CREATION_RULE, com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE})
    public PagedInfoList getRelativePeriods(@BeanParam JsonQueryParameters queryParameters) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        List<RelativePeriod> relativePeriods = fetchRelativePeriods().stream()
                .sorted((RelativePeriod rp1, RelativePeriod rp2) -> {
                    int cmp = Long.compare(getIntervalLengthDifference(rp1, now), getIntervalLengthDifference(rp2, now));
                    if (cmp == 0) {
                        return Long.compare(
                                Math.abs(rp1.getOpenClosedZonedInterval(now).upperEndpoint().toInstant().toEpochMilli() - now.toInstant().toEpochMilli()),
                                Math.abs(rp2.getOpenClosedZonedInterval(now).upperEndpoint().toInstant().toEpochMilli() - now.toInstant().toEpochMilli()));
                    } else {
                        return cmp;
                    }
                })
                .collect(Collectors.toList());

        List<IdWithNameInfo> infos = relativePeriods.stream()
                .map(period -> new IdWithNameInfo(period.getId(), findTranslatedRelativePeriod(period.getName())))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("relativePeriods", infos, queryParameters);
    }


    @POST
    @Path("/validateaction")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response validateAction(CreationRuleActionInfo info) {
        CreationRuleActionBuilder actionBuilder = getIssueService().getIssueCreationService().newCreationRule().newCreationRuleAction();
        setAction(info, actionBuilder);
        actionBuilder.complete().validate();
        return Response.ok().build();
    }

    private CreationRule findAndLockCreationRule(CreationRuleInfo info) {
        return getIssueService().getIssueCreationService().findAndLockCreationRuleByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getIssueService().getIssueCreationService().findCreationRuleById(info.id).map(CreationRule::getVersion).orElse(null))
                        .supplier());
    }

    private void setBaseFields(CreationRuleInfo rule, CreationRuleBuilder builder) {
        builder.setName(rule.name)
                .setComment(rule.comment)
                .setDueInTime(DueInType.fromString(rule.dueIn.type), rule.dueIn.number)
                .setPriority(Priority.get(rule.priority.urgency, rule.priority.impact));
        if (rule.reason != null) {
            builder.setReason(getIssueService().findOrCreateReason(rule.reason.id, getIssueService().findIssueType("devicealarm")
                    .get()));
            builder.setIssueType(getIssueService().findIssueType("devicealarm").get());
        }
    }

    private void setTemplate(CreationRuleInfo rule, CreationRuleBuilder builder) {
        if (rule.template == null) {
            return;
        }
        Optional<CreationRuleTemplate> template = getIssueService().getIssueCreationService().findCreationRuleTemplate(rule.template.name);
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
        rule.actions.forEach((info) -> setAction(info, builder.newCreationRuleAction()));
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

    private List<? extends RelativePeriod> fetchRelativePeriods() {
        return timeService.getRelativePeriodQuery().select(Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name")
                .isEqualTo(ModuleConstants.ALARM_RELATIVE_PERIOD_CATEGORY));
    }

    private String findTranslatedRelativePeriod(String name) {
        return defaultRelativePeriodDefinitionTranslationKeys()
                .filter(e -> e.getDefaultFormat().equals(name))
                .findFirst()
                .map(e -> thesaurus.getFormat(e).format())
                .orElse(name);
    }

    private Stream<TranslationKey> defaultRelativePeriodDefinitionTranslationKeys() {
        return Stream.of(DefaultRelativePeriodDefinition.RelativePeriodTranslationKey.values());
    }

    private long getIntervalLengthDifference(RelativePeriod relativePeriod, ZonedDateTime now) {
        Range<ZonedDateTime> interval = relativePeriod.getOpenClosedZonedInterval(now);
        ZonedDateTime relativePeriodStart = interval.lowerEndpoint();
        if (now.isAfter(relativePeriodStart)) {
            return getIntervalLength(interval.intersection(Range.atMost(now)));
        }
        return Long.MAX_VALUE;
    }

    private long getIntervalLength(Range<ZonedDateTime> interval) {
        return interval.upperEndpoint().toInstant().toEpochMilli() - interval.lowerEndpoint().toInstant().toEpochMilli();
    }
}
