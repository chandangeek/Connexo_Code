package com.energyict.protocolimplv2.dlms.idis.hs3400.plc.pp.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;

import java.util.Arrays;
import java.util.List;

public class HS3400ConfigurationSupport extends HS3300ConfigurationSupport {

    public HS3400ConfigurationSupport(PropertySpecService propertySpecService) {
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
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.dlmsWANKEKPropertySpec(),
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
                this.frameCounterLimit(),
                this.deviceSystemTitlePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.useUndefinedForTimeDeviation(),
                this.skipFramecounterAuthenticationTagValidation(),
                this.cipheringTypePropertySpec(),
                this.ipV4Address(),
                this.ipV6Address(),
                this.shortAddressPan(),
                this.increaseFrameCounterOnHLSReply(),
                this.masterKeyPropertySpec()
        );
    }

}
