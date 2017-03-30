/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ENQCommand.java
 *
 * Created on 8 september 2006, 9:27
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
public class ENQCommand extends AbstractCommand{


    /** Creates a new instance of ENQCommand */
    public ENQCommand(SchlumbergerProtocol schlumbergerProtocol) {
        super(schlumbergerProtocol);
    }

    protected Command preparebuild() throws IOException {
        Command command = new Command((char)SchlumbergerConnection.ENQ);
        return command;
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
    }
}
