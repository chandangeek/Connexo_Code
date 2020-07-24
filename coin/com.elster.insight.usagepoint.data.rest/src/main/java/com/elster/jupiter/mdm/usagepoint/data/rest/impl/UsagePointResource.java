/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.audit.ApplicationType;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.audit.rest.AuditI18N;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.UsagePointMeterActivationException;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfoFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.PathVerification;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.TemporalAmountComparator;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/usagepoints")
public class UsagePointResource {

    private static TemporalAmountComparator temporalAmountComparator = new TemporalAmountComparator();
    private static NavigableMap<TemporalAmount, TemporalAmount> validationOverviewLevelsPerIntervalLength = new TreeMap<>(temporalAmountComparator);

    static {
        validationOverviewLevelsPerIntervalLength.put(Duration.ofMinutes(1), Period.ofDays(1));
        validationOverviewLevelsPerIntervalLength.put(Duration.ofMinutes(5), Period.ofWeeks(1));
        validationOverviewLevelsPerIntervalLength.put(Duration.ofMinutes(15), Period.ofWeeks(2));
        validationOverviewLevelsPerIntervalLength.put(Duration.ofHours(1), Period.ofMonths(2));
        validationOverviewLevelsPerIntervalLength.put(Period.ofDays(1), Period.ofYears(1));
        validationOverviewLevelsPerIntervalLength.put(Period.ofWeeks(1), Period.ofYears(2));
        validationOverviewLevelsPerIntervalLength.put(Period.ofMonths(1), Period.ofYears(10));
        validationOverviewLevelsPerIntervalLength.put(Period.ofMonths(3), Period.ofYears(20));
        validationOverviewLevelsPerIntervalLength.put(Period.ofYears(1), Period.ofYears(20));
    }

    private final RestQueryService queryService;
    private final TimeService timeService;
    private final MeteringService meteringService;
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final ServiceCallService serviceCallService;
    private final ServiceCallInfoFactory serviceCallInfoFactory;
    private final Thesaurus thesaurus;

    private final Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider;
    private final Provider<GoingOnResource> goingOnResourceProvider;
    private final Provider<UsagePointOutputResource> usagePointOutputResourceProvider;
    private final Provider<UsagePointCalendarResource> usagePointCalendarResourceProvider;
    private final Provider<UsagePointCalendarHistoryResource> usagePointCalendarHistoryResourceProvider;
    private final Provider<BulkScheduleResource> bulkScheduleResourceProvider;

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final LocationInfoFactory locationInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory;
    private final ResourceHelper resourceHelper;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointDataCompletionService usagePointDataCompletionService;
    private final ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;
    private final HistoricalMeterActivationInfoFactory historicalMeterActivationInfoFactory;
    private final TransactionService transactionService;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ValidationService validationService;
    private final CalendarService calendarService;
    private final MetrologyConfigurationHistoryInfoFactory metrologyConfigurationHistoryInfoFactory;
    private final UsagePointTransitionInfoFactory usagePointTransitionInfoFactory;
    private final NlsService nlsService;
    private final AuditService auditService;
    private final AuditInfoFactory auditInfoFactory;

