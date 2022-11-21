package com.energyict.protocolimplv2.dlms.idis.hs340.plc.sp;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;
import com.energyict.protocolimplv2.dlms.idis.hs340.plc.sp.messages.HS340PLCMessaging;
import com.energyict.protocolimplv2.dlms.idis.hs340.plc.sp.properties.HS340ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs340.plc.sp.properties.HS340PlcProperties;

/**
 * Supported device type(s): HS340DxxHxCP SP PLC
 * Protocol release notes: https://confluence.honeywell.com/pages/viewpage.action?pageId=657053974
 */
public class HS340PlcSP extends HS3300 {

    protected HS340PLCMessaging deviceMessaging;

    public HS340PlcSP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                       TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                       DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                       KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
                messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-11-08$";
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS340 Single-phase PLC DLMS Meter";
    }

    @Override
    public HS340PlcProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS340PlcProperties(this.getPropertySpecService(), this.getNlsService(), this.getCertificateWrapperExtractor());
        }
        return (HS340PlcProperties) dlmsProperties;
    }

    @Override
    protected HS340PLCMessaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new HS340PLCMessaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), nlsService, converter, calendarExtractor, certificateWrapperExtractor,
                    messageFileExtractor, keyAccessorTypeExtractor);
        }
        return this.deviceMessaging;
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new HS340ConfigurationSupport(this.getPropertySpecService());
    }
}
