package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;

public class T210 extends ESMR50Protocol implements SerialNumberSupport {


    public T210(PropertySpecService propertySpecService, NlsService nlsService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService, nlsService, collectedDataFactory, issueFactory);
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return null;
    }

    @Override
    public String getVersion() {
        return "Enexis first protocol integration version 10.10.2018";
    }

}
