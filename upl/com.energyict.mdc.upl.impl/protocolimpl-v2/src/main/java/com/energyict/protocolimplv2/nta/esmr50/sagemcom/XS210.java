package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;

public class XS210 extends ESMR50Protocol implements SerialNumberSupport {


    public XS210(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom XS210 protocol V2";
    }

    @Override
    public String getVersion() {
        return "Enexis first protocol integration version 10.10.2018";
    }

}
