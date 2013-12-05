/*
 * FunctionWithoutDataCommand.java
 *
 * Created on 8 juli 2005, 11:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class FunctionWithoutDataCommand extends CommandBuilder {

    private static final int COMMANDBYTE = 0x08;
    private static final int BILLING_READ_COMPLETE=0x10;
    private static final int ALARM_READ_COMPLETE=0x11;

    /** Creates a new instance of FunctionWithoutDataCommand */
    public FunctionWithoutDataCommand(AlphaConnection alphaConnection) {
        super(alphaConnection);
    }
    protected int getExpectedFrameType() {
        return AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
    }

    public void sendBillingReadComplete() throws IOException {
        byte[] data = new byte[2];
        data[0] = COMMANDBYTE;  // CB
        data[1] = BILLING_READ_COMPLETE;  // FUNC
        sendCommandWithResponse(data);
    }

    public void sendAlarmReadComplete() throws IOException {
        byte[] data = new byte[2];
        data[0] = COMMANDBYTE;  // CB
        data[1] = ALARM_READ_COMPLETE;  // FUNC
        sendCommandWithResponse(data);
    }

}
