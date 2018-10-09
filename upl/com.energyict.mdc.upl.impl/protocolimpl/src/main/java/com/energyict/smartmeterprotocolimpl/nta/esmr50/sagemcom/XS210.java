package com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom;

import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Protocol;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom.events.SagemcomEsmr50EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom.registers.XS210RegisterFactory;

public class XS210 extends ESMR50Protocol {
    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new XS210RegisterFactory(this);
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
}
