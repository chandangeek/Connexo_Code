package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.acud.messages.AcudElectricMessaging;
import com.energyict.protocolimplv2.dlms.acud.messages.AcudMessaging;

public class AcudElectricity extends Acud {

    private static final EndDeviceType typeMeter = EndDeviceType.ELECTRICMETER;

    public AcudElectricity(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, calendarExtractor, messageFileExtractor);
    }

    protected AcudRegisterFactory createRegisterFactory() {
        return new AcudElectricRegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
    }

    protected AcudLogBookFactory createLogBookFactory() {
        return new AcudElectricLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
    }

    protected AcudMessaging createProtocolMessaging() {
        return new AcudElectricMessaging(this, getPropertySpecService(), getNlsService(), getConverter(), getTariffCalendarExtractor(), getMessageFileExtractor());
    }

    public EndDeviceType getTypeMeter() {
        return typeMeter;
    }

    @Override
    public String getProtocolDescription() {
        return "ACUD Electricity";
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-03-31 13:26:25 +0200 (Tue, 31 Mar 2020) $";
    }
}
