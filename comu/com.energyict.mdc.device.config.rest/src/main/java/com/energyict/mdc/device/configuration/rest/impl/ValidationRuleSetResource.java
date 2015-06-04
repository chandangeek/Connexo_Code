package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.google.common.collect.Iterables;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 9/12/14.
 */
public class ValidationRuleSetResource {

    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ValidationRuleSetResource(ResourceHelper resourceHelper, ValidationService validationService, Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getValidationsRuleSets(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ValidationRuleSet> ruleSets = deviceConfiguration.getValidationRuleSets();
        List<ValidationRuleSetInfo> result = new ArrayList<>();
        for (ValidationRuleSet ruleSet : ruleSets) {
            result.add(new ValidationRuleSetInfo(ruleSet));
        }
        result = ListPager.of(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR).from(queryParameters).find();
        return Response.ok(PagedInfoList.fromPagedList("validationRuleSets", result, queryParameters)).build();
    }

    @DELETE
    @Path("/{validationRuleSetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response deleteValidationRuleSetFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("validationRuleSetId") long validationRuleSetId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        Optional<? extends ValidationRuleSet> optional = validationService.getValidationRuleSet(validationRuleSetId);
        if (optional.isPresent()) {
            ValidationRuleSet ruleSet = optional.get();
            deviceConfiguration.removeValidationRuleSet(ruleSet);
        }
        return Response.ok().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
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
        for (ValidationRuleSet validationRuleSet : all ? allRuleSets(deviceConfiguration) : ruleSetsFor(ids)) {
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
        return Iterables.transform(ids, input -> validationService.getValidationRuleSet(input).get());
    }

    private Iterable<ValidationRuleSet> allRuleSets(DeviceConfiguration deviceConfiguration) {
        final List<ReadingType> relatedToConfiguration = deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration);
        List<ValidationRuleSet> validationRuleSets = validationService.getValidationRuleSets();
        return Iterables.filter(validationRuleSets, validationRuleSet -> !validationRuleSet.getRules(relatedToConfiguration).isEmpty());
    }


}
