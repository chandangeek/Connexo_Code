package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.usagepoint.config.security.Privileges;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public PagedInfoList getMetrologyCustomPropertySetsWithValues(@PathParam("mrid") String usagePointMrid,
                                                                  @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension valuesExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> metrologyCustomPropertySetValues = valuesExtension.getMetrologyCustomPropertySetValues();
        List<CustomPropertySetInfo> infos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : valuesExtension.getMetrologyCustomPropertySets()) {
            CustomPropertySet<?, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
            CustomPropertySetValues customPropertySetValues = metrologyCustomPropertySetValues.get(registeredCustomPropertySet);
            CustomPropertySetInfo info = customPropertySetInfoFactory.getGeneralInfo(registeredCustomPropertySet);
            info.properties = customPropertySet.getPropertySpecs()
                    .stream()
                    .map(propertySpec -> customPropertySetInfoFactory.getPropertyInfo(propertySpec,
                            key -> customPropertySetValues != null ? customPropertySetValues.getProperty(key) : null))
                    .collect(Collectors.toList());
            infos.add(info);
        }
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }
}
