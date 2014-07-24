package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationRule;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<DeviceConfiguration> configs = deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(validationRuleSetId);
        DeviceConfigurationInfos.DeviceConfigAndTypeInfo[] infos = new DeviceConfigurationInfos.DeviceConfigAndTypeInfo[configs.size()];
        for (int i = queryParameters.getStart(); i < queryParameters.getStart() + queryParameters.getLimit() + 1; i++) {
            infos[i] = new DeviceConfigurationInfos.DeviceConfigAndTypeInfo(configs.get(i));
        }
        List<DeviceConfigurationInfos.DeviceConfigAndTypeInfo> result = ListPager.of(Arrays.asList(infos)).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("deviceConfigurations",
                result, queryParameters)).build();
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
        for (DeviceConfiguration deviceConfiguration : allLinkableDeviceConfigurations(ruleset)) {
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
        ValidationRuleSet validationRuleSet = getValidationRuleSet(validationRuleSetId);
        for (DeviceConfiguration configuration : allLinkableDeviceConfigurations(validationRuleSet)) {
            addConfiguration(configuration, result, validationRuleSet);
        }
        Collections.sort(result.deviceConfigurations, DeviceConfigurationInfos.DEVICE_CONFIG_NAME_COMPARATOR);
        result.deviceConfigurations = ListPager.of(result.deviceConfigurations).from(queryParameters).find();
        return Response.ok(PagedInfoList.asJson("deviceConfigurations", result.deviceConfigurations, queryParameters)).build();
    }


    private List<DeviceConfiguration> allLinkableDeviceConfigurations(ValidationRuleSet validationRuleSet) {
        Set<ReadingType> readingTypesInRuleSet = readingTypesFor(validationRuleSet);
        List<DeviceConfiguration> allLinkable = new ArrayList<>();
        for (DeviceType deviceType : deviceConfigurationService.findAllDeviceTypes().find()) {
            allLinkable.addAll(allLinkableDeviceConfigurations(validationRuleSet, deviceType, readingTypesInRuleSet));
        }
        return allLinkable;
    }

    private List<DeviceConfiguration> allLinkableDeviceConfigurations(ValidationRuleSet ruleSet, DeviceType deviceType, Set<ReadingType> readingTypesInRuleSet) {
        List<DeviceConfiguration> allLinkableForDeviceType = new ArrayList<>();
        for (DeviceConfiguration configuration : deviceType.getConfigurations()) {
            if (!areLinked(configuration, ruleSet) && haveCommonReadingTypes(readingTypesInRuleSet, readingTypesFor(configuration))) {
                allLinkableForDeviceType.add(configuration);
            }
        }
        return allLinkableForDeviceType;
    }

    private List<ReadingType> readingTypesFor(DeviceConfiguration configuration) {
        return deviceConfigurationService.getReadingTypesRelatedToConfiguration(configuration);
    }

    private Set<ReadingType> readingTypesFor(ValidationRuleSet ruleSet) {
        Set<ReadingType> readingTypesInRuleSet = new HashSet<>();
        for (ValidationRule validationRule : ruleSet.getRules()) {
            for (ReadingType readingType : validationRule.getReadingTypes()) {
                readingTypesInRuleSet.add(readingType);
            }
        }
        return readingTypesInRuleSet;
    }

    private boolean haveCommonReadingTypes(Set<ReadingType> readingTypesInRuleSet, List<ReadingType> readingTypes) {
        return !Collections.disjoint(readingTypesInRuleSet, readingTypes);
    }

    private void addConfiguration(DeviceConfiguration configuration, DeviceConfigurationInfos result, ValidationRuleSet ruleSet) {
        if (!ruleSet.getRules(readingTypesFor(configuration)).isEmpty()) {
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

    private boolean areLinked(DeviceConfiguration deviceConfiguration, ValidationRuleSet validationRuleSet) {
        for (ValidationRuleSet ruleSet : deviceConfiguration.getValidationRuleSets()) {
            if (ruleSet.getId() == validationRuleSet.getId()) {
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
