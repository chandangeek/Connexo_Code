package com.energyict.protocolimplv2.dlms.idis.as3000g.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 11/08/2015 - 15:15
 */
public class AS3000GConfigurationSupport extends AM540ConfigurationSupport {

    public static final BigDecimal DEFAULT_LOWER_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_UPPER_SERVER_MAC_ADDRESS = BigDecimal.ONE;

    public AS3000GConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.pollingDelayPropertySpec(),
                this.initialFrameCounter(),
                this.requestAuthenticatedFrameCounter(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep(),
                this.deviceSystemTitlePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.supportsHundrethsTimeField(),
                this.useUndefinedForClockStatus(),
                this.useUndefinedForTimeDeviation(),
                this.skipFramecounterAuthenticationTagValidation(),
                this.useFixedObjectList(),
                this.skipSlaveDevices(),
                this.validateLoadProfileChannelsPropertySpec(),
                this.cipheringTypePropertySpec(),
                this.ipV4Address(),
                this.ipV6Address(),
                this.shortAddressPan(),
                this.increaseFrameCounterOnHLSReply(),
                this.masterKeyPropertySpec()
        );
    }

    @Override
    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_DLMS_SERVER_LOWER_MAC_ADDRESS, DEFAULT_LOWER_SERVER_MAC_ADDRESS);
    }

    @Override
    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_DLMS_SERVER_UPPER_MAC_ADDRESS, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }
}