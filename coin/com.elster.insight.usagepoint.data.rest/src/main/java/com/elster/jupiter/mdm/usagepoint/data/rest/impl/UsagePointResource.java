package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Effectivity;
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
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.TemporalAmountComparator;

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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

@Path("/usagepoints")
public class UsagePointResource {

    private static TemporalAmountComparator temporalAmountComparator = new TemporalAmountComparator();
    private static TreeMap<TemporalAmount, TemporalAmount> validationOverviewLevelsPerIntervalLength = new TreeMap<>(temporalAmountComparator);

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

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final LocationInfoFactory locationInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory;
    private final ResourceHelper resourceHelper;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointDataService usagePointDataService;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService, TimeService timeService,
                              Clock clock,
                              ServiceCallService serviceCallService, ServiceCallInfoFactory serviceCallInfoFactory,
                              Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider,
                              CustomPropertySetService customPropertySetService,
                              UsagePointInfoFactory usagePointInfoFactory,
                              CustomPropertySetInfoFactory customPropertySetInfoFactory,
                              ExceptionFactory exceptionFactory,
                              LocationInfoFactory locationInfoFactory,
                              ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory,
                              Thesaurus thesaurus,
                              ResourceHelper resourceHelper,
                              MetrologyConfigurationService metrologyConfigurationService,
                              UsagePointDataService usagePointDataService,
                              Provider<GoingOnResource> goingOnResourceProvider,
                              Provider<UsagePointOutputResource> usagePointOutputResourceProvider) {
        this.queryService = queryService;
        this.timeService = timeService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.usagePointCustomPropertySetResourceProvider = usagePointCustomPropertySetResourceProvider;
        this.customPropertySetService = customPropertySetService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.locationInfoFactory = locationInfoFactory;
        this.validationSummaryInfoFactory = validationSummaryInfoFactory;
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.goingOnResourceProvider = goingOnResourceProvider;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.usagePointDataService = usagePointDataService;
        this.usagePointOutputResourceProvider = usagePointOutputResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getUsagePoints(@BeanParam JsonQueryParameters queryParameters,
                                        @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(true, params);

        List<UsagePointInfo> usagePointInfos = ListPager.of(list)
                .from(queryParameters).find()
                .stream()
                .map(usagePointInfoFactory::fullInfoFrom)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("usagePoints", usagePointInfos, queryParameters);
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
    @RolesAllowed({Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public UsagePointInfo updateUsagePoint(@PathParam("id") String id, UsagePointInfo info) {
        UsagePoint usagePoint = resourceHelper.lockUsagePointOrThrowException(info);

        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validateGeoCoordinates(validationBuilder, "extendedGeoCoordinates", info.extendedGeoCoordinates);
        validateLocation(validationBuilder, "location", info.extendedLocation);
        validationBuilder.validate();

        usagePoint.setSpatialCoordinates(usagePointInfoFactory.getGeoCoordinates(info));
        Location location = usagePointInfoFactory.getLocation(info);
        if (location != null) {
            usagePoint.setLocation(location.getId());
        }

        info.writeTo(usagePoint);
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();

        UsagePointCustomPropertySetExtension extension = usagePoint.forCustomProperties();
        info.customPropertySets.stream()
                .forEach(customPropertySetInfo -> {
                    UsagePointPropertySet propertySet = extension.getPropertySet(customPropertySetInfo.id);
                    propertySet.setValues(customPropertySetInfoFactory
                            .getCustomPropertySetValues(customPropertySetInfo, propertySet.getCustomPropertySet().getPropertySpecs()));
                });

        return usagePointInfoFactory.fullInfoFrom(usagePoint);
    }

    @GET
    @Path("/{mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public UsagePointInfo getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        return usagePointInfoFactory.fullInfoFrom(resourceHelper.findUsagePointByMrIdOrThrowException(mRid));
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{mrid}/metrologyconfiguration/linkable")
    public PagedInfoList getLinkableMetrologyConfigurations(@PathParam("mrid") String mrid,
                                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        List<MetrologyConfigurationInfo> configs = metrologyConfigurationService
                .findLinkableMetrologyConfigurations(usagePoint)
                .stream()
                .filter(mc -> !mc.getCustomPropertySets().stream().anyMatch(cas -> !cas.isEditableByCurrentUser()))
                .map(mc -> new MetrologyConfigurationInfo(mc, mc.getCustomPropertySets()
                        .stream()
                        .sorted((a, b) -> a.getCustomPropertySet().getName().compareToIgnoreCase(b.getCustomPropertySet().getName()))
                        .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyConfigurations", configs, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Transactional
    @Path("/{mrid}/meteractivations")
    public Response getMetersOnUsagePoint(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        return Response.ok()
                .entity(PagedInfoList.fromCompleteList("meterActivations", usagePointInfoFactory.getMetersOnUsagePointInfo(usagePoint, auth), queryParameters))
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{mrid}/activatemeters")
    public Response activateMeters(@PathParam("mrid") String mrid, UsagePointInfo info) {
        UsagePoint usagePoint = resourceHelper.findAndLockUsagePointByMrIdOrThrowException(mrid, info.version);
        if (info.meterActivations != null && !info.meterActivations.isEmpty()) {
            UsagePointMeterActivator linker = usagePoint.linkMeters();
            info.meterActivations
                    .stream()
                    .filter(meterActivation -> meterActivation.meterRole != null && !Checks.is(meterActivation.meterRole.id).emptyOrOnlyWhiteSpace())
                    .forEach(meterActivation -> {
                        MeterRole meterRole = resourceHelper.findMeterRoleOrThrowException(meterActivation.meterRole.id);
                        if (meterActivation.meter != null && !Checks.is(meterActivation.meter.mRID).emptyOrOnlyWhiteSpace()) {
                            Meter meter = resourceHelper.findMeterOrThrowException(meterActivation.meter.mRID);
                            linker.activate(meter, meterRole);
                        } else {
                            linker.clear(meterRole);
                        }
                    });
            linker.complete();
        }
        return Response.ok().entity(usagePointInfoFactory.fullInfoFrom(usagePoint)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    @Path("/{mrid}/metrologyconfiguration")
    public Response linkMetrologyConfigurations(@PathParam("mrid") String mrid,
                                                @QueryParam("validate") boolean validate,
                                                @QueryParam("customPropertySetId") long customPropertySetId,
                                                @QueryParam("upVersion") long upVersion,
                                                MetrologyConfigurationInfo info) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);

        if (usagePoint.getEffectiveMetrologyConfiguration(usagePoint.getInstallationTime()).isPresent()) {
            throw resourceHelper.throwUsagePointLinkedException(mrid);
        }

        usagePoint = resourceHelper.findAndLockUsagePointByMrIdOrThrowException(mrid, upVersion);

        new RestValidationBuilder()
                .notEmpty(info.id, "id")
                .notEmpty(info.name, "name")
                .validate();

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
            }
            return Response.accepted().build();
        }

        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = resourceHelper.findAndLockActiveUsagePointMetrologyConfigurationOrThrowException(info.id, info.version);
        usagePoint.apply(usagePointMetrologyConfiguration, usagePoint.getInstallationTime());
        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            UsagePointPropertySet propertySet = usagePoint.forCustomProperties()
                    .getPropertySet(customPropertySetInfo.id);
            propertySet.setValues(customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo,
                    propertySet.getCustomPropertySet().getPropertySpecs()));
        }
        usagePoint.update();
        return Response.ok().entity(usagePointInfoFactory.fullInfoFrom(usagePoint)).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Path("/{mrid}/metrologyconfiguration")
    public Response getLinkedMetrologyConfiguration(@PathParam("mrid") String mrid) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
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
                .flatMap(sc -> sc.isPresent() && sc.get().isActive() ? Stream.of(sc.get()) : Stream.empty())
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .filter(sc -> !sc.getCustomPropertySets().stream().anyMatch(rcps -> !rcps.isEditableByCurrentUser()))
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
    @Transactional
    public Response createUsagePoint(UsagePointInfo info,
                                     @QueryParam("validate") boolean validate,
                                     @QueryParam("step") long step,
                                     @QueryParam("customPropertySetId") long customPropertySetId) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validateGeoCoordinates(validationBuilder, "extendedGeoCoordinates", info.extendedGeoCoordinates);
        validateLocation(validationBuilder, "location", info.extendedLocation);

        validationBuilder.notEmpty(info.mRID, "mRID")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .notEmpty(info.isSdp, "typeOfUsagePoint")
                .notEmpty(info.isVirtual, "typeOfUsagePoint")
                .validate();

        validateServiceKind(info.serviceCategory);

        if (validate) {
            if (step == 1) {
                usagePointInfoFactory.newUsagePointBuilder(info).validate();
            } else if (step == 2) {
                if (info.techInfo == null) {
                    throw exceptionFactory.newException(MessageSeeds.NO_SUCH_TECHNICAL_INFO, info.serviceCategory);
                }
                info.techInfo.getUsagePointDetailBuilder(usagePointInfoFactory.newUsagePointBuilder(info)
                        .validate(), clock).validate();
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

        UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();

        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            UsagePointPropertySet propertySet = usagePoint.forCustomProperties()
                    .getPropertySet(customPropertySetInfo.id);
            propertySet.setValues(customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo,
                    propertySet.getCustomPropertySet().getPropertySpecs()));
        }
        return Response.status(Response.Status.CREATED).entity(usagePointInfoFactory.fullInfoFrom(usagePoint)).build();
    }

    private void validateServiceKind(String serviceKindString) {
        if (Arrays.stream(ServiceKind.values()).allMatch(sk -> !sk.name().equals(serviceKindString))) {
            throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_SERVICE_CATEGORY, "serviceCategory");
        }
    }

    private void validateGeoCoordinates(RestValidationBuilder validationBuilder, String fieldName, CoordinatesInfo geoCoordinates) {
        String spatialCoordinates = geoCoordinates.spatialCoordinates;
        if (spatialCoordinates == null || spatialCoordinates.length() == 0 || !spatialCoordinates.contains(":")) {
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
            BigDecimal numericElevation = new BigDecimal(parts[2]);
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

    private void validateLocation(RestValidationBuilder validationBuilder, String fieldName, LocationInfo editLocation) {
        if (editLocation.properties != null) {
            List<PropertyInfo> propertyInfos = Arrays.asList(editLocation.properties);
            for (PropertyInfo propertyInfo : propertyInfos) {
                if (propertyInfo.required && ((propertyInfo.propertyValueInfo.value == null) || (propertyInfo.propertyValueInfo.value.toString().length() == 0))) {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "properties." + propertyInfo.key));
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

    @Path("/{mrid}/customproperties")
    public UsagePointCustomPropertySetResource getUsagePointCustomPropertySetResource() {
        return usagePointCustomPropertySetResourceProvider.get();
    }

    @Path("/{mRID}/whatsgoingon")
    public GoingOnResource getGoingOnResource() {
        return goingOnResourceProvider.get();
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
    @Path("{mRID}/runningservicecalls")
    public PagedInfoList getServiceCallsFor(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Set<DefaultState> states = EnumSet.of(
                DefaultState.CREATED,
                DefaultState.SCHEDULED,
                DefaultState.PENDING,
                DefaultState.PAUSED,
                DefaultState.ONGOING,
                DefaultState.WAITING);

        serviceCallService.findServiceCalls(usagePoint, states)
                .stream()
                .map(serviceCallInfoFactory::summarized)
                .forEach(serviceCallInfos::add);

        return PagedInfoList.fromCompleteList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{mRID}/runningservicecalls/{id}")
    public Response cancelServiceCall(@PathParam("mRID") String mrid, @PathParam("id") long serviceCallId, ServiceCallInfo info) {
        if (info.state.id.equals("sclc.default.cancelled")) {
            serviceCallService.getServiceCall(serviceCallId).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("{mRID}/servicecallhistory")
    public PagedInfoList getServiceCallHistoryFor(@PathParam("mRID") String mrid,
                                                  @BeanParam JsonQueryParameters queryParameters,
                                                  @BeanParam JsonQueryFilter jsonQueryFilter) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();

        ServiceCallFilter filter = serviceCallInfoFactory.convertToServiceCallFilter(jsonQueryFilter);
        serviceCallService.getServiceCallFinder(filter)
                .stream()
                .filter(serviceCall -> serviceCall.getTargetObject().map(usagePoint::equals).orElse(false))
                .map(serviceCallInfoFactory::summarized)
                .forEach(serviceCallInfos::add);

        return PagedInfoList.fromCompleteList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{mRID}/servicecalls")
    public Response cancelServiceCallsFor(@PathParam("mRID") String mrid, ServiceCallInfo serviceCallInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
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
    @Path("/{mRID}/validationSummary")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getDataValidationStatistics(@PathParam("mRID") String mRID,
                                                     @QueryParam("purposeId") long contractId,
                                                     @QueryParam("periodId") long periodId,
                                                     @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
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
                interval = usagePoint.getMeterActivations().stream()
                        .map(Effectivity::getInterval)
                        .map(Interval::toOpenClosedRange)
                        .filter(interval::isConnected)
                        .map(interval::intersection)
                        .filter(not(Range::isEmpty))
                        // TODO: fix this if MeterActivations over a gap are supported; if so, RangeSet is needed instead of Range::span
                        .reduce(Range::span) // find out interval with data
                        .orElse(Range.openClosed(now, now)); // or else an empty interval to get empty summary
                List<ChannelDataValidationSummaryInfo> result = usagePointDataService
                        .getValidationSummary(effectiveMC, metrologyContract, interval).entrySet().stream()
                        .map(channelEntry -> validationSummaryInfoFactory.from(channelEntry.getKey(), channelEntry.getValue()))
                        .collect(Collectors.toList());
                return PagedInfoList.fromCompleteList("outputs", result, queryParameters);
            }
        }
        throw exceptionFactory.newException(MessageSeeds.RELATIVEPERIOD_IS_IN_THE_FUTURE, periodId);
    }

    @GET
    @Path("/{mRID}/validationSummaryPeriods")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getDataValidationStatisticsRelativePeriods(@PathParam("mRID") String mRID,
                                                                    @QueryParam("purposeId") long contractId,
                                                                    @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);
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
                .map(rp -> new IdWithNameInfo(rp.getId(), rp.getName()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("relativePeriods", infos, queryParameters);
    }

    private List<? extends RelativePeriod> getRelativePeriodsDefaultOnTop(TemporalAmount intervalLength) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        TemporalAmount targetIntervalLength = getValidationOverviewIntervalLength(intervalLength).orElse(intervalLength);
        return fetchRelativePeriods().stream()
                .sorted(Comparator.comparing(relativePeriod -> getIntervalLengthDifference(relativePeriod, targetIntervalLength, now)))
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

    private long getIntervalLengthDifference(RelativePeriod relativePeriod, TemporalAmount targetIntervalLength, ZonedDateTime referenceTime) {
        Range<ZonedDateTime> interval = relativePeriod.getOpenClosedZonedInterval(referenceTime);
        ZonedDateTime relativePeriodStart = interval.lowerEndpoint();
        if (referenceTime.isAfter(relativePeriodStart)) {
            long relativePeriodLength = getIntervalLength(interval.intersection(Range.atMost(referenceTime)));
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

    @Path("/{mRID}/purposes")
    public UsagePointOutputResource getUsagePointOutputResource() {
        return usagePointOutputResourceProvider.get();
    }
        
    @GET
    @Path("/{mRID}/deliverables")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationDeliverables(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);

        List<ReadingTypeDeliverablesInfo> deliverables = usagePoint.getCurrentEffectiveMetrologyConfiguration().get()
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory) //Temporary. Should be replaced by active/inactive check
                .map(MetrologyContract::getDeliverables)
                .flatMap(List::stream)
                .map(ReadingTypeDeliverablesInfo::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deliverables", deliverables, queryParameters);
    }
}
