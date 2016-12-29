package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;

/**
 * @author sva
 * @since 20/02/2015 - 17:09
 */
public class AM540MbusProfileDataReader extends IDISProfileDataReader {

    public AM540MbusProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }
}
