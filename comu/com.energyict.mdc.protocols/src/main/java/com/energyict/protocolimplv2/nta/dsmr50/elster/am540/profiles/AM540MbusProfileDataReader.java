/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;

public class AM540MbusProfileDataReader extends IDISProfileDataReader {

    public AM540MbusProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueService issueService) {
        super(protocol, collectedDataFactory, issueService);
    }
}
