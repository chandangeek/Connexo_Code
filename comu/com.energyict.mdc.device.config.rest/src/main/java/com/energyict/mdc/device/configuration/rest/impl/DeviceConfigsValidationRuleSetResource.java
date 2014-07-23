package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/validationruleset")
public class DeviceConfigsValidationRuleSetResource {

    public static final String ALL = "all";
    private final DeviceConfigurationService deviceConfigurationService;
    private final ValidationService validationService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceConfigsValidationRuleSetResource(DeviceConfigurationService deviceConfigurationService,
                                                  ValidationService validationService, Thesaurus thesaurus) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/{validationRuleSetId}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLinkedDeviceConfigurations(@PathParam("validationRuleSetId") long validationRuleSetId, @BeanParam QueryParameters queryParameters) {
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        List<DeviceConfiguration> configs = deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(validationRuleSetId);
        for (DeviceConfiguration config : configs) {
            result.add(config);
        }
        return Response.ok(PagedInfoList.asJson("deviceConfigurations",
                ListPager.of(result.deviceConfigurations).from(queryParameters).find(), queryParameters)).build();
    }

    @POST
    @Path("/{validationRuleSetId}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfos addDeviceConfigurationsToRuleSet(@PathParam("validationRuleSetId") long validationRuleSetId,
                                                                     List<Long> ids, @Context UriInfo uriInfo) {
        boolean addAll = getBoolean(uriInfo, ALL);
        if (!addAll && (ids == null || ids.size() == 0)) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_DEVICECONFIG_ID_FOR_ADDING);
        }
        ValidationRuleSet ruleset = getValidationRuleSet(validationRuleSetId);
        if (addAll) {
            return addAllDeviceConfigurations(ruleset);
        }
        return AddSelectedDeviceConfigurations(ruleset, ids);
    }

    private DeviceConfigurationInfos AddSelectedDeviceConfigurations(ValidationRuleSet ruleset, List<Long> ids) {
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        for (Long id : ids) {
            DeviceConfiguration deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(id);
            if (deviceConfiguration != null) {
                deviceConfiguration.addValidationRuleSet(ruleset);
                result.add(deviceConfiguration);
            }
        }
        return result;
    }

    private DeviceConfigurationInfos addAllDeviceConfigurations(ValidationRuleSet ruleset) {
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        List<DeviceConfiguration> deviceConfigurations = deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(ruleset.getId());
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            deviceConfiguration.addValidationRuleSet(ruleset);
            result.add(deviceConfiguration);
        }
        return result;
    }

    @GET
    @Path("/{validationRuleSetId}/linkabledeviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLinkableDeviceConfigurations(@PathParam("validationRuleSetId") long validationRuleSetId,
                                                    @BeanParam QueryParameters queryParameters) {
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        for (DeviceConfiguration configuration : allLinkableDeviceConfigurations(validationRuleSetId)) {
            addConfiguration(configuration, result, validationRuleSetId);
        }
        Collections.sort(result.deviceConfigurations, DeviceConfigurationInfos.DEVICE_CONFIG_NAME_COMPARATOR);
        result.deviceConfigurations = ListPager.of(result.deviceConfigurations).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("deviceConfigurations", result.deviceConfigurations, queryParameters)).build();
    }


    private List<DeviceConfiguration> allLinkableDeviceConfigurations(long validationRuleSetId) {
        List<DeviceConfiguration> allLinkable = new ArrayList<>();
        for (DeviceType deviceType : deviceConfigurationService.findAllDeviceTypes().find()) {
            allLinkable.addAll(allLinkableDeviceConfigurations(validationRuleSetId, deviceType));
        }
        return allLinkable;
    }

    private List<DeviceConfiguration> allLinkableDeviceConfigurations(long validationRuleSetId, DeviceType deviceType) {
        List<DeviceConfiguration> allLinkableForDeviceType = new ArrayList<>();
        for (DeviceConfiguration configuration : deviceType.getConfigurations()) {
            if (!isLinkedToCurrentRuleSet(configuration.getValidationRuleSets(), validationRuleSetId)) {
                allLinkableForDeviceType.add(configuration);
            }
        }
        return allLinkableForDeviceType;
    }

    private void addConfiguration(DeviceConfiguration configuration, DeviceConfigurationInfos result, long validationRuleSetId) {
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        List<ReadingType> readingTypes = deviceConfigurationService.getReadingTypesRelatedToConfiguration(configuration);
        if (!ruleSet.getRules(readingTypes).isEmpty()) {
            result.add(configuration);
        }
    }

    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<ValidationRuleSet> ruleSetRef = validationService.getValidationRuleSet(validationRuleSetId);
        if (!ruleSetRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return ruleSetRef.get();
    }

    private boolean isLinkedToCurrentRuleSet(List<ValidationRuleSet> ruleSetList, long validationRuleSetId) {
        for (ValidationRuleSet ruleSet : ruleSetList) {
            if (ruleSet.getId() == validationRuleSetId) {
                return true;
            }
        }
        return false;
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }


}