    @Inject
    public UsagePointResource(
            RestQueryService queryService,
            MeteringService meteringService,
            TimeService timeService,
            Clock clock,
            ServiceCallService serviceCallService,
            ServiceCallInfoFactory serviceCallInfoFactory,
            Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider,
            CustomPropertySetService customPropertySetService,
            Provider<UsagePointCalendarResource> usagePointCalendarResourceProvider,
            UsagePointInfoFactory usagePointInfoFactory,
            CustomPropertySetInfoFactory customPropertySetInfoFactory,
            Provider<UsagePointCalendarHistoryResource> usagePointCalendarHistoryResourceProvider,
            Provider<BulkScheduleResource> bulkScheduleResourceProvider,
            ExceptionFactory exceptionFactory,
            LocationInfoFactory locationInfoFactory,
            ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory,
            Thesaurus thesaurus,
            ResourceHelper resourceHelper,
            MetrologyConfigurationService metrologyConfigurationService,
            UsagePointDataCompletionService usagePointDataCompletionService,
            Provider<GoingOnResource> goingOnResourceProvider,
            Provider<UsagePointOutputResource> usagePointOutputResourceProvider,
            ReadingTypeDeliverableFactory readingTypeDeliverableFactory,
            DataValidationTaskInfoFactory dataValidationTaskInfoFactory,
            HistoricalMeterActivationInfoFactory historicalMeterActivationInfoFactory,
            TransactionService transactionService,
            UsagePointLifeCycleService usagePointLifeCycleService,
            PropertyValueInfoService propertyValueInfoService,
            ValidationService validationService,
            CalendarService calendarService,
            MetrologyConfigurationHistoryInfoFactory metrologyConfigurationHistoryInfoFactory,
            UsagePointTransitionInfoFactory usagePointTransitionInfoFactory,
            NlsService nlsService,
            AuditService auditService,
            AuditInfoFactory auditInfoFactory) {
        this.queryService = queryService;
        this.timeService = timeService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.usagePointCustomPropertySetResourceProvider = usagePointCustomPropertySetResourceProvider;
        this.customPropertySetService = customPropertySetService;
        this.usagePointCalendarResourceProvider = usagePointCalendarResourceProvider;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.usagePointCalendarHistoryResourceProvider = usagePointCalendarHistoryResourceProvider;
        this.bulkScheduleResourceProvider = bulkScheduleResourceProvider;
        this.locationInfoFactory = locationInfoFactory;
        this.validationSummaryInfoFactory = validationSummaryInfoFactory;
        this.thesaurus = thesaurus.join(nlsService.getThesaurus(AuditI18N.COMPONENT_NAME, Layer.REST));
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.goingOnResourceProvider = goingOnResourceProvider;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.usagePointDataCompletionService = usagePointDataCompletionService;
        this.usagePointOutputResourceProvider = usagePointOutputResourceProvider;
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
        this.historicalMeterActivationInfoFactory = historicalMeterActivationInfoFactory;
        this.transactionService = transactionService;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.validationService = validationService;
        this.calendarService = calendarService;
        this.metrologyConfigurationHistoryInfoFactory = metrologyConfigurationHistoryInfoFactory;
        this.usagePointTransitionInfoFactory = usagePointTransitionInfoFactory;
        this.auditService = auditService;
        this.auditInfoFactory = auditInfoFactory;
        this.nlsService = nlsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getUsagePoints(@BeanParam JsonQueryParameters queryParameters,
                                        @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        if (params.containsKey("nameOnly")) {
            Condition condition;
            condition = Condition.TRUE;
            if (!params.isEmpty()) {
                String name = params.getFirst("name");
                if (name != null) {
                    condition = condition.and(where("name").likeIgnoreCase(name.length() == 0 ? "*" : "*" + name + "*"));
                }
            }
            List<UsagePoint> list;
            Query<UsagePoint> query = meteringService.getUsagePointQuery();
            query.setRestriction(Where.where("obsoleteTime").isNull());
            Order order = Order.ascending("upper(name)");
            Optional<Integer> start = queryParameters.getStart();
            Optional<Integer> limit = queryParameters.getLimit();
            if (start.isPresent() && limit.isPresent()) {
                int from = start.get() + 1;
                list = query.select(condition, from, from + limit.get(), order);
            } else {
                list = query.select(condition, order);
            }
            List<UsagePointInfo> usagePointInfos = list
                    .stream()
                    .map(usagePointInfoFactory::asMinimalInfo)
                    .collect(Collectors.toList());

            return PagedInfoList.fromPagedList("usagePoints", usagePointInfos, queryParameters);
        } else {
            List<UsagePoint> list = queryUsagePoints(true, params);

            List<UsagePointInfo> usagePointInfos = ListPager.of(list)
                    .from(queryParameters).find()
                    .stream()
                    .map(usagePointInfoFactory::fullInfoFrom)
                    .collect(Collectors.toList());

            return PagedInfoList.fromPagedList("usagePoints", usagePointInfos, queryParameters);
        }
    }

    private List<UsagePoint> queryUsagePoints(boolean maySeeAny, QueryParameters queryParameters) {
        Query<UsagePoint> query = meteringService.getUsagePointQuery();
        query.setLazy("serviceLocation");
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        return queryService.wrap(query).select(queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.MANAGE_USAGE_POINT_ATTRIBUTES})
    @Transactional
    public UsagePointInfo updateUsagePoint(UsagePointInfo info) {
        UsagePoint usagePoint = resourceHelper.lockUsagePointOrThrowException(info);

        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validateGeoCoordinates(validationBuilder, "extendedGeoCoordinates", info.extendedGeoCoordinates);
        validateLocation(validationBuilder, info.extendedLocation);
        validationBuilder.validate();

        usagePoint.setSpatialCoordinates(usagePointInfoFactory.getGeoCoordinates(info));
        Location location = usagePointInfoFactory.getLocation(info);
        if (location != null) {
            usagePoint.setLocation(location.getId());
        }

        info.writeTo(usagePoint);
        if (!info.techInfo.isEqual(usagePoint, clock)){
            info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
        }

        UsagePointCustomPropertySetExtension extension = usagePoint.forCustomProperties();
        info.customPropertySets
                .forEach(customPropertySetInfo -> {
                    UsagePointPropertySet propertySet = extension.getPropertySet(customPropertySetInfo.id);
                    List<PropertySpec> propertySpecs = propertySet.getCustomPropertySet().getPropertySpecs();
                    CustomPropertySetValues newValues = customPropertySetInfoFactory
                            .getCustomPropertySetValues(customPropertySetInfo, propertySpecs);
                    if (!areEquivalent(newValues, propertySet.getValues(), propertySpecs)) {
                        // TODO: equivalence logic can be better encapsulated in CustomPropertySetValues class,
                        // by adding to its map all default values during construction: old constructors to be deprecated,
                        // new ones to take a Collection of PropertySpec, looks feasible. Pls see CXO-5526.
                        propertySet.setValues(newValues);
                    }
                });

        return usagePointInfoFactory.fullInfoFrom(usagePoint);
    }

    private static boolean areEquivalent(CustomPropertySetValues values1, CustomPropertySetValues values2, Collection<PropertySpec> specs) {
        if (values1.equals(values2)) {
            return true;
        }
        Map<String, PropertySpec> propertySpecMap = specs.stream()
                .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
        return containsOnlyDefaultValues(values1, propertySpecMap) && containsOnlyDefaultValues(values2, propertySpecMap);
    }

    private static boolean containsOnlyDefaultValues(CustomPropertySetValues values, Map<String, PropertySpec> propertySpecMap) {
        return values.propertyNames().stream()
                .allMatch(name -> Objects.equals(values.getProperty(name), getDefaultValue(propertySpecMap.get(name))));
    }

    private static Object getDefaultValue(PropertySpec spec) {
        return Optional.ofNullable(spec.getPossibleValues())
                .map(PropertySpecPossibleValues::getDefault)
                .orElse(null);
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public UsagePointInfo getUsagePoint(@PathParam("name") String name) {
        return usagePointInfoFactory.fullInfoFrom(resourceHelper.findUsagePointByNameOrThrowException(name));
    }

//    @GET
//    @Path("/byMRID/{mrid}")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
//            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
//    public UsagePointInfo getUsagePointbyMRID(@PathParam("mrid") String mrid) {
//        return usagePointInfoFactory.fullInfoFrom(resourceHelper.findUsagePointByMRIDOrThrowException(mrid));
//    }   // this returns an usage point with all its info

    @GET
    @Path("/byMRID/{mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public String getUsagePointNamebyMRID(@PathParam("mrid") String mrid) {
        return usagePointInfoFactory.getUsagePointName(resourceHelper.findUsagePointByMRIDOrThrowException(mrid));
    }  // this returns only the usage point name

    @PUT
    @Path("/{name}/usagepointlifecycle")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public Response updateUsagePointLifeCycle(@PathParam("name") String name, String newLifeCycle) {
        if (newLifeCycle == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "usagePointLifeCycleId");
        }
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);

        State oldLifeCycleState = usagePoint.getState();

        ChangeUsagePointLifeCycleInfo changeUsagePointLifeCycleInfo = new ChangeUsagePointLifeCycleInfo();
        changeUsagePointLifeCycleInfo.usagePointName = usagePoint.getName();
        changeUsagePointLifeCycleInfo.newUsagePointLifeCycle = meteringService.findUsagePointLifeCycle(newLifeCycle)
                .getName();
        changeUsagePointLifeCycleInfo.usagePointState = usagePointInfoFactory.fullInfoFrom(usagePoint).state.name;

        if (!meteringService.findUsagePointLifeCycle(newLifeCycle)
                .getStates()
                .stream()
                .filter(state -> thesaurus.getString(state.getName(), state.getName()).equals(thesaurus.getString(usagePoint.getState().getName(), usagePoint.getState().getName())))
                .findAny()
                .isPresent()) {
            changeUsagePointLifeCycleInfo.errorCode = exceptionFactory.newException(MessageSeeds.NO_STATE_FOR_LIFECYCLE,changeUsagePointLifeCycleInfo.usagePointName,changeUsagePointLifeCycleInfo.usagePointState).getErrorCode();
            return Response.status(Response.Status.BAD_REQUEST).entity(changeUsagePointLifeCycleInfo).build();
        }

        usagePoint.setLifeCycle(newLifeCycle);
        List<State> states = usagePoint.getLifeCycle().getStates();
        if(states.stream().anyMatch(state -> state.getName().equals(oldLifeCycleState.getName()))){
            usagePoint.setState(oldLifeCycleState, Instant.now());
        }else{
            State initialState = states.stream()
                    .filter(State::isInitial)
                    .findFirst()
                    .get();
            usagePoint.setState(initialState, Instant.now());
        }
        usagePoint.update();
        return Response.ok(usagePointInfoFactory.from(usagePoint)).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/metrologyconfiguration/linkable")
    public PagedInfoList getLinkableMetrologyConfigurations(@PathParam("name") String name,
                                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        List<MetrologyConfigurationInfo> configs = metrologyConfigurationService
                .findLinkableMetrologyConfigurations(usagePoint)
                .stream()
                .filter(mc -> mc.getCustomPropertySets().stream().allMatch(RegisteredCustomPropertySet::isEditableByCurrentUser))
                .filter(mc -> usagePoint.getMeterActivations(clock.instant())
                        .stream()
                        .map(MeterActivation::getMeterRole)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .allMatch(role -> mc.getMeterRoles().contains(role)))
                .map(mc -> new MetrologyConfigurationInfo(mc, mc.getCustomPropertySets()
                        .stream()
                        .sorted(Comparator.comparing(rcps -> rcps.getCustomPropertySet().getName(), String.CASE_INSENSITIVE_ORDER))
                        .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyConfigurations", configs, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{name}/meteractivations")
    public Response getMetersOnUsagePoint(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("meterActivations", usagePointInfoFactory.getMetersOnUsagePointFullInfo(usagePoint, auth), queryParameters))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/meterroles")
    public Response getMeterRoles(@BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        List<MeterRole> meterRoles = resourceHelper.getMeterRoles();
        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("meterRoles", meterRoles, queryParameters))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{name}/meterroles")
    public Response getMeterRolesOnUsagePoint(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("meterActivations", usagePointInfoFactory.getMetersOnUsagePointInfo(usagePoint), queryParameters))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{name}/availablemeterroles/{timeStamp}")
    public Response getAvailableMeterRolesOnUsagePoint(@PathParam("name") String name, @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        Instant instant = Instant.ofEpochMilli(timeStamp);
        List<UsagePointMetrologyConfiguration> configurations = usagePoint.getEffectiveMetrologyConfigurations().stream()
                .filter(emc -> emc.isEffectiveAt(instant) || emc.getStart().isAfter(instant))
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return Response.ok()
                    .entity(PagedInfoList.fromCompleteList("meterRoles", configurations.stream()
                            .flatMap(mc -> mc.getMeterRoles().stream())
                            .distinct()
                            .map(MeterRoleInfo::new)
                            .collect(Collectors.toList()), queryParameters))
                    .build();
        } else {
            return Response.ok()
                    .entity(PagedInfoList.fromCompleteList("meterRoles", metrologyConfigurationService.getMeterRoles().stream()
                            .map(MeterRoleInfo::new)
                            .collect(Collectors.toList()), queryParameters))
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{name}/transitions")
    public Response getAvailableTransitions(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);

        List<UsagePointTransition> transitions = resourceHelper.getAvailableTransitions(usagePoint);

        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("transitions", transitions.stream()
                        .map(usagePointTransitionInfoFactory::from).collect(Collectors.toList()), queryParameters))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{name}/transitions/{toStage}")
    public Response getAvailableTransitions(@PathParam("name") String name, @PathParam("toStage") String toStage, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);

        List<UsagePointTransition> transitions = resourceHelper.getAvailableTransitions(usagePoint).stream()
                .filter(usagePointTransition -> usagePointTransition.getTo().getStage().filter(stage -> stage.getName().equals(toStage)).isPresent())
                .collect(Collectors.toList());

        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("transitions", transitions.stream()
                        .map(usagePointTransitionInfoFactory::from).collect(Collectors.toList()), queryParameters))
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{name}/activatemeters")
    public Response activateMeters(@PathParam("name") String name, UsagePointInfo info) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, info.version);
        Stage stage = usagePoint.getState().getStage().get();
        if (!stage.getName().equals(UsagePointStage.PRE_OPERATIONAL.getKey()) && !stage.getName()
                .equals(UsagePointStage.OPERATIONAL.getKey()) && !stage.getName()
                .equals(UsagePointStage.SUSPENDED.getKey())) {
            throw UsagePointMeterActivationException.usagePointIncorrectStage(thesaurus);
        }
        try {
            resourceHelper.performMeterActivations(info, usagePoint);
        } catch (UsagePointMeterActivationException ex) {
            new RestValidationBuilder().addValidationError(new LocalizedFieldValidationException(ex.getMessageSeed(), "meterRole", ex.getMessageArgs())).validate();
        }
        return Response.ok().entity(usagePointInfoFactory.fullInfoFrom(usagePoint)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{name}/metrologyconfiguration")
    public Response linkMetrologyConfigurations(@PathParam("name") String name,
                                                @QueryParam("validate") boolean validate,
                                                @QueryParam("customPropertySetId") long customPropertySetId,
                                                @QueryParam("upVersion") long upVersion,
                                                @QueryParam("createNew") boolean createNew,
                                                MetrologyConfigurationInfo info) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, upVersion);

        RestValidationBuilder validationBuilder = new RestValidationBuilder()
                .notEmpty(info.id, "id")
                .notEmpty(info.name, "name");
        validationBuilder.validate();
        new RestValidationBuilder()
                .on(info.activationTime)
                .check(startTime -> !usagePoint.getInstallationTime().isAfter(info.activationTime))
                .field("metrologyConfiguration")
                .message(MessageSeeds.START_DATE_MUST_BE_GRATER_THAN_UP_CREATED_DATE).test().validate();

        if (validate) {
            if (customPropertySetId > 0) {
                RegisteredCustomPropertySet set = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)
                        .stream()
                        .filter(rcps -> rcps.getId() == customPropertySetId && rcps.isEditableByCurrentUser())
                        .findAny()
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET, customPropertySetId));

