/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

public class JanzB280 extends CX20009 {

    @Inject
    public JanzB280(Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService,
                    SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService,
                    MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService,
                    CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory, MeteringService meteringService,
                    Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService,
                readingTypeUtilService, identificationService, collectedDataFactory, loadProfileFactory, meteringService,
                dsmrSecuritySupportProvider);
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