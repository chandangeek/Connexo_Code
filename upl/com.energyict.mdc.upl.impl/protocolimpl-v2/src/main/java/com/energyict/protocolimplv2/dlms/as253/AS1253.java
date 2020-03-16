package com.energyict.protocolimplv2.dlms.as253;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

public class AS1253 extends AS253 {

    public AS1253(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS1253 AC";
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-04-01 13:26:25 +0200 (We, 01 Apr 2020) $";
    }
}