                CustomPropertySetInfo customPropertySetInfo = info.customPropertySets.stream()
                        .filter(cps -> cps.id == set.getId())
                        .findFirst()
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET, customPropertySetId));
                validateCasValues(set, customPropertySetInfo);
            } else {
                UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = resourceHelper.findActiveUsagePointMetrologyConfigurationOrThrowException(info.id);
                for (RegisteredCustomPropertySet customPropertySet : usagePointMetrologyConfiguration.getCustomPropertySets()) {
                    Optional<UsagePointVersionedPropertySet> propertySet = usagePoint.forCustomProperties().getAllPropertySets().stream()
                            .filter(usagePointPropertySet -> usagePointPropertySet.getId() == customPropertySet.getId()
                                    && usagePointPropertySet.getCustomPropertySet().isVersioned())
                            .map(UsagePointVersionedPropertySet.class::cast)
                            .findFirst();
                    if (!createNew && !propertySet.filter(cps -> cps.getVersionValues(info.activationTime) != null).isPresent()) {
                        throw new LocalizedFieldValidationException(MessageSeeds.NO_CAS_VERSION_AT_DATE, "metrologyConfiguration", resourceHelper.formatDate(info.activationTime));
                    } else if (createNew && propertySet
                            .filter(cps -> cps.getAllVersionValues().stream().anyMatch(values -> values.getEffectiveRange().lowerEndpoint().isAfter(info.activationTime)))
                            .isPresent()) {
                        throw new LocalizedFieldValidationException(MessageSeeds.ANOTHER_CAS_VERSION_IN_THE_FUTURE, "metrologyConfiguration", resourceHelper.formatDate(info.activationTime));
                    }
                }
            }
            return Response.accepted().build();
        }

        applyMetrologyConfiguration(usagePoint, validationBuilder, info);

        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            if (createNew && customPropertySetInfo.isVersioned) {
                UsagePointVersionedPropertySet propertySet = usagePoint.forCustomProperties().getVersionedPropertySet(customPropertySetInfo.id);
                CustomPropertySetValues existingVersion = propertySet.getVersionValues(info.activationTime);
                if (existingVersion != null) {
                    Range<Instant> range = Range.closedOpen(existingVersion.getEffectiveRange().lowerEndpoint(), info.activationTime);
                    CustomPropertySetValues existingValues = CustomPropertySetValues.emptyDuring(range);
                    copyPropertyValues(propertySet, existingValues, existingVersion);
                    propertySet.setVersionValues(info.activationTime, existingValues);
                    CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(info.activationTime);
                    copyPropertyValues(propertySet, values, customPropertySetInfo);
                    propertySet.setVersionValues(info.activationTime, values);
                } else {
                    Instant startTime = propertySet.getAllVersionValues().stream()
                            .filter(values -> values.getEffectiveRange().hasUpperBound())
                            .map(values -> values.getEffectiveRange().upperEndpoint()).max(Comparator.naturalOrder())
                            .orElse(info.activationTime);
                    CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(startTime);
                    copyPropertyValues(propertySet, values, customPropertySetInfo);
                    propertySet.setVersionValues(startTime, values);
                }
            } else {
                resourceHelper.persistCustomProperties(usagePoint, customPropertySetInfo);
            }
        }
        usagePoint.update();

        return Response.ok().entity(usagePointInfoFactory.fullInfoFrom(usagePoint)).build();
    }

    private void applyMetrologyConfiguration(UsagePoint usagePoint, RestValidationBuilder validationBuilder, MetrologyConfigurationInfo info) {
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = resourceHelper.findActiveUsagePointMetrologyConfigurationOrThrowException(info.id);
        try {
            if (info.purposes != null) {
                usagePoint.apply(usagePointMetrologyConfiguration, info.activationTime, usagePointMetrologyConfiguration
                        .getContracts()
                        .stream()
                        .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                        .filter(metrologyContract -> info.purposes.stream()
                                .anyMatch(purpose -> metrologyContract.getId() == purpose.id))
                        .filter(metrologyContract -> !metrologyContract.isMandatory())
                        .distinct()
                        .collect(Collectors.toSet()));
            } else {
                usagePoint.apply(usagePointMetrologyConfiguration, info.activationTime);
            }
        } catch (LocalizedException ex) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(ex.getMessageSeed(), "metrologyConfiguration", ex.getMessageArgs())).validate();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{usagePointName}/meterroles/{key}/unlink/{timeStamp}")
    public Response unlinkMeterRole(@PathParam("usagePointName") String usagePointName,
                                    @PathParam("key") String key,
                                    @PathParam("timeStamp") Long timeStamp) {

        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        Instant unlinkDate = Instant.ofEpochMilli(timeStamp);
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();

        Optional<MeterActivation> noEndMeterActivation = meterActivations
                .stream()
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole().get().getKey().equals(key) && ma.getInterval().getEnd() == null)
                .findAny();

        if (noEndMeterActivation.isPresent()){
            if(noEndMeterActivation.get().getInterval().getStart().isBefore(unlinkDate) ) {
                UsagePointMeterActivator linker = usagePoint.linkMeters();
                linker.clear(unlinkDate, resourceHelper.findMeterRoleOrThrowException(key));
                linker.complete();
            }else{
                throw exceptionFactory.newException(MessageSeeds.CANNOT_UNLINK_BEFORE_LINK_DATE);
            }
        }
        else {
            meterActivations.stream()
                    .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole().get().getKey().equals(key)).findAny()
                    .map(ma -> {
                        throw exceptionFactory.newException(
                                MessageSeeds.METER_CANNOT_BE_UNLINKED,
                                ma.getMeter().get().getName(),
                                usagePoint.getName(), resourceHelper.formatDate(unlinkDate)
                        );
                    })
                    .orElseThrow(() -> {
                        return exceptionFactory.newException(
                                MessageSeeds.NO_SUCH_METER_ROLE,
                                key
                        );
                    });
        }
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{name}/unlinkmetrologyconfiguration")
    public Response unlinkMetrologyConfigurations(@PathParam("name") String name,
                                                  UsagePointInfo info) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByNameOrThrowException(name, info.version);

        usagePoint.getEffectiveMetrologyConfigurations().stream()
                .findFirst()
                .ifPresent(emc -> emc.close(emc.getStart()));

        return Response.ok().build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Path("/{name}/metrologyconfiguration")
    public Response getLinkedMetrologyConfiguration(@PathParam("name") String name) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        return usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .map(MetrologyConfigurationInfo::new)
                .map(Response.ok()::entity)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.BAD_REQUEST).entity(new MetrologyConfigurationInfo()).build());
    }

    private void validateCasValues(RegisteredCustomPropertySet set, CustomPropertySetInfo customPropertySetInfo) {
        customPropertySetService.validateCustomPropertySetValues(set.getCustomPropertySet(), customPropertySetInfoFactory
                .getCustomPropertySetValues(customPropertySetInfo, set.getCustomPropertySet().getPropertySpecs()));
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_SERVICECATEGORY})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/servicecategory")
    public PagedInfoList getServiceCategories(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCategoryInfo> categories = Arrays.stream(ServiceKind.values())
                .map(meteringService::getServiceCategory)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ServiceCategory::isActive)
                .filter(sc -> sc.getCustomPropertySets().stream().allMatch(RegisteredCustomPropertySet::isEditableByCurrentUser))
                .sorted(Comparator.comparing(ServiceCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(sc -> new ServiceCategoryInfo(sc, sc.getCustomPropertySets()
                        .stream()
                        .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                        .collect(Collectors.toList()), thesaurus))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("categories", categories, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response createUsagePoint(UsagePointInfo info,
                                     @QueryParam("validate") boolean validate,
                                     @QueryParam("step") String step,
                                     @QueryParam("customPropertySetId") long customPropertySetId) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validateUsagePointAttributes(info, validationBuilder);

        if (validate) {
            if ("generalInfo".equals(step)) {
                try (TransactionContext transaction = transactionService.getContext()) {
                    usagePointInfoFactory.newUsagePointBuilder(info).validate();
                }
            } else if ("techInfo".equals(step)) {
                if (info.techInfo == null) {
                    throw exceptionFactory.newException(MessageSeeds.NO_SUCH_TECHNICAL_INFO, info.serviceCategory);
                }
                try (TransactionContext transaction = transactionService.getContext()) {
                    info.techInfo.getUsagePointDetailBuilder(usagePointInfoFactory.newUsagePointBuilder(info)
                            .validate(), clock).validate();
                }
            } else if ("metrologyConfigurationWithMetersInfo".equals(step)) {
                validateMetrologyConfiguration(info, validationBuilder);
            } else if ("calendarTransitionInfo".equals(step)) {
                validateCalendarConfiguration(info, validationBuilder);
            } else if (customPropertySetId > 0) {
                RegisteredCustomPropertySet set = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)
                        .stream()
                        .filter(rcps -> rcps.getId() == customPropertySetId)
                        .findAny()
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET, customPropertySetId));

                CustomPropertySetInfo customPropertySetInfo = info.customPropertySets.stream()
                        .filter(cps -> cps.id == set.getId())
                        .findFirst()
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET, customPropertySetId));
                validateCasValues(set, customPropertySetInfo);
            }
            return Response.accepted().build();
        }
        createUsagePointAndActivateMeters(info, validationBuilder);
        return Response.status(Response.Status.CREATED)
                .build();

    }

    @GET
    @Path("{name}/history/metrologyConfigurations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public PagedInfoList getMetrologyConfigurationsHistory(@PathParam("name") String name, @HeaderParam("Authorization") String auth, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        List<MetrologyConfigurationHistoryInfo> infos = usagePoint.getEffectiveMetrologyConfigurations()
                .stream()
                .map(metrologyConfiguration -> metrologyConfigurationHistoryInfoFactory.from(metrologyConfiguration, usagePoint, auth))
                .sorted(Comparator.comparing((MetrologyConfigurationHistoryInfo info) -> info.start).thenComparing(info -> info.current).reversed())
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("data", infos, queryParameters);
    }

    private void validateServiceKind(String serviceKindString) {
        if (Arrays.stream(ServiceKind.values()).noneMatch(sk -> sk.name().equals(serviceKindString))) {
            throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_SERVICE_CATEGORY, "serviceCategory");
        }
    }

    private void validateGeoCoordinates(RestValidationBuilder validationBuilder, String fieldName, CoordinatesInfo geoCoordinates) {
        String spatialCoordinates = geoCoordinates.spatialCoordinates;
        if (Checks.is(spatialCoordinates).empty() || !spatialCoordinates.contains(":")) {
            return;
        }
        String[] parts = spatialCoordinates.split(":");
        if (parts.length == 0) {
            return;
        }

        if (parts.length != 3) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            return;
        }

        if (Arrays.stream(parts)
                .anyMatch(element -> element.split(",").length > 2
                        || element.split(".").length > 2)) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            return;
        }

        try {
            BigDecimal numericLatitude = new BigDecimal(parts[0].contains(",") ? String.valueOf(parts[0].replace(",", ".")) : parts[0]);
            BigDecimal numericLongitude = new BigDecimal(parts[1].contains(",") ? String.valueOf(parts[1].replace(",", ".")) : parts[1]);
            if (numericLatitude.compareTo(BigDecimal.valueOf(-90)) < 0
                    || numericLatitude.compareTo(BigDecimal.valueOf(90)) > 0
                    || numericLongitude.compareTo(BigDecimal.valueOf(-180)) < 0
                    || numericLongitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            }
        } catch (Exception e) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
        }
    }

    private void validateLocation(RestValidationBuilder validationBuilder, LocationInfo editLocation) {
        if (editLocation.properties != null) {
            List<PropertyInfo> propertyInfos = Arrays.asList(editLocation.properties);
            for (PropertyInfo propertyInfo : propertyInfos) {
                if (propertyInfo.required && ((propertyInfo.propertyValueInfo.value == null)
                        || (propertyInfo.propertyValueInfo.value.toString().isEmpty()))) {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "properties." + propertyInfo.key));
                }
                if(propertyInfo.propertyValueInfo.value != null && !propertyInfo.propertyValueInfo.value.toString().isEmpty()
                            && !PathVerification.validateInputPattern(propertyInfo.propertyValueInfo.value.toString(), HasNotAllowedChars.Constant.SCRIPT_CHARS)) {
                        validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.FORBIDDEN_CHARACTER, "properties." + propertyInfo.key));
                    }
                }
        }
    }

    @GET
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByIdOrThrowException(id);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @Path("/{name}/customproperties")
    public UsagePointCustomPropertySetResource getUsagePointCustomPropertySetResource() {
        return usagePointCustomPropertySetResourceProvider.get();
    }

    @Path("/{name}/calendars")
    public UsagePointCalendarResource getUsagePointCalendarResource() {
        return usagePointCalendarResourceProvider.get();
    }

    @Path("/{name}/history/calendars")
    public UsagePointCalendarHistoryResource getUsagePointCalendarHistoryResource() {
        return usagePointCalendarHistoryResourceProvider.get();
    }

    @Path("/{name}/whatsgoingon")
    public GoingOnResource getGoingOnResource() {
        return goingOnResourceProvider.get();
    }

    @Path("/calendars")
    public BulkScheduleResource getBulkScheduleResource() {
        return bulkScheduleResourceProvider.get();
    }

    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
        Set<ReadingType> readingTypes = new LinkedHashSet<>();
        usagePoint.getMeterActivations()
                .stream()
                .map(MeterActivation::getReadingTypes)
                .flatMap(Collection::stream)
                .forEach(readingTypes::add);
        return readingTypes;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{name}/runningservicecalls")
    public PagedInfoList getServiceCallsFor(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        Set<DefaultState> states = EnumSet.of(
                DefaultState.CREATED,
                DefaultState.SCHEDULED,
                DefaultState.PENDING,
                DefaultState.PAUSED,
                DefaultState.ONGOING,
                DefaultState.WAITING);

        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(usagePoint);
        filter.states = states.stream().map(Enum::name).collect(Collectors.toList());

        List<ServiceCallInfo> serviceCallInfos = serviceCallService.getServiceCallFinder(filter)
                .from(queryParameters)
                .stream()
                .map(serviceCallInfoFactory::summarized)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{name}/runningservicecalls/{id}")
    public Response cancelServiceCall(@PathParam("id") long serviceCallId, ServiceCallInfo info) {
        if ("sclc.default.cancelled".equals(info.state.id)) {
            serviceCallService.getServiceCall(serviceCallId).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{name}/servicecallhistory")
    public PagedInfoList getServiceCallHistoryFor(@PathParam("name") String name,
                                                  @BeanParam JsonQueryParameters queryParameters,
                                                  @BeanParam JsonQueryFilter jsonQueryFilter,
                                                  @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();

        ServiceCallFilter filter = serviceCallInfoFactory.convertToServiceCallFilter(jsonQueryFilter, appKey);
        filter.targetObjects.add(usagePoint);
        serviceCallService.getServiceCallFinder(filter)
                .from(queryParameters)
                .stream()
                .map(serviceCallInfoFactory::summarized)
                .forEach(serviceCallInfos::add);

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{name}/servicecalls")
    public Response cancelServiceCallsFor(@PathParam("name") String name, ServiceCallInfo serviceCallInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        if (serviceCallInfo.state == null) {
            throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
        }
        if (DefaultState.CANCELLED.getKey().equals(serviceCallInfo.state.id)) {
            serviceCallService.cancelServiceCallsFor(usagePoint);
            return Response.accepted().build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET
    @Transactional
    @Path("/locations/{locationId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public Response getLocationAttributes(@PathParam("locationId") long locationId) {
        return Response.ok(locationInfoFactory.from(locationId)).build();
    }

    @GET
    @Transactional
    @Path("/{name}/validationSummary")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getDataValidationStatistics(@PathParam("name") String name,
                                                     @QueryParam("purposeId") long contractId,
                                                     @QueryParam("periodId") long periodId,
                                                     @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        Instant now = clock.instant();
        Range<Instant> interval = timeService.findRelativePeriod(periodId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_RELATIVEPERIOD_FOR_ID, periodId))
                .getOpenClosedInterval(ZonedDateTime.ofInstant(now, clock.getZone()));
        Range<Instant> upToNow = Range.atMost(now);
        if (interval.isConnected(upToNow)) {
            interval = interval.intersection(upToNow); // find out interval in past, or else throw an exception
            if (!interval.isEmpty()) {
                List<ChannelDataValidationSummaryInfo> result = usagePointDataCompletionService
                        .getDataCompletionStatistics(effectiveMC, metrologyContract, interval).entrySet().stream()
                        .map(channelEntry -> validationSummaryInfoFactory.from(channelEntry.getKey(), channelEntry.getValue()))
                        .sorted(Comparator.comparing(info -> info.name))
                        .collect(Collectors.toList());
                return PagedInfoList.fromCompleteList("outputs", result, queryParameters);
            }
        }
        throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
    }

    @GET
    @Path("/{name}/validationSummaryPeriods")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getDataValidationStatisticsRelativePeriods(@PathParam("name") String name,
                                                                    @QueryParam("purposeId") long contractId,
                                                                    @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(effectiveMC, contractId);
        TemporalAmount max = metrologyContract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .filter(ReadingType::isRegular)
                .map(ReadingType::getIntervalLength)
                .flatMap(Functions.asStream())
                .max(temporalAmountComparator)
                .orElse(Period.ofYears(1));//return max period to cover the use-case with registers when there is no intervalLength
        List<IdWithNameInfo> infos = getRelativePeriodsDefaultOnTop(max).stream()
                .map(rp -> new IdWithNameInfo(rp.getId(), findTranslatedRelativePeriod(rp.getName())))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("relativePeriods", infos, queryParameters);
    }

    private String findTranslatedRelativePeriod(String name) {
        return defaultRelativePeriodDefinitionTranslationKeys()
                .filter(e -> e.getDefaultFormat().equals(name))
                .findFirst()
                .map(e -> thesaurus.getFormat(e).format())
                .orElse(name);
    }

    private Stream<TranslationKey> defaultRelativePeriodDefinitionTranslationKeys() {
        return Stream.concat(
                Stream.of(DefaultRelativePeriodDefinition.RelativePeriodTranslationKey.values()),
                Stream.of(GasDayOptions.RelativePeriodTranslationKey.values()));
    }

    private List<? extends RelativePeriod> getRelativePeriodsDefaultOnTop(TemporalAmount intervalLength) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        TemporalAmount targetIntervalLength = getValidationOverviewIntervalLength(intervalLength).orElse(intervalLength);
        return fetchRelativePeriods().stream()
                .sorted((rp1, rp2) -> {
                    int cmp = Long.compare(getIntervalLengthDifference(rp1, targetIntervalLength, now), getIntervalLengthDifference(rp2, targetIntervalLength, now));
                    if (cmp == 0) {
                        return Long.compare(
                                Math.abs(rp1.getOpenClosedZonedInterval(now).upperEndpoint().toInstant().toEpochMilli() - now.toInstant().toEpochMilli()),
                                Math.abs(rp2.getOpenClosedZonedInterval(now).upperEndpoint().toInstant().toEpochMilli() - now.toInstant().toEpochMilli()));
                    } else {
                        return cmp;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<? extends RelativePeriod> fetchRelativePeriods() {
        return timeService.getRelativePeriodQuery().select(Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name")
                .isEqualTo(DefaultTranslationKey.RELATIVE_PERIOD_CATEGORY_USAGE_POINT_VALIDATION_OVERVIEW.getKey()));
    }

    private Optional<TemporalAmount> getValidationOverviewIntervalLength(TemporalAmount intervalLength) {
        Map.Entry<TemporalAmount, TemporalAmount> targetInterval = validationOverviewLevelsPerIntervalLength.floorEntry(intervalLength);
        return Optional.ofNullable(targetInterval).map(Map.Entry::getValue);
    }

    private long getIntervalLengthDifference(RelativePeriod relativePeriod, TemporalAmount targetIntervalLength, ZonedDateTime now) {
        Range<ZonedDateTime> interval = relativePeriod.getOpenClosedZonedInterval(now);
        ZonedDateTime relativePeriodStart = interval.lowerEndpoint();
        if (now.isAfter(relativePeriodStart)) {
            long relativePeriodLength = getIntervalLength(interval.intersection(Range.atMost(now)));
            long targetLength = getIntervalLength(Range.openClosed(relativePeriodStart, relativePeriodStart.plus(targetIntervalLength)));
            return Math.abs(targetLength - relativePeriodLength);
        }
        // period starts in the future, this is not what we need,
        // return max interval length to move such relative period to the bottom of the list
        return Long.MAX_VALUE;
    }

    private long getIntervalLength(Range<ZonedDateTime> interval) {
        return interval.upperEndpoint().toInstant().toEpochMilli() - interval.lowerEndpoint().toInstant().toEpochMilli();
    }

    @Path("/{name}/purposes")
    public UsagePointOutputResource getUsagePointOutputResource() {
        return usagePointOutputResourceProvider.get();
    }

    @GET
    @Path("/{name}/deliverables")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationDeliverables(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = resourceHelper
                .findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        List<ReadingTypeDeliverablesInfo> deliverables = effectiveMC
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(mc -> effectiveMC.getChannelsContainer(mc, clock.instant()).isPresent())
                .map(MetrologyContract::getDeliverables)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ReadingTypeDeliverable::getName))
                .map(readingTypeDeliverableFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deliverables", deliverables, queryParameters);
    }

    @GET
    @Path("/{name}/history/meters")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getHistoryOfMeters(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        List<HistoricalMeterActivationInfo> meterActivationInfoList = usagePoint.getMeterActivations().stream()
                .map(ma -> historicalMeterActivationInfoFactory.from(ma, usagePoint, auth))
                .sorted(Comparator.comparing((HistoricalMeterActivationInfo info) -> info.start)
                        .thenComparing((HistoricalMeterActivationInfo info) -> info.current).reversed()
                        .thenComparing(info -> info.meterRole).thenComparing(info -> info.meter))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("meters", meterActivationInfoList, queryParameters);
    }

    @GET
    @Path("/{name}/history/audit")
    @RolesAllowed({com.elster.jupiter.audit.security.Privileges.Constants.VIEW_AUDIT_LOG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getDeviceAuditTrail(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return PagedInfoList.fromPagedList("audit", auditService.getAuditTrail(getDeviceAuditTrailFilter(filter, name))
                .from(queryParameters)
                .stream()
                .map(audit -> auditInfoFactory.from(audit, thesaurus))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Path("/{name}/validationtasks")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getValidationTasksOnUsagePoint(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);

        List<DataValidationTask> validationTasks = validationService.findValidationTasks()
                .stream()
                .filter(task -> task.getQualityCodeSystem().equals(QualityCodeSystem.MDM))
                .collect(Collectors.toList());

        List<DataValidationTaskInfo> dataValidationTasks = validationTasks
                .stream()
                .map(DataValidationTask::getUsagePointGroup)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .filter(usagePointGroup -> isMember(usagePoint, usagePointGroup))
                .flatMap(usagePointGroup -> validationTasks.stream()
                        .filter(dataValidationTask -> dataValidationTask.getUsagePointGroup()
                                .filter(usagePointGroup::equals)
                                .isPresent()))
                .map(dataValidationTaskInfoFactory::asInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("dataValidationTasks", dataValidationTasks, queryParameters);
    }

    @GET
    @Path("/{name}/metrologyconfiguration/privileges")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUsagePointPrivileges(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> privileges = UsagePointPrivileges
                .getUsagePointPrivilegesBasedOnStage(resourceHelper.findUsagePointByNameOrThrowException(name))
                .stream()
                .map(privilege -> new IdWithNameInfo(null, privilege))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("privileges", privileges, queryParameters);
    }

    private boolean isMember(UsagePoint usagePoint, UsagePointGroup usagePointGroup) {
        return !meteringService.getUsagePointQuery()
                .select(Where.where("id").isEqualTo(usagePoint.getId())
                        .and(ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "id")), 1, 1)
                .isEmpty();
    }

    private void performLifeCycleTransition(UsagePointTransitionInfo transitionToPerform, UsagePoint usagePoint, RestValidationBuilder validationBuilder) {
        if (transitionToPerform != null) {
            UsagePointTransition transition = resourceHelper.findUsagePointTransitionOrThrowException(transitionToPerform.id);
            checkRequiredProperties(transitionToPerform, validationBuilder);
            checkTransitionTime(transitionToPerform.effectiveTimestamp, usagePoint);
            Map<String, Object> propertiesMap = DecoratedStream.decorate(transition.getActions().stream())
                    .flatMap(microAction -> microAction.getPropertySpecs().stream())
                    .distinct(PropertySpec::getName)
                    .collect(Collectors.toMap(PropertySpec::getName, propertySpec -> this.propertyValueInfoService.findPropertyValue(propertySpec, transitionToPerform.properties)));

            usagePointLifeCycleService.scheduleTransition(usagePoint, transition, transitionToPerform.effectiveTimestamp, "INS", propertiesMap);
        }
    }

    private void checkRequiredProperties(UsagePointTransitionInfo transitionInfo, RestValidationBuilder validationBuilder) {
        transitionInfo.properties.stream()
                .filter(propertyInfo -> propertyInfo.required
                        && (propertyInfo.getPropertyValueInfo() == null
                        || propertyInfo.getPropertyValueInfo().getValue() == null || Checks.is(String.valueOf(propertyInfo.getPropertyValueInfo().getValue()))
                        .emptyOrOnlyWhiteSpace()))
                .findFirst()
                .ifPresent(propertyInfo -> {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, propertyInfo.key));
                    validationBuilder.validate();
                });
    }

    private void checkTransitionTime(Instant transitionTime, UsagePoint usagePoint) {
        if (!transitionTime.isAfter(usagePoint.getInstallationTime())) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_TRANSITION_TIME, "effectiveTimestamp",
                    resourceHelper.formatDate(usagePoint.getInstallationTime().atZone(usagePoint.getZoneId())));
        }
    }

    private void checkMeterRolesActivationTime(List<MeterRoleInfo> meterRoles, Instant installationTime, RestValidationBuilder restValidationBuilder) {
        meterRoles.stream()
                .filter(role -> !Checks.is(role.meter).emptyOrOnlyWhiteSpace())
                .filter(role -> role.activationTime.isBefore(installationTime))
                .findFirst()
                .ifPresent(meterRoleInfo -> {
                    restValidationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_ACTIVATION_TIME_OF_METER_ROLE, "meter.role." + meterRoleInfo.name));
                    restValidationBuilder.validate();
                });
    }

    private void validateUsagePointAttributes(UsagePointInfo info, RestValidationBuilder validationBuilder) {
        validateGeoCoordinates(validationBuilder, "extendedGeoCoordinates", info.extendedGeoCoordinates);
        validateLocation(validationBuilder, info.extendedLocation);

        validationBuilder.notEmpty(info.name, "name")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .notEmpty(info.isSdp, "typeOfUsagePoint")
                .notEmpty(info.isVirtual, "typeOfUsagePoint")
                .validate();

        validateServiceKind(info.serviceCategory);
    }

    private void validateMetrologyConfiguration(UsagePointInfo info, RestValidationBuilder validationBuilder) {
        try (TransactionContext transaction = transactionService.getContext()) {
            if (info.metrologyConfiguration != null) {
                UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
                info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
                resourceHelper.activateMeters(info, usagePoint);
                checkMeterRolesActivationTime(info.metrologyConfiguration.meterRoles, usagePoint.getInstallationTime(), validationBuilder);
                UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = (UsagePointMetrologyConfiguration) resourceHelper.findMetrologyConfigurationOrThrowException(info.metrologyConfiguration.id);
                if (usagePointMetrologyConfiguration.requiresCalendarOnUsagePoint()) {
                    this.addFakeCalendar(usagePoint, usagePointMetrologyConfiguration.getEventSets());
                }
                usagePoint.apply(usagePointMetrologyConfiguration, info.metrologyConfiguration.activationTime,
                        getActiveContractsFromConfiguration(usagePointMetrologyConfiguration, info.metrologyConfiguration));
                resourceHelper.activateMeters(info, usagePoint);
            }
        } catch (UsagePointManagementException ex) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(ex.getMessageSeed(), "metrologyConfiguration", ex.getMessageArgs())).validate();
            validationBuilder.addValidationError(new LocalizedFieldValidationException(ex.getMessageSeed(), "meterRole", ex.getMessageArgs())).validate();
        } catch (UsagePointMeterActivationException ex) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(ex.getMessageSeed(), "meterRole", ex.getMessageArgs())).validate();
        }
    }

    private void validateCalendarConfiguration(UsagePointInfo info, RestValidationBuilder validationBuilder) {
        try (TransactionContext transaction = transactionService.getContext()) {
            if (info.metrologyConfiguration != null) {
                UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
                info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
                resourceHelper.activateMeters(info, usagePoint);
                checkMeterRolesActivationTime(info.metrologyConfiguration.meterRoles, usagePoint.getInstallationTime(), validationBuilder);
                UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = (UsagePointMetrologyConfiguration) resourceHelper.findMetrologyConfigurationOrThrowException(info.metrologyConfiguration.id);
                addCalendars(info, usagePoint);
                usagePoint.apply(usagePointMetrologyConfiguration, info.metrologyConfiguration.activationTime);
                resourceHelper.activateMeters(info, usagePoint);
            }
            else if(!info.calendars.isEmpty()){
                info.calendars.forEach(calendarInfo -> {
                    Calendar calendar = calendarService.findCalendar(calendarInfo.calendar.id).orElse(null);
                    Instant start = calendarInfo.fromTime == 0 ? clock.instant() : Instant.ofEpochMilli(calendarInfo.fromTime);
                    if (Year.from(LocalDateTime.ofInstant(start, clock.getZone())).isBefore(calendar.getStartYear())) {
                        throw new LocalizedFieldValidationException(MessageSeeds.CANNOT_LINK_BEFORE_START, "activationOn");
                    } else if(start.isBefore(Instant.ofEpochMilli(info.installationTime))){
                        throw new LocalizedFieldValidationException(MessageSeeds.CANNOT_LINK_BEFORE_CREATION_DATE, "activationOn");
                    }
                });
            }
        }
    }

    private void addCalendars(UsagePointInfo info, UsagePoint usagePoint) {
        info.calendars.forEach(calendarInfo -> {
            Calendar calendar = calendarService.findCalendar(calendarInfo.calendar.id).orElse(null);
            Instant start = Instant.ofEpochMilli(calendarInfo.fromTime);
            if (calendarInfo.immediately) {
                usagePoint.getUsedCalendars().addCalendar(calendar);
            } else {
                if (Year.from(LocalDateTime.ofInstant(start, clock.getZone())).isBefore(calendar.getStartYear())) {
                    throw new LocalizedFieldValidationException(MessageSeeds.CANNOT_LINK_BEFORE_START, "activationOn");
                } else if(start.isBefore(Instant.ofEpochMilli(info.installationTime))){
                    throw new LocalizedFieldValidationException(MessageSeeds.CANNOT_LINK_BEFORE_CREATION_DATE, "activationOn");
                }
                usagePoint.getUsedCalendars().addCalendar(calendar, start);
            }
        });
    }

    private Set<MetrologyContract> getActiveContractsFromConfiguration(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration, MetrologyConfigurationInfo info) {
        if (info.purposes != null) {
            return usagePointMetrologyConfiguration.getContracts()
                    .stream()
                    .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                    .filter(metrologyContract -> info.purposes.stream()
                            .anyMatch(purpose -> metrologyContract.getId() == purpose.id))
                    .filter(metrologyContract -> !metrologyContract.isMandatory())
                    .distinct()
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    private void addFakeCalendar(UsagePoint usagePoint, List<EventSet> eventSets) {
        eventSets
                .stream()
                .map(EventSet::getEvents)
                .flatMap(Collection::stream)
                .mapToLong(Event::getCode)
                .findAny()
                .ifPresent(randomEventCode -> this.addFakeCalendar(usagePoint, eventSets, randomEventCode));
    }

    private void addFakeCalendar(UsagePoint usagePoint, List<EventSet> eventSets, long randomEventCode) {
        String fakeDayTypeName = "always";
        String fakePeriodName = "allyear";
        usagePoint
                .getUsedCalendars()
                .addCalendar(this.calendarService
                        .newCalendar("Fake", this.getTimeOfUseCategory(), Year.now(this.clock), this.createFakeEventSet(eventSets))
                        .description("Generated to satisfy the metrology configuration as part of validating one step of the wizzard that creates a usage point")
                        .newDayType(fakeDayTypeName).eventWithCode(randomEventCode).startsFrom(LocalTime.MIDNIGHT).add()
                        .addPeriod(fakePeriodName, fakeDayTypeName, fakeDayTypeName, fakeDayTypeName, fakeDayTypeName, fakeDayTypeName, fakeDayTypeName, fakeDayTypeName)
                        .on(MonthDay.of(Month.JANUARY, 1)).transitionTo(fakePeriodName)
                        .add());
    }

    private EventSet createFakeEventSet(List<EventSet> eventSets) {
        CalendarService.EventSetBuilder eventSetBuilder = this.calendarService.newEventSet("Fake");
        eventSets
                .stream()
                .map(EventSet::getEvents)
                .flatMap(Collection::stream)
                .map(Event::getCode)
                .distinct()
                .forEach(code -> eventSetBuilder.addEvent("fake" + code).withCode(code).add());
        return eventSetBuilder.add();
    }

    private Category getTimeOfUseCategory() {
        return this.calendarService
                .findCategoryByName(OutOfTheBoxCategory.TOU.name())
                .orElseThrow(() -> new IllegalStateException("Calendar service installer failure, time of use category is missing"));
    }

    private UsagePoint createUsagePointAndActivateMeters(UsagePointInfo info, RestValidationBuilder validationBuilder) {
        UsagePoint usagePoint;
        try (TransactionContext transaction = transactionService.getContext()) {
            UsagePointBuilder usagePointBuilder = usagePointInfoFactory.newUsagePointBuilder(info);
            usagePoint = usagePointBuilder.create();
            info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
            addCalendars(info, usagePoint);
            resourceHelper.activateMeters(info, usagePoint);
            if (info.metrologyConfiguration != null) {
                UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = (UsagePointMetrologyConfiguration) resourceHelper
                        .findMetrologyConfigurationOrThrowException(info.metrologyConfiguration.id);
                usagePoint.apply(usagePointMetrologyConfiguration, info.metrologyConfiguration.activationTime,
                        getActiveContractsFromConfiguration(usagePointMetrologyConfiguration, info.metrologyConfiguration));
            }

            info.customPropertySets.forEach(customPropertySetInfo -> resourceHelper.persistCustomProperties(usagePoint, customPropertySetInfo));
            usagePoint.update();

            performLifeCycleTransition(info.transitionToPerform, usagePoint, validationBuilder);
            transaction.commit();
        }

        return usagePoint;
    }

    private void copyPropertyValues(UsagePointVersionedPropertySet from, CustomPropertySetValues to, CustomPropertySetInfo info) {
        copyPropertyValues(from, to, customPropertySetInfoFactory.getCustomPropertySetValues(info, from.getCustomPropertySet().getPropertySpecs()));
    }

    private void copyPropertyValues(UsagePointVersionedPropertySet from, CustomPropertySetValues to, CustomPropertySetValues fromValues) {
        from.getCustomPropertySet().getPropertySpecs().stream()
                .map(PropertySpec::getName)
                .forEach(propertyName -> to.setProperty(propertyName, fromValues.getProperty(propertyName)));
    }
    private AuditTrailFilter getDeviceAuditTrailFilter(JsonQueryFilter filter, String name) {
        AuditTrailFilter auditFilter = auditService.newAuditTrailFilter(ApplicationType.MDM_APPLICATION_KEY);
        if (filter.hasProperty("changedOnFrom")) {
            auditFilter.setChangedOnFrom(filter.getInstant("changedOnFrom"));
        }
        if (filter.hasProperty("changedOnTo")) {
            auditFilter.setChangedOnTo(filter.getInstant("changedOnTo"));
        }
        if (filter.hasProperty("users")) {
            auditFilter.setChangedBy(filter.getStringList("users"));
        }
        auditFilter.setCategories(filter.getStringList(AuditDomainType.USAGEPOINT.name()));
        auditFilter.setDomainContexts(
                Arrays.stream(AuditDomainContextType.values())
                        .filter(auditDomainContextType -> auditDomainContextType.domainType() == AuditDomainType.USAGEPOINT)
                        .collect(Collectors.toList())
        );
        auditFilter.setDomain(resourceHelper.findUsagePointByNameOrThrowException(name).getId());
        return auditFilter;
    }
}