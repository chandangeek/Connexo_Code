package com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp;

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
import com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp.messages.HS340Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp.properties.HS340ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp.properties.HS340LteProperties;

/**
 * Supported device type(s): HS340DxxHxCL SP LTE
 * Protocol release notes: https://confluence.honeywell.com/pages/viewpage.action?pageId=657053892
 */
public class HS340LteSP extends HS3300 {

    HS340Messaging deviceMessaging;

    public HS340LteSP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                       TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                       DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                       KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
                messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-08-19$";
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS340 Single-phase LTE DLMS Meter";
    }

    @Override
    public HS340LteProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS340LteProperties(this.getPropertySpecService(), this.getNlsService(), this.getCertificateWrapperExtractor());
        }
        return (HS340LteProperties) dlmsProperties;
    }

    protected HS340Messaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new HS340Messaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(),  this.getCertificateWrapperExtractor(),
                    this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return this.deviceMessaging;
    }

    @Override
    public HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new HS340ConfigurationSupport(this.getPropertySpecService());
    }
}
