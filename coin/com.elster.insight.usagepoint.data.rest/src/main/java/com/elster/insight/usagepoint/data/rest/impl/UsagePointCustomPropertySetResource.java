package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.usagepoint.config.security.Privileges;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointCustomPropertySetResource(CustomPropertySetInfoFactory customPropertySetInfoFactory,
                                               ResourceHelper resourceHelper) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Path("/metrology")
    @RolesAllowed({Privileges.Constants.VIEW_CPS_ON_METROLOGY_CONFIGURATION, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationCustomPropertySetsWithValues(@PathParam("mrid") String usagePointMrid,
                                                                               @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> metrologyCustomPropertySetValues = usagePointExtension.getMetrologyConfigurationCustomPropertySetValues();
        List<CustomPropertySetInfo> infos = new ArrayList<>();
        for (Map.Entry<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValueEntry : metrologyCustomPropertySetValues.entrySet()) {
            CustomPropertySet<?, ?> customPropertySet = customPropertySetValueEntry.getKey().getCustomPropertySet();
            CustomPropertySetValues customPropertySetValue = customPropertySetValueEntry.getValue();
            CustomPropertySetInfo info = customPropertySetInfoFactory.getGeneralInfo(customPropertySetValueEntry.getKey());
            info.properties = customPropertySet.getPropertySpecs()
                    .stream()
                    .map(propertySpec -> customPropertySetInfoFactory.getPropertyInfo(propertySpec,
                            key -> customPropertySetValue != null ? customPropertySetValue.getProperty(key) : null))
                    .collect(Collectors.toList());
            infos.add(info);
        }
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }

    @PUT
    @Path("/metrology")
    @RolesAllowed({Privileges.Constants.VIEW_CPS_ON_METROLOGY_CONFIGURATION, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList setMetrologyConfigurationCustomPropertySetValues(@PathParam("mrid") String usagePointMrid,
                                                                               @BeanParam JsonQueryParameters queryParameters,
                                                                               CustomPropertySetInfo<UsagePointInfo> info) {
        // TODO lock UP! Concurrency check!
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper.getRegisteredCustomPropertySetOrThrowException(info.customPropertySetId);
        CustomPropertySet<?,?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        Map<String, PropertySpec> propertySpecMap = customPropertySet.getPropertySpecs()
                .stream()
                .collect(Collectors.toMap(propertySpec -> propertySpec.getName(), Function.identity()));
        CustomPropertySetValues customPropertySetValues = info.getCustomPropertySetValues((key, value) -> propertySpecMap.get(key).getValueFactory().fromStringValue(value.toString()));
        usagePointExtension.setMetrologyConfigurationCustomPropertySetValue(customPropertySet, customPropertySetValues);
        return getMetrologyConfigurationCustomPropertySetsWithValues(usagePointMrid, queryParameters);
    }
}
