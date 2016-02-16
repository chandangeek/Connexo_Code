package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;

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
import java.util.function.BiConsumer;
import java.util.function.Supplier;
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

    private PagedInfoList getCustomPropertySetValues(JsonQueryParameters queryParameters,
                                                     Supplier<Map<RegisteredCustomPropertySet, CustomPropertySetValues>> customPropertySetValuesSupplier) {
        List<CustomPropertySetInfo> infos = new ArrayList<>();
        for (Map.Entry<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValueEntry : customPropertySetValuesSupplier.get().entrySet()) {
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

    private void setCustomPropertySetValues(CustomPropertySetInfo<UsagePointInfo> info, BiConsumer<CustomPropertySet<?, ?>, CustomPropertySetValues> customPropertySetValuesConsumer) {
        RegisteredCustomPropertySet registeredCustomPropertySet = resourceHelper.getRegisteredCustomPropertySetOrThrowException(info.customPropertySetId);
        CustomPropertySet<?, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        CustomPropertySetValues customPropertySetValues = customPropertySetInfoFactory.getCustomPropertySetValues(info, customPropertySet.getPropertySpecs());
        customPropertySetValuesConsumer.accept(customPropertySet, customPropertySetValues);
    }

    @GET
    @Path("/metrology")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationCustomPropertySetsWithValues(@PathParam("mrid") String usagePointMrid,
                                                                               @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        return getCustomPropertySetValues(queryParameters, usagePointExtension::getMetrologyConfigurationCustomPropertySetValues);
    }

    @PUT
    @Path("/metrology/{cpsId}")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public PagedInfoList setMetrologyConfigurationCustomPropertySetValues(@PathParam("mrid") String usagePointMrid,
                                                                          @BeanParam JsonQueryParameters queryParameters,
                                                                          CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.lockUsagePointCustomPropertySetExtensionOrThrowException(info.parent);
        setCustomPropertySetValues(info, usagePointExtension::setMetrologyConfigurationCustomPropertySetValue);
        return getCustomPropertySetValues(queryParameters, usagePointExtension::getMetrologyConfigurationCustomPropertySetValues);
    }


    @GET
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getServiceCategoryCustomPropertySetsWithValues(@PathParam("mrid") String usagePointMrid,
                                                                        @BeanParam JsonQueryParameters queryParameters) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.findUsagePointExtensionByMrIdOrThrowException(usagePointMrid);
        return getCustomPropertySetValues(queryParameters, usagePointExtension::getServiceCategoryCustomPropertySetValues);
    }


    @PUT
    @Path("/{cpsId}")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public PagedInfoList setServiceCategoryCustomPropertySetValues(@PathParam("mrid") String usagePointMrid,
                                                                   @BeanParam JsonQueryParameters queryParameters,
                                                                   CustomPropertySetInfo<UsagePointInfo> info) {
        UsagePointCustomPropertySetExtension usagePointExtension = resourceHelper.lockUsagePointCustomPropertySetExtensionOrThrowException(info.parent);
        setCustomPropertySetValues(info, usagePointExtension::setServiceCategoryCustomPropertySetValue);
        return getCustomPropertySetValues(queryParameters, usagePointExtension::getServiceCategoryCustomPropertySetValues);
    }
}
