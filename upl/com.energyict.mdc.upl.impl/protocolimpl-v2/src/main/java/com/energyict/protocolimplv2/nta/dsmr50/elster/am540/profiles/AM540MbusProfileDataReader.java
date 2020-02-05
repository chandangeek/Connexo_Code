package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540;

/**
 * @author sva
 * @since 20/02/2015 - 17:09
 */
public final class AM540MbusProfileDataReader<T extends AM540> extends IDISProfileDataReader<AM540> {

    public AM540MbusProfileDataReader(AM540 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }
}
