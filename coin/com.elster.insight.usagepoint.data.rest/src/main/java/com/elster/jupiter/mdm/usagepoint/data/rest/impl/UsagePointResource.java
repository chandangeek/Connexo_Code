package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
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

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/usagepoints")
public class UsagePointResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final ServiceCallService serviceCallService;
    private final ServiceCallInfoFactory serviceCallInfoFactory;
    private final Thesaurus thesaurus;

    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;
    private final Provider<UsagePointValidationResource> usagePointValidationResourceProvider;
    private final Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider;
    private final Provider<GoingOnResource> goingOnResourceProvider;

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final LocationInfoFactory locationInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService,
                              Clock clock,
                              ServiceCallService serviceCallService, ServiceCallInfoFactory serviceCallInfoFactory, Provider<ChannelResource> channelsOnUsagePointResourceProvider,
                              Provider<RegisterResource> registersOnUsagePointResourceProvider,
                              UsagePointConfigurationService usagePointConfigurationService,
                              Provider<UsagePointValidationResource> usagePointValidationResourceProvider,
                              Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider,
                              CustomPropertySetService customPropertySetService,
                              UsagePointInfoFactory usagePointInfoFactory,
                              CustomPropertySetInfoFactory customPropertySetInfoFactory,
                              ExceptionFactory exceptionFactory,
                              LocationInfoFactory locationInfoFactory,
                              Thesaurus thesaurus,
                              ResourceHelper resourceHelper,
                              MetrologyConfigurationService metrologyConfigurationService,
                              Provider<GoingOnResource> goingOnResourceProvider) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.channelsOnUsagePointResourceProvider = channelsOnUsagePointResourceProvider;
        this.registersOnUsagePointResourceProvider = registersOnUsagePointResourceProvider;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointValidationResourceProvider = usagePointValidationResourceProvider;
        this.usagePointCustomPropertySetResourceProvider = usagePointCustomPropertySetResourceProvider;
        this.customPropertySetService = customPropertySetService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.locationInfoFactory = locationInfoFactory;
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.goingOnResourceProvider = goingOnResourceProvider;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public PagedInfoList getUsagePoints(@BeanParam JsonQueryParameters queryParameters,
                                        @BeanParam JsonQueryFilter filter,
                                        @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(true, params);

        List<UsagePointInfo> usagePointInfos = ListPager.of(list)
                .from(queryParameters).find()
                .stream()
                .map(usagePointInfoFactory::from)
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

        usagePoint.setGeoCoordinates(usagePointInfoFactory.getGeoCoordinates(info));
        Location location = usagePointInfoFactory.getLocation(info);
        if (location != null){
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

        return usagePointInfoFactory.from(usagePoint);
    }

    @GET
    @Path("/{mrid}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    public UsagePointInfo getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRid);
        UsagePointInfo result = usagePointInfoFactory.from(usagePoint);
        return result;
    }

    @GET
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{mrid}/metrologyconfiguration/linkable")
    public MetrologyConfigurationInfos getLinkableMetrologyConfigurations(@PathParam("mrid") String mrid) {
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
        return new MetrologyConfigurationInfos(configs);
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

        if (usagePoint.getMetrologyConfiguration().isPresent()) {
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

        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = resourceHelper.findAndLockUsagePointMetrologyConfigurationOrThrowException(info.id, info.version);
        usagePoint.apply(usagePointMetrologyConfiguration);
        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            UsagePointPropertySet propertySet = usagePoint.forCustomProperties()
                    .getPropertySet(customPropertySetInfo.id);
            propertySet.setValues(customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo,
                    propertySet.getCustomPropertySet().getPropertySpecs()));
        }
        usagePoint.update();
        return Response.ok().entity(usagePointInfoFactory.from(usagePoint)).build();
    }

    private void validateCasValues(RegisteredCustomPropertySet set, CustomPropertySetInfo customPropertySetInfo) {
        List<PropertySpec> specs = set.getCustomPropertySet().getPropertySpecs();
        customPropertySetService.validateCustomPropertySetValues(set.getCustomPropertySet(), customPropertySetInfoFactory
                .getCustomPropertySetValues(customPropertySetInfo, specs));
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_SERVICECATEGORY})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/servicecategory")
    public PagedInfoList getServiceCategories(@BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
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
    @SuppressWarnings("unchecked")
    public Response createUsagePoint(UsagePointInfo info, @QueryParam("validate") boolean validate, @QueryParam("step") long step, @QueryParam("customPropertySetId") long customPropertySetId) {
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validateGeoCoordinates(validationBuilder, "extendedGeoCoordinates", info.extendedGeoCoordinates);
        validateLocation(validationBuilder, "location", info.extendedLocation);

        validationBuilder.notEmpty(info.mRID, "mRID")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .notEmpty(info.isSdp, "typeOfUsagePoint")
                .notEmpty(info.isVirtual, "typeOfUsagePoint")
                .validate();

        validateSeviceKind(info.serviceCategory);

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
        return Response.status(Response.Status.CREATED).entity(usagePointInfoFactory.from(usagePoint)).build();
    }

    private void validateSeviceKind(String serviceKindString) {
        if (Arrays.stream(ServiceKind.values()).allMatch(sk -> !sk.name().equals(serviceKindString))) {
            throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_SERVICE_CATEGORY, "serviceCategory");
        }
    }

    private void validateGeoCoordinates(RestValidationBuilder validationBuilder, String fieldName, CoordinatesInfo geoCoordinates) {
        String spatialCoordinates = geoCoordinates.spatialCoordinates;
        if (spatialCoordinates == null || spatialCoordinates.length() == 0 || spatialCoordinates.indexOf(":") == -1) {
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

        if (Arrays.asList(parts)
                .stream()
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
    @Path("/{mrid}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRid);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByIdOrThrowException(id);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @Path("/{mrid}/channels")
    public ChannelResource getChannelResource() {
        return channelsOnUsagePointResourceProvider.get();
    }

    @Path("/{mrid}/registers")
    public RegisterResource getRegisterResource() {
        return registersOnUsagePointResourceProvider.get();
    }

    @Path("/{mrid}/validationrulesets")
    public UsagePointValidationResource getUsagePointValidationResource() {
        return usagePointValidationResourceProvider.get();
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
        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (MeterActivation meterActivation : meterActivations) {
            readingTypes.addAll(meterActivation.getReadingTypes());
        }
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
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

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
    public PagedInfoList getServiceCallHistoryFor(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();

        ServiceCallFilter filter = serviceCallInfoFactory.convertToServiceCallFilter(jsonQueryFilter);
        serviceCallService.getServiceCallFinder(filter)
                .stream()
                .filter(serviceCall -> serviceCall.getTargetObject().map(usagePoint::equals).orElse(false))
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

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
}