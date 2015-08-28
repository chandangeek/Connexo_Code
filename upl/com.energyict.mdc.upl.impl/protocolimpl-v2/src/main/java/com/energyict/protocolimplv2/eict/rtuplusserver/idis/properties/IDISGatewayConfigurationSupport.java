package com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.Arrays;
import java.util.Collections;
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


    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
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