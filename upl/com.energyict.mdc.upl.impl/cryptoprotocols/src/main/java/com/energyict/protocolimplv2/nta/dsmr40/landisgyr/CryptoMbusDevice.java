package com.energyict.protocolimplv2.nta.dsmr40.landisgyr;


import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

public class CryptoMbusDevice extends com.energyict.protocolimplv2.nta.dsmr40.common.CryptoMbusDevice {
    public CryptoMbusDevice(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor1) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor, keyAccessorTypeExtractor1);
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2019-02-28";
    }

    @Override
    public String getProtocolDescription() {
        return "LandisGyr Crypto MbusDevice DLMS (NTA DSMR4.0) V2";
    }

}