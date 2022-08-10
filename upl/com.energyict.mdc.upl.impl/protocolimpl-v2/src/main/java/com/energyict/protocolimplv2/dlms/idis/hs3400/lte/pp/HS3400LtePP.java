package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp;

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
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.properties.HS3400LteProperties;

/**
 * Supported device type(s): HS3400DxxHxCL PP LTE
 * Protocol release notes: https://confluence.honeywell.com/pages/viewpage.action?pageId=657054006
 */
public class HS3400LtePP extends HS3300 {

    public HS3400LtePP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
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
        return "Honeywell HS3400 Poly-phase LTE DLMS Meter";
    }

    @Override
    public HS3400LteProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS3400LteProperties(this.getPropertySpecService(), this.getNlsService(), this.getCertificateWrapperExtractor());
        }
        return (HS3400LteProperties) dlmsProperties;
    }

}
