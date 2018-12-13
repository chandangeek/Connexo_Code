package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;

import java.io.IOException;

/**
 * @author sva
 * @since 6/01/2015 - 14:45
 */
public class Dsmr40MbusMessageExecutor extends Dsmr23MbusMessageExecutor {

    public Dsmr40MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    /**
     * DSMR4.0 adds support for load profiles with channels that have the same obiscode but a different unit.
     * E.g.: gas value (attr 2) and gas capture time (attr 5), both come from the same extended register but are stored in 2 individual channels.
     * <p/>
     * They should be stored in 1 register only in EiServer, gas capture time is stored as event timestamp of this register.
     */
    protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = super.loadProfileRegisterRequest(pendingMessage);
        return new LoadProfileToRegisterParser().parse(collectedMessage);
    }
}