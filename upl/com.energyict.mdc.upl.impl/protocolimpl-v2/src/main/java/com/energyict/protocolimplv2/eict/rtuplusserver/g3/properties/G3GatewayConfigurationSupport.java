package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

/**
 * A collection of general DLMS properties that are relevant for the G3 gateway protocol.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class G3GatewayConfigurationSupport {

    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.readCachePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.validateInvokeIdPropertySpec()
        );
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, BigDecimal.ONE);
    }

    private PropertySpec timeZonePropertySpec() {
        return Services
                    .propertySpecService()
                    .timeZoneSpec()
                    .named(TIMEZONE, TIMEZONE)
                    .describedAs("Description for " + TIMEZONE)
                    .finish();
    }

    private PropertySpec validateInvokeIdPropertySpec() {
        return UPLPropertySpecFactory.booleanValue(VALIDATE_INVOKE_ID, true);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return this.durationPropertySpecBuilder(G3GatewayProperties.AARQ_TIMEOUT)
                    .setDefaultValue(G3GatewayProperties.AARQ_TIMEOUT_DEFAULT)
                    .finish();
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.booleanValue(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.durationPropertySpecBuilder(FORCED_DELAY)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(MAX_REC_PDU_SIZE, false, DEFAULT_MAX_REC_PDU_SIZE);
    }

    private PropertySpecBuilder<Duration> durationPropertySpecBuilder(String name) {
        return Services
                .propertySpecService()
                .durationSpec()
                .named(name, name)
                .describedAs("Description for " + name);
    }

}