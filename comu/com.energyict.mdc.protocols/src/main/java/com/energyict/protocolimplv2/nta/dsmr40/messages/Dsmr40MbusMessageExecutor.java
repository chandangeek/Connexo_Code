package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;

import java.io.IOException;
import java.time.Clock;

/**
 * @author sva
 * @since 6/01/2015 - 14:45
 */
public class Dsmr40MbusMessageExecutor extends Dsmr23MbusMessageExecutor {

    public Dsmr40MbusMessageExecutor(AbstractDlmsProtocol protocol, Clock clock, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, TopologyService topologyService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(protocol, clock, issueService, readingTypeUtilService, topologyService, collectedDataFactory, loadProfileFactory);
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
