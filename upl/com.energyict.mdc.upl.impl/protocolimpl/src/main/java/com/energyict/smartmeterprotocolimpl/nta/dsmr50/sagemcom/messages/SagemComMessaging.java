package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/10/2014 - 9:17
 */
public class SagemComMessaging extends AM540Messaging {

    public SagemComMessaging(AM540 protocol) {
        super(protocol);
    }

    protected Dsmr50MessageExecutor getMessageExecutor() {
        return new SagemComDsmr50MessageExecutor(protocol);
    }
}