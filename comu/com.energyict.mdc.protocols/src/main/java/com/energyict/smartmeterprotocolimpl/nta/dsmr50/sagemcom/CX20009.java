package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;

import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComMessaging;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 16/09/2014 - 9:49
 */
public class CX20009 extends AM540 {

    @Inject
    public CX20009(TopologyService topologyService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService) {
        super(topologyService, ormClient, readingTypeUtilService);
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-10-03 11:24:32 +0200 (vr, 03 okt 2014) $";
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new SagemComMessaging(this, this.getTopologyService());
        }
        return messageProtocol;
    }

}