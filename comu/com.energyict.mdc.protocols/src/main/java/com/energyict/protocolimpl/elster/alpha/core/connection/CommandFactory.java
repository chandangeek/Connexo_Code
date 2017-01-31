/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 11 juli 2005, 14:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CommandFactory {

    AlphaConnection alphaConnection;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(AlphaConnection alphaConnection) {
        this.alphaConnection=alphaConnection;
    }

    // KV 10032006
    public void signOn(int deviceNumber, String pass) throws IOException {
        WhoAreYouData wayd = getFunctionWithDataCommand((alphaConnection.getTimeout()/1000)*2).whoAreYou(deviceNumber);
        getFunctionWithDataCommand().passwordCheck(wayd,pass);

    }

    public void opticalHandshake(SerialCommunicationChannel commChannel,String pass, int dtrBehaviour) throws IOException {
        commChannel.setBaudrate(1200);

        if (dtrBehaviour == 0)
            commChannel.setDTR(false);
        else if (dtrBehaviour == 1)
            commChannel.setDTR(true);

        commChannel.setRTS(false);    // important when using the Elster US optical head. With RTS active, the optical head seams not to work!
        alphaConnection.response2AreYouOK();
        commChannel.setBaudrate(9600);

        if (dtrBehaviour == 0)
            commChannel.setDTR(false);
        else if (dtrBehaviour == 1)
            commChannel.setDTR(true);

        commChannel.setRTS(false);    // important when using the Elster US optical head. With RTS active, the optical head seams not to work!

        // KV_TO_DO i am waiting for a good solution. Now we wait for 300 ms and then flush the buffer.
        alphaConnection.waitForTakeControl();
        alphaConnection.delayAndFlush(300);

        getFunctionWithDataCommand().passwordCheck(null,pass);
    }

    public void opticalHandshakeOverModemport(String pass) throws IOException {
        alphaConnection.response2AreYouOK();
        // KV_TO_DO i am waiting for a good solution. Now we wait for 300 ms and then flush the buffer.
        alphaConnection.waitForTakeControl();
        alphaConnection.delayAndFlush(300);
        getFunctionWithDataCommand().passwordCheck(null,pass);
    }

    public FunctionWithDataCommand getFunctionWithDataCommand() {
        return getFunctionWithDataCommand(0);
    }
    public FunctionWithDataCommand getFunctionWithDataCommand(int timeout) {
        return new FunctionWithDataCommand(alphaConnection, timeout);
    }
    public FunctionWithoutDataCommand getFunctionWithoutDataCommand() {
        return new FunctionWithoutDataCommand(alphaConnection);
    }
    public ClassReadCommand getClassReadCommand() {
        return new ClassReadCommand(alphaConnection);
    }
    public ClassWriteCommand getClassWriteCommand() {
        return new ClassWriteCommand(alphaConnection);
    }
    public ShortFormatCommand getShortFormatCommand() {
        return new ShortFormatCommand(alphaConnection);
    }

}
