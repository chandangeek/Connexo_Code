package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.common.services.ListPager;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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
    private final Thesaurus thesaurus;

    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;
    private final Provider<UsagePointValidationResource> usagePointValidationResourceProvider;
    private final Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider;

    private final UsagePointInfoFactory usagePointInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService,
                              Clock clock,
                              Provider<ChannelResource> channelsOnUsagePointResourceProvider,
                              Provider<RegisterResource> registersOnUsagePointResourceProvider,
                              UsagePointConfigurationService usagePointConfigurationService,
                              Provider<UsagePointValidationResource> usagePointValidationResourceProvider,
                              Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider,
                              CustomPropertySetService customPropertySetService,
                              UsagePointInfoFactory usagePointInfoFactory,
                              CustomPropertySetInfoFactory customPropertySetInfoFactory,
                              ExceptionFactory exceptionFactory,
                              Thesaurus thesaurus,
                              ResourceHelper resourceHelper) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.channelsOnUsagePointResourceProvider = channelsOnUsagePointResourceProvider;
        this.registersOnUsagePointResourceProvider = registersOnUsagePointResourceProvider;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointValidationResourceProvider = usagePointValidationResourceProvider;
        this.usagePointCustomPropertySetResourceProvider = usagePointCustomPropertySetResourceProvider;
        this.customPropertySetService = customPropertySetService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
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
                .from(queryParameters)
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
        info.writeTo(usagePoint);
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
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
    @RolesAllowed({Privileges.Constants.VIEW_SERVICECATEGORY})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/servicecategory")
    public PagedInfoList getServiceCategories(@BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        List<ServiceCategoryInfo> categories = Arrays.stream(ServiceKind.values())
                .map(meteringService::getServiceCategory).flatMap(sc -> sc.isPresent() && sc.get().isActive() ? Stream.of(sc.get()) : Stream.empty()).sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
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

        new RestValidationBuilder()
                .notEmpty(info.mRID, "mRID")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .notEmpty(info.isSdp, "typeOfUsagePoint")
                .notEmpty(info.isVirtual, "typeOfUsagePoint")
                .validate();
        validateSeviceKind(info.serviceCategory);

        if (validate) {
            if (step == 1) {
                usagePointInfoFactory.newUsagePointBuilder(info).validate();
            } else if (step == 2) {
                if(info.techInfo==null){
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
                customPropertySetService.validateCustomPropertySetValues(set.getCustomPropertySet(), customPropertySetInfoFactory
                        .getCustomPropertySetValues(customPropertySetInfo, set.getCustomPropertySet()
                                .getPropertySpecs()));
            }
            return Response.accepted().build();
        }

        UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();

        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            CustomPropertySet set = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class).stream()
                    .filter(rcps -> customPropertySetInfo.id == rcps.getId()).findFirst().orElseThrow(IllegalArgumentException::new).getCustomPropertySet();
            if (!set.isVersioned()) {
                customPropertySetService.setValuesFor(set, usagePoint, customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, set
                        .getPropertySpecs()));
            } else {
                customPropertySetService.setValuesVersionFor(set, usagePoint, customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, set
                        .getPropertySpecs()), Range.atLeast(usagePoint.getInstallationTime()));
            }
        }
        return Response.status(Response.Status.CREATED).entity(usagePointInfoFactory.from(usagePoint)).build();
    }

    private void validateSeviceKind(String serviceKindString){
        if (Arrays.stream(ServiceKind.values()).allMatch(sk -> !sk.name().equals(serviceKindString))){
            throw new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_SERVICE_CATEGORY,"serviceCategory");
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

    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
        Set<ReadingType> readingTypes = new LinkedHashSet<>();
        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (MeterActivation meterActivation : meterActivations) {
            readingTypes.addAll(meterActivation.getReadingTypes());
        }
        return readingTypes;
    }
}