package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;

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
import java.util.ArrayList;
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

    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;
    private final Provider<UsagePointValidationResource> usagePointValidationResourceProvider;
    private final Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider;

    private final UsagePointInfoFactory usagePointInfoFactory;

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
                              CustomPropertySetInfoFactory customPropertySetInfoFactory) {
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
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
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
    @RolesAllowed({Privileges.Constants.ADMIN_OWN, Privileges.Constants.ADMIN_ANY})
    @Transactional
    public Response updateUsagePoint(@PathParam("id") String id, UsagePointInfo info, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = new UpdateUsagePointTransaction(info, securityContext, meteringService, clock).perform();

        MetrologyConfiguration metrologyConfiguration = null;
        if (info.metrologyConfiguration != null) {
            metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(info.metrologyConfiguration.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        }

        Optional<MetrologyConfiguration> currentMC = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(usagePoint);
        if (currentMC.isPresent() && info.metrologyConfiguration != null) {
            //check for update
            if (currentMC.get().getId() != info.metrologyConfiguration.id) {
                usagePointConfigurationService.link(usagePoint, metrologyConfiguration);
            }
        } else if (currentMC.isPresent() && info.metrologyConfiguration == null) {
            //unlink
            usagePointConfigurationService.unlink(usagePoint, metrologyConfiguration);
        } else {
            //new link
            if (info.metrologyConfiguration != null) {
                usagePointConfigurationService.link(usagePoint, metrologyConfiguration);
            }
        }
        return Response.status(Response.Status.CREATED).entity(getUsagePoint(info.mRID, securityContext)).build();
    }

    @GET
    @Path("/{mrid}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public UsagePointInfo getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        UsagePointInfo result = usagePointInfoFactory.from(usagePoint);
        result.addServiceLocationInfo();
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
                .map(sc -> new ServiceCategoryInfo(sc, sc.getCustomPropertySets().stream().map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo).collect(Collectors.toList())))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("categories", categories, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY})
    @Transactional
    @SuppressWarnings("unchecked")
    public Response createUsagePoint(UsagePointInfo info) {
        UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        info.techInfo.getUsagePointDetailBuilder(usagePoint,clock).build();

        for (CustomPropertySetInfo customPropertySetInfo : info.customPropertySets) {
            CustomPropertySet set = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class).stream()
                    .filter(rcps -> customPropertySetInfo.id == rcps.getId()).findFirst().orElseThrow(IllegalArgumentException::new).getCustomPropertySet();
            customPropertySetService.setValuesFor(set,usagePoint,customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, set.getPropertySpecs()));
        }
        return Response.status(Response.Status.CREATED).entity(usagePointInfoFactory.from(usagePoint)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY})
    @Path("/validation")
    @Transactional
    @SuppressWarnings("unchecked")
    public Response validateUsagePoint(UsagePointInfo info, @QueryParam("step") long step) {

        if(step==1) {
            usagePointInfoFactory.newUsagePointBuilder(info).validate();
        }
        else if (step==2){
            info.techInfo.getUsagePointDetailBuilder(usagePointInfoFactory.newUsagePointBuilder(info).validate(),clock).validate();
        } else {
            RegisteredCustomPropertySet set = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class).stream().filter(rcps -> rcps.getId()==step).findAny().orElseThrow(IllegalArgumentException::new);

            CustomPropertySetInfo customPropertySetInfo = info.customPropertySets.stream().filter(cps -> cps.id==set.getId()).findFirst().orElseThrow(IllegalArgumentException::new);
            customPropertySetService.validateCustomPropertySetValues(set.getCustomPropertySet(),customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, set.getCustomPropertySet().getPropertySpecs()));
        }

        return Response.accepted().build();
    }

    @GET
    @Path("/{mrid}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }


    @GET
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
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

    @Path("/{mrid}/properties")
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

    private UsagePoint fetchUsagePoint(long id, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(id);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal())
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("INS", Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal())
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("INS", Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }
}