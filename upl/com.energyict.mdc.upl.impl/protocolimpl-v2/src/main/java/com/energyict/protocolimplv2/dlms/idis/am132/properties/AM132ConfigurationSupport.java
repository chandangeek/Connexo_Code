package com.energyict.protocolimplv2.dlms.idis.am132.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iulian on 12/23/2016.
 */
public class AM132ConfigurationSupport extends AM540ConfigurationSupport {

    public AM132ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> uplPropertySpecs = new ArrayList<>(super.getUPLPropertySpecs());

        // for this meter we don't need mirror and gatweay -> only a single required property - nodeAddress
        uplPropertySpecs.remove(super.mirrorLogicalDeviceIdPropertySpec());
        uplPropertySpecs.remove(super.actualLogicalDeviceIdPropertySpec());

        return uplPropertySpecs;
    }

    /**
     * This is a required property for AM132
     */
    protected PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), true, PropertyTranslationKeys.V2_DLMS_NODEID, this.getPropertySpecService()::bigDecimalSpec).finish();
    }
}