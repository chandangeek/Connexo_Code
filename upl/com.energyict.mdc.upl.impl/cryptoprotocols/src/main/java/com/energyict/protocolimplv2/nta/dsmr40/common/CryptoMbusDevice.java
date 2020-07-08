package com.energyict.protocolimplv2.nta.dsmr40.common;

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
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.nta.dsmr23.messages.CryptoDSMR23MbusMessaging;

public class CryptoMbusDevice extends Dsmr40MbusProtocol {

    private CryptoDSMR23MbusMessaging mbusMessaging;

    public CryptoMbusDevice(PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                            CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                            DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                            NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor,
                            KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, messageFileExtractor,
                calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2020-07-07";
    }

    @Override
    public String getProtocolDescription() {
        return "Generic Crypto MbusDevice DLMS (NTA DSMR4.0) V2";
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (mbusMessaging == null) {
            mbusMessaging = new CryptoDSMR23MbusMessaging(this, this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getLoadProfileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return mbusMessaging;
    }
}