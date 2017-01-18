package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocolimplv2.g3.common.G3Properties;
import com.energyict.protocolimplv2.g3.common.G3Topology;

/**
 * Copyrights EnergyICT
 * Date: 1/13/15
 * Time: 2:13 PM
 */
public class RtuPlusServerTopology extends G3Topology{

    public RtuPlusServerTopology(DeviceIdentifier masterDeviceIdentifier, IdentificationService identificationService, IssueService issueService, PropertySpecService propertySpecService, DlmsSession dlmsSession, G3Properties g3Properties, CollectedDataFactory collectedDataFactory) {
        super(masterDeviceIdentifier, identificationService, issueService, propertySpecService, dlmsSession, g3Properties, collectedDataFactory);
    }

    @Override
    public CollectedTopology collectTopology() {
        CollectedTopology deviceTopology = getDeviceTopology();

        collectAndUpdateMacAddresses(deviceTopology);
        collectAndUpdateShortAddresses(deviceTopology);
        collectAndUpdatePathSegments(deviceTopology);
        collectAndUpdateNeighbours(deviceTopology);

        return deviceTopology;
    }
}
