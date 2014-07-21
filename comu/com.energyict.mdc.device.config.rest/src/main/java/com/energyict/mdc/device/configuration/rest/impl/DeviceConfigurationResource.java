package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import javax.inject.Inject;
import javax.inject.Provider;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ValidationService validationService;
    private final Provider<RegisterConfigurationResource> registerConfigurationResourceProvider;
    private final Provider<ConnectionMethodResource> connectionMethodResourceProvider;
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;
    private final Provider<LoadProfileConfigurationResource> loadProfileConfigurationResourceProvider;
    private final Provider<SecurityPropertySetResource> securityPropertySetResourceProvider;
    private final Provider<ComTaskEnablementResource> comTaskEnablementResourceProvider;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceConfigurationResource(ResourceHelper resourceHelper,
                                       DeviceConfigurationService deviceConfigurationService,
                                       ValidationService validationService,
                                       Provider<RegisterConfigurationResource> registerConfigurationResourceProvider,
                                       Provider<ConnectionMethodResource> connectionMethodResourceProvider,
                                       Provider<ProtocolDialectResource> protocolDialectResourceProvider,
                                       Provider<LoadProfileConfigurationResource> loadProfileConfigurationResourceProvider,
                                       Provider<SecurityPropertySetResource> securityPropertySetResourceProvider,
                                       Provider<ComTaskEnablementResource> comTaskEnablementResourceProvider,
                                       Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.validationService = validationService;
        this.registerConfigurationResourceProvider = registerConfigurationResourceProvider;
        this.connectionMethodResourceProvider = connectionMethodResourceProvider;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
        this.loadProfileConfigurationResourceProvider = loadProfileConfigurationResourceProvider;
        this.securityPropertySetResourceProvider = securityPropertySetResourceProvider;
        this.comTaskEnablementResourceProvider = comTaskEnablementResourceProvider;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceConfigurationsForDeviceType(@PathParam("deviceTypeId") long id, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<DeviceConfiguration> deviceConfigurations;
        if(Boolean.parseBoolean(queryFilter.getFilterProperties().get("active"))){
            deviceConfigurations = deviceConfigurationService.
                    findActiveDeviceConfigurationsForDeviceType(deviceType).
                    from(queryParameters).
                    find();
        } else {
            deviceConfigurations = deviceConfigurationService.
                    findDeviceConfigurationsUsingDeviceType(deviceType).
                    from(queryParameters).
                    find();
        }
        return PagedInfoList.asJson("deviceConfigurations", DeviceConfigurationInfo.from(deviceConfigurations), queryParameters);
    }

    @GET
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo getDeviceConfigurationsById(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return new DeviceConfigurationInfo(deviceConfiguration);
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    @GET
    @Path("/{deviceConfigurationId}/logbookconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceConfigurationsLogBookConfigurations(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters,
            @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookSpec> logBookSpecs = deviceConfiguration.getLogBookSpecs();
        List<LogBookTypeInfo> logBookTypes = new ArrayList<>(logBookSpecs.size());
        if (available != null && Boolean.parseBoolean(available)) {
            logBookTypes = LogBookTypeInfo.from(findAllAvailableLogBookTypesForDeviceConfiguration(deviceType, deviceConfiguration));
        } else {
            for (LogBookSpec logBookSpec : logBookSpecs) {
                logBookTypes.add(LogBookSpecInfo.from(logBookSpec));
            }
        }
        return Response.ok(PagedInfoList.asJson("data", logBookTypes, queryParameters)).build();
    }

    private List<LogBookType> findAllAvailableLogBookTypesForDeviceConfiguration(DeviceType deviceType, DeviceConfiguration deviceConfiguration) {
        List<LogBookType> allLogBookTypes = deviceType.getLogBookTypes();
        Iterator<LogBookType> logBookTypeIterator = allLogBookTypes.iterator();
        while (logBookTypeIterator.hasNext()) {
            LogBookType logBookType = logBookTypeIterator.next();
            if (deviceConfiguration.hasLogBookSpecForConfig((int) logBookType.getId(), 0)){
                logBookTypeIterator.remove();
            }
        }
        return allLogBookTypes;
    }

    @POST
    @Path("/{deviceConfigurationId}/logbookconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLogBooksSpecForDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookTypeInfo> addedLogBookSpecs = new ArrayList<>(ids.size());
        for (LogBookType logBookType : deviceType.getLogBookTypes()) {
            if(ids.contains(logBookType.getId())){
                LogBookSpec newLogBookSpec = deviceConfiguration.createLogBookSpec(logBookType).add();
                addedLogBookSpecs.add(LogBookSpecInfo.from(newLogBookSpec));
            }
        }
        return Response.ok(addedLogBookSpecs).build();
    }

    @DELETE
    @Path("/{deviceConfigurationId}/logbookconfigurations/{logBookSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogBooksSpecFromDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("logBookSpecId") long logBookSpecId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LogBookSpec logBookSpec = null;
        for (LogBookSpec spec : deviceConfiguration.getLogBookSpecs()) {
            if (spec.getId() == logBookSpecId){
                logBookSpec = spec;
                break;
            }
        }
        if (logBookSpec == null) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_FOUND, logBookSpecId);
        }
        deviceConfiguration.deleteLogBookSpec(logBookSpec);
        return Response.ok().build();
    }

    @PUT
    @Path("/{deviceConfigurationId}/logbookconfigurations/{logBookSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editLogBookSpecForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("logBookSpecId") long logBookSpecId,
            LogBookSpecInfo logBookRequest) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LogBookSpec> logBookSpecs = new ArrayList<>(deviceConfiguration.getLogBookSpecs());
        for (LogBookSpec logBookSpec : logBookSpecs) {
            if (logBookSpec.getId() == logBookSpecId){
                deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec).setOverruledObisCode(logBookRequest.overruledObisCode).update();
                return Response.ok(LogBookSpecInfo.from(logBookSpec)).build();
            }
        }
        throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOGBOOK_TYPE_FOUND, logBookRequest.id);
    }

    @DELETE
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        deviceType.removeConfiguration(deviceConfiguration);
        return Response.ok().build();
    }

    @PUT
    @Path("/{deviceConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo updateDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, DeviceConfigurationInfo deviceConfigurationInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        if (deviceConfigurationInfo.active!=null && deviceConfigurationInfo.active && !deviceConfiguration.isActive()) {
            deviceConfiguration.activate();
        } else if (deviceConfigurationInfo.active!=null && !deviceConfigurationInfo.active && deviceConfiguration.isActive()) {
            deviceConfiguration.deactivate();
        } else {
            deviceConfigurationInfo.writeTo(deviceConfiguration);
            deviceConfiguration.save();
        }
        return new DeviceConfigurationInfo(deviceConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfo createDeviceConfiguration(@PathParam("deviceTypeId") long deviceTypeId, DeviceConfigurationInfo deviceConfigurationInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(deviceConfigurationInfo.name).
                description(deviceConfigurationInfo.description);
        if (deviceConfigurationInfo.canBeGateway!=null) {
            deviceConfigurationBuilder.canActAsGateway(deviceConfigurationInfo.canBeGateway);
        }
        if (deviceConfigurationInfo.isDirectlyAddressable!=null) {
            deviceConfigurationBuilder.isDirectlyAddressable(deviceConfigurationInfo.isDirectlyAddressable);
        }
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        return new DeviceConfigurationInfo(deviceConfiguration);
    }

    @Path("/{deviceConfigurationId}/registerconfigurations")
    public RegisterConfigurationResource getRegisterConfigResource() {
        return registerConfigurationResourceProvider.get();
    }

    @Path("/{deviceConfigurationId}/connectionmethods")
    public ConnectionMethodResource getConnectionMethodResource() {
        return connectionMethodResourceProvider.get();
    }

    @Path("/{deviceConfigurationId}/protocoldialects")
    public ProtocolDialectResource getProtocolDialectsResource() {
        return protocolDialectResourceProvider.get();
    }

    @Path("/{deviceConfigurationId}/loadprofileconfigurations")
    public LoadProfileConfigurationResource getLoadProfileConfigurationResource() {
        return loadProfileConfigurationResourceProvider.get();
    }
    
    @Path("/{deviceConfigurationId}/securityproperties")
    public SecurityPropertySetResource getSecurityPropertySetResource() {
        return securityPropertySetResourceProvider.get();
    }

    @Path("/{deviceConfigurationId}/comtaskenablements")
    public ComTaskEnablementResource getComTaskEnablementResource() {
        return comTaskEnablementResourceProvider.get();
    }

    @GET
    @Path("/{deviceConfigurationId}/comtasks")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComTasks(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        return comTaskEnablementResourceProvider.get().getComTasks(deviceTypeId, deviceConfigurationId, queryParameters, uriInfo);
    }

    @GET
    @Path("/{deviceConfigurationId}/registers/{registerId}/validationrules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationRulesForRegister(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("registerId") long registerId,
            @BeanParam QueryParameters queryParameters) {

        List<ValidationRule> rules = resourceHelper.findRegisterSpec(registerId).getValidationRules();
        List<ValidationRuleInfo> result = new ArrayList<>();
        for (ValidationRule rule : rules) {
            result.add(new ValidationRuleInfo(rule));
        }
        result = ListPager.of(result).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("validationRules", result, queryParameters)).build();
    }

    @GET
    @Path("/{deviceConfigurationId}/channels/{channelId}/validationrules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationRulesForChannel(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("channelId") long channelId,
            @BeanParam QueryParameters queryParameters) {

        List<ValidationRule> rules = resourceHelper.findChannelSpec(channelId).getValidationRules();
        List<ValidationRuleInfo> result = new ArrayList<>();
        for (ValidationRule rule : rules) {
            result.add(new ValidationRuleInfo(rule));
        }
        result = ListPager.of(result).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("validationRules", result, queryParameters)).build();
    }

    @GET
    @Path("/{deviceConfigurationId}/loadprofiles/{loadProfileId}/validationrules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationRulesForLoadProfile(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileId") long loadProfileId,
            @BeanParam QueryParameters queryParameters) {

        List<ValidationRule> rules = resourceHelper.findLoadProfileSpec(loadProfileId).getValidationRules();
        List<ValidationRuleInfo> result = new ArrayList<>();
        for (ValidationRule rule : rules) {
            result.add(new ValidationRuleInfo(rule));
        }
        result = ListPager.of(result).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("validationRules", result, queryParameters)).build();
    }



    @GET
    @Path("/{deviceConfigurationId}/validationrulesets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationsRuleSets(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ValidationRuleSet> ruleSets = deviceConfiguration.getValidationRuleSets();
        List<ValidationRuleSetInfo> result = new ArrayList<>();
        for (ValidationRuleSet ruleSet : ruleSets) {
            result.add(new ValidationRuleSetInfo(ruleSet));
        }
        result = ListPager.of(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("validationRuleSets", result, queryParameters)).build();
    }

    @GET
    @Path("/{deviceConfigurationId}/linkablevalidationrulesets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLinkableValidationsRuleSets(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters) {
        ValidationRuleSetInfos validationRuleSetInfos = new ValidationRuleSetInfos();
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ValidationRuleSet> linkedRuleSets = deviceConfiguration.getValidationRuleSets();
        List<ReadingType> readingTypes = deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration);
        List<ValidationRuleSet> validationRuleSets = validationService.getValidationRuleSets();
        for(ValidationRuleSet validationRuleSet : validationRuleSets) {
            if(!validationRuleSet.getRules(readingTypes).isEmpty() && !linkedRuleSets.contains(validationRuleSet)) {
                validationRuleSetInfos.add(validationRuleSet);
            }
        }
        List<ValidationRuleSetInfo> infolist = validationRuleSetInfos.ruleSets;
        infolist = ListPager.of(infolist).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("validationRuleSets", infolist, queryParameters)).build();
    }

    @DELETE
    @Path("/{deviceConfigurationId}/validationrulesets/{validationRuleSetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteValidationRuleSetFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("validationRuleSetId") long validationRuleSetId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        Optional<ValidationRuleSet> optional = validationService.getValidationRuleSet(validationRuleSetId);
        if (optional.isPresent()) {
            ValidationRuleSet ruleSet = optional.get();
            deviceConfiguration.removeValidationRuleSet(ruleSet);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/{deviceConfigurationId}/validationrulesets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRuleSetsToDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            List<Long> ids,
            @Context UriInfo uriInfo) {
        boolean all = getBoolean(uriInfo, "all");

        if (!all && (ids == null || ids.size() == 0)) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_VALIDATIONRULESET_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ValidationRuleSetInfo> addedValidationRuleSets = new ArrayList<>();
        for (ValidationRuleSet validationRuleSet : all ? allRuleSets() : ruleSetsFor(ids)) {
            if (!deviceConfiguration.getValidationRuleSets().contains(validationRuleSet)) {
                deviceConfiguration.addValidationRuleSet(validationRuleSet);
                addedValidationRuleSets.add(new ValidationRuleSetInfo(validationRuleSet));
            }
        }
        return Response.ok(addedValidationRuleSets).build();
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

    private Iterable<? extends ValidationRuleSet> ruleSetsFor(List<Long> ids) {
        return Iterables.transform(ids, new Function<Long, ValidationRuleSet>() {
            @Override
            public ValidationRuleSet apply(Long input) {
                return validationService.getValidationRuleSet(input).get();
            }
        });
    }

    private Iterable<ValidationRuleSet> allRuleSets() {
        return validationService.getValidationRuleSets();
    }


}
