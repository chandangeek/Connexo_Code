package com.energyict.protocolimplv2.nta.elster;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;

import javax.inject.Inject;
import java.time.Clock;

/**
 * The AM100 implementation of the NTA spec.
 *
 * @author sva
 * @since 30/10/12 (9:58)
 */
public class AM100 extends WebRTUKP {

    @Inject
    public AM100(Clock clock, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory) {
        super(clock, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 DLMS (PRE-NTA)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }
}