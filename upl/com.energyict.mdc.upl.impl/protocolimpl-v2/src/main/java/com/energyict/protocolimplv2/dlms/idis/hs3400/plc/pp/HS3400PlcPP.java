package com.energyict.protocolimplv2.dlms.idis.hs3400.plc.pp;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;
import com.energyict.protocolimplv2.dlms.idis.hs3400.plc.pp.properties.HS3400PlcProperties;

/**
 * Supported device type(s): HS3400DxxHxCP PP PLC
 * Protocol release notes: https://confluence.honeywell.com/pages/viewpage.action?pageId=657054014
 */
public class HS3400PlcPP extends HS3300 {

    public HS3400PlcPP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                        TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                        DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                        KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
                messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-08-10$";
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS3400 Poly-phase PLC DLMS Meter";
    }

    @Override
    public HS3400PlcProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS3400PlcProperties(this.getPropertySpecService(), this.getNlsService(), this.getCertificateWrapperExtractor());
        }
        return (HS3400PlcProperties) dlmsProperties;
    }

}
