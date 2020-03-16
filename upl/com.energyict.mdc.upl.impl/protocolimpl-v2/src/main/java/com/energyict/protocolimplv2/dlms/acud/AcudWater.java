package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

public class AcudWater extends Acud {

    private static final EndDeviceType typeMeter = EndDeviceType.WATERMETER;

    public AcudWater(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService);
    }

    public EndDeviceType getTypeMeter() {
        return typeMeter;
    }

    @Override
    public String getProtocolDescription() {
        return "ACUD Water";
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-03-31 13:26:25 +0200 (Tue, 31 Mar 2020) $";
    }
}
