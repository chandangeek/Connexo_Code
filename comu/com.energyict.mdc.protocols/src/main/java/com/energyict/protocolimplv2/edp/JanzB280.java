package com.energyict.protocolimplv2.edp;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 11:11
 * Author: khe
 */
public class JanzB280 extends CX20009 {

    @Inject
    public JanzB280(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService) {
        super(propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService);
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public String getProtocolDescription() {
        return "Janz B280 DLMS";
    }
}
