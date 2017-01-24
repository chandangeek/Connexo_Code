/*
 * AbstractCommand.java
 *
 * Created on 7 september 2006, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractCommand {

    private SchlumbergerProtocol schlumbergerProtocol;

    abstract protected Command preparebuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;

    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(SchlumbergerProtocol schlumbergerProtocol) {
        this.setSchlumbergerProtocol(schlumbergerProtocol);
    }

    protected SchlumbergerProtocol getSchlumbergerProtocol() {
        return schlumbergerProtocol;
    }

    private void setSchlumbergerProtocol(SchlumbergerProtocol schlumbergerProtocol) {
        this.schlumbergerProtocol = schlumbergerProtocol;
    }

    public void invoke() throws IOException {
        Command command = preparebuild();
        Response response = getSchlumbergerProtocol().getSchlumbergerConnection().sendCommand(command);
        if (response != null) {
            parse(response.getData());
        }
    }
}
