package com.energyict.protocolimplv2.dlms.idis.am132.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iulian on 12/23/2016.
 */
public class AM132ConfigurationSupport extends AM540ConfigurationSupport {

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();

        propertySpecs.addAll(super.getRequiredProperties());

        propertySpecs.add(super.nodeAddressPropertySpec());

        return propertySpecs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();

        propertySpecs.addAll(super.getOptionalProperties());

        // for this meter we don't need mirror and gatweay -> only a single required property - nodeAddress
        propertySpecs.remove(super.mirrorLogicalDeviceIdPropertySpec());
        propertySpecs.remove(super.actualLogicalDeviceIdPropertySpec());

        // move node address as a required property
        propertySpecs.remove(super.nodeAddressPropertySpec());

        return propertySpecs;
    }
}
