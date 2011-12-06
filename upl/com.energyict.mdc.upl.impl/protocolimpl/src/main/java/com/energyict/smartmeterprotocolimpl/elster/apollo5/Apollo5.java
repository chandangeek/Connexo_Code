package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

/**
 * Copyrights EnergyICT
 * Date: 28/11/11
 * Time: 14:48
 */
public class Apollo5 extends AS300 {

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public AS300Messaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new Apollo5Messaging(new Apollo5MessageExecutor(this));
        }
        return messageProtocol;
    }
}