package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.messages.HS3400Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.properties.HS3400ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.properties.HS3400LteProperties;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers.HS3400RegisterFactory;

import java.util.List;

/**
 * Supported device type(s): HS3400DxxHxCL PP LTE
 * Protocol release notes: https://confluence.honeywell.com/pages/viewpage.action?pageId=657054006
 */
public class HS3400LtePP extends HS3300 {


    protected HS3400Messaging deviceMessaging;
    protected HS3400RegisterFactory registerFactory;

    public HS3400LtePP(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                        TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                        DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                        KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, calendarExtractor, nlsService, converter,
                messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-09-05$";
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

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDeviceMessaging().getSupportedMessages();
    }

    protected HS3400Messaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new HS3400Messaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(),  this.getCertificateWrapperExtractor(),
                    this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return this.deviceMessaging;
    }

    @Override
    public HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new HS3400ConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    protected HS3400RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new HS3400RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        journal(getLogPrefix() + "Reading " + registers.size() + " registers");
        // communication exceptions are handled inside readRegisters
        return getRegisterFactory().readRegisters(registers);
    }
}
