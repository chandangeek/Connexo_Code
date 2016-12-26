package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;

/**
 * @author sva
 * @since 20/02/2015 - 17:09
 */
public class AM540MbusProfileDataReader extends IDISProfileDataReader {

    public AM540MbusProfileDataReader(AbstractDlmsProtocol protocol) {
        super(protocol, collectedDataFactory, issueFactory);
    }
}
