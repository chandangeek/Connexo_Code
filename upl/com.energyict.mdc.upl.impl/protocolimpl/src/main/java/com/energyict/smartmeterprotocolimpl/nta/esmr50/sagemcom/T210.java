package com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom;


import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSConnection;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Protocol;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom.events.SagemcomEsmr50EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom.registers.T210RegisterFactory;

import java.io.IOException;

public class T210 extends ESMR50Protocol {
    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new T210RegisterFactory(this);
        }
        return this.registerFactory;
    }

    /**
     * Getter for the <b>ESMR 5.0</b> EventProfile
     *
     * @return the lazy loaded EventProfile
     */
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new SagemcomEsmr50EventProfile(this);
        }
        return eventProfile;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new ESMR50Messaging(new ESMR50MessageExecutor(this));
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        DLMSConnection connection = getDlmsSession().getDLMSConnection();
        getLogger().info("Not necessary to do HHU SignOn initialization, just create the DLMS Connection "+connection.toString());
        return;
    }
}
