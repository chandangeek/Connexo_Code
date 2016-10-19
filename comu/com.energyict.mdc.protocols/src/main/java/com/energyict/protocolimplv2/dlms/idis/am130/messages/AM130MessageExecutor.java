package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/02/2015 - 15:10
 */
public class AM130MessageExecutor extends IDISMessageExecutor {

    public AM130MessageExecutor(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
    }
}