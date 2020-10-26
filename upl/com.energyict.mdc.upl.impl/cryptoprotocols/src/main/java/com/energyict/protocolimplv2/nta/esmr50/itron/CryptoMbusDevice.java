package com.energyict.protocolimplv2.nta.esmr50.itron;

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
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50MbusDevice;

import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol.CRYPTO_ITRON_MBUS_PROTOCOL_DESCRIPTION;

public class CryptoMbusDevice extends CryptoESMR50MbusDevice {

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
        return "Crypto version: 2020-10-22";
    }

    @Override
    public String getProtocolDescription() {
        return CRYPTO_ITRON_MBUS_PROTOCOL_DESCRIPTION;
    }
}
