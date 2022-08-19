package com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;
import java.util.Arrays;
import java.util.List;

public class HS340ConfigurationSupport extends HS3300ConfigurationSupport  {

    public HS340ConfigurationSupport(PropertySpecService propertySpecService) {
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
                this.nodeAddressPropertySpec(),
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
                this.increaseFrameCounterOnHLSReply(),
                this.masterKeyPropertySpec()
        );
    }
}
