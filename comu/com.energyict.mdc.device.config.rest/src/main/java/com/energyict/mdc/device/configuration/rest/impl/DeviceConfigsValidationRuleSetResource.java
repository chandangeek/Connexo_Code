package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.masterdata.MeasurementType;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/validationruleset")
public class DeviceConfigsValidationRuleSetResource {
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
        for(DeviceConfiguration config : configs) {
            result.add(config);
        }
        return Response.ok(PagedInfoList.asJson("deviceConfigurations",
                ListPager.of(result.deviceConfigurations).from(queryParameters).find(), queryParameters)).build();
    }

    @POST
    @Path("/{validationRuleSetId}/deviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfos addDeviceConfigurationsToRuleSet(@PathParam("validationRuleSetId") long validationRuleSetId,
                                                                     List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_DEVICECONFIG_ID_FOR_ADDING);
        }
        Optional<ValidationRuleSet> optional = validationService.getValidationRuleSet(validationRuleSetId);
        if (!optional.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        ValidationRuleSet ruleset = optional.get();

        for (Long id : ids) {
            DeviceConfiguration deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(id);
            if(deviceConfiguration != null) {
                deviceConfiguration.addValidationRuleSet(ruleset);
                result.add(deviceConfiguration);
            }
        }
        return result;
    }


    @GET
    @Path("/{validationRuleSetId}/linkabledeviceconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceConfigurationInfos getLinkableDeviceConfigurations(@PathParam("validationRuleSetId") long validationRuleSetId,
                                                                             @BeanParam QueryParameters queryParameters) {
        DeviceConfigurationInfos result = new DeviceConfigurationInfos();
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.findAllDeviceTypes();
        List<DeviceType> allDeviceTypes = deviceTypeFinder.from(queryParameters).find();
        for(DeviceType deviceType : allDeviceTypes) {
            Finder<DeviceConfiguration> deviceConfigurationFinder = deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(deviceType);
            List<DeviceConfiguration>  allDeviceConfigurationPerDeviceTypes = deviceConfigurationFinder.from(queryParameters).find();
            addLinkableConfigurations(allDeviceConfigurationPerDeviceTypes, result, validationRuleSetId);
        }
        Collections.sort(result.deviceConfigurations, DeviceConfigurationInfos.DEVICE_CONFIG_NAME_COMPARATOR);
        return result;
    }

    private void addLinkableConfigurations(List<DeviceConfiguration>  allDeviceConfigurationPerDeviceTypes,
                                          DeviceConfigurationInfos result, long validationRuleSetId) {
        for(DeviceConfiguration configuration : allDeviceConfigurationPerDeviceTypes) {
            List<ValidationRuleSet> ruleSetList = configuration.getValidationRuleSets();
            if(ruleSetList.isEmpty() || !isLinkedToCurrentRuleSet(ruleSetList, validationRuleSetId)) {
                addConfiguration(configuration, result, validationRuleSetId);
            }
        }
    }

    private void addConfiguration(DeviceConfiguration configuration, DeviceConfigurationInfos result, long validationRuleSetId) {
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        List<ReadingType> readingTypes = getReadingTypesRelatedToConfiguration(configuration);
        if(!ruleSet.getRules(readingTypes).isEmpty()) {
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

    private List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration) {
        List<ReadingType> readingTypes = new ArrayList<>();
        for (LoadProfileSpec spec : configuration.getLoadProfileSpecs()) {
            for (MeasurementType mapping : spec.getLoadProfileType().getChannelTypes()) {
                readingTypes.add(mapping.getReadingType());
            }
        }
        for (RegisterSpec spec : configuration.getRegisterSpecs()) {
            readingTypes.add(spec.getRegisterType().getReadingType());
        }
        return readingTypes;
    }
}
