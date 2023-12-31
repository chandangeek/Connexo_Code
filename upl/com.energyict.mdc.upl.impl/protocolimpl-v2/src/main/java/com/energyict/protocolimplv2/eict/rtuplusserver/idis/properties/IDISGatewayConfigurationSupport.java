package com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.Arrays;
import java.util.List;

/**
 * A collection of general DLMS properties that are relevant for the IDIS gateway protocol.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in an implementation of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 *
 * @author sva
 * @since 15/10/2014 - 11:16
 */
public class IDISGatewayConfigurationSupport extends DlmsConfigurationSupport {

    public IDISGatewayConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.deviceId()
        );
    }

}