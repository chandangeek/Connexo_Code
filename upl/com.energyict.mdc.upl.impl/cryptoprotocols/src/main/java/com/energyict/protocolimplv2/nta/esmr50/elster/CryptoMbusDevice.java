package com.energyict.protocolimplv2.nta.esmr50.elster;

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

import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol.CRYPTO_ELSTER_MBUS_PROTOCOL_DESCRIPTION;

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
        return "Crypto version: 2021-12-21";
    }

    @Override
    public String getProtocolDescription() {
        return CRYPTO_ELSTER_MBUS_PROTOCOL_DESCRIPTION;
    }

    @Override
    public boolean supportsAuxiliaryFirmwareVersion() {
        return false;
    }

}
