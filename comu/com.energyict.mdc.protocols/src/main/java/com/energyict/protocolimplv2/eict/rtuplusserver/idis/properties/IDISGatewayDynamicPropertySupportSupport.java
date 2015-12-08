package com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of general DLMS properties that are relevant for the IDIS gateway protocol.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in an implementation of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p>
 *
 * @author sva
 * @since 15/10/2014 - 11:16
 */
public class IDISGatewayDynamicPropertySupportSupport extends DlmsProperties {


    public IDISGatewayDynamicPropertySupportSupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(Arrays.asList(
                callingAPTitlePropertySpec(),
                nodeAddressPropertySpec()
        ));
        return propertySpecs;
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return getPropertySpecService().stringPropertySpec(IDIS.CALLING_AP_TITLE, false, IDIS.CALLING_AP_TITLE_DEFAULT);
    }

    public PropertySpec nodeAddressPropertySpec() {
        return getPropertySpecService().stringPropertySpec(MeterProtocol.NODEID, false, "");
    }
}