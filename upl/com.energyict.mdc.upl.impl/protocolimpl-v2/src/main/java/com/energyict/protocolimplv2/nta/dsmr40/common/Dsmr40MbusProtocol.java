package com.energyict.protocolimplv2.nta.dsmr40.common;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.dsmr23.eict.MbusDevice;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 17:22
 */
public class Dsmr40MbusProtocol extends MbusDevice {

    public Dsmr40MbusProtocol(PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                              CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                              DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                              NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor,
                              KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, messageFileExtractor,
                calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-02-04 14:56:46 +0200 (Mon, 04 Feb 2013) $";
    }

    @Override
    public String getProtocolDescription() {
        return "Generic MbusDevice DLMS (NTA DSMR4.0) V2";
    }

}
