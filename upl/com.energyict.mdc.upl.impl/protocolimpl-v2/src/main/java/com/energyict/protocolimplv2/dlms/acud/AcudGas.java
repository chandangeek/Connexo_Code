package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

public class AcudGas extends Acud {

    private static final EndDeviceType typeMeter = EndDeviceType.GASMETER;


    public AcudGas(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, calendarExtractor, messageFileExtractor);
    }

    protected AcudLogBookFactory createLogBookFactory() {
        return new AcudGasLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
    }

    public EndDeviceType getTypeMeter() {
        return typeMeter;
    }

    @Override
    public String getProtocolDescription() {
        return "ACUD Gas";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-07-06 $";
    }
}