package com.energyict.protocolimplv2.dlms.idis.hs330s.lte.sp;

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

/**
 * Supported device type(s): HS330SIxxxUx SP LTE
 * Protocol release notes: https://confluence.honeywell.com/display/PRTCL/Honeywell+HS330S+Single+Phase+LTE
 */
public class HS330SLteSP extends HS3300 {

    public HS330SLteSP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                       TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                       DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                       KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
              messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-12-14$";
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS330S Single-phase LTE DLMS Meter";
    }

}
