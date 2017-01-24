/*
 * ShortFormatCommand.java
 *
 * Created on 8 juli 2005, 11:31
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
public class ShortFormatCommand extends CommandBuilder {

    static public final int COMMANDBYTE_TERMINATE_SESSION = 0x80;
    static public final int COMMANDBYTE_CONTINUE_READ = 0x81;
    static public final int COMMANDBYTE_RESEND_LAST_PACKET = 0x82;
    static public final int COMMANDBYTE_ARE_YOU_OK = 0x83;
    static public final int COMMANDBYTE_TAKE_CONTROL = 0x84;
    static public final int COMMANDBYTE_SET_BAUDRATE_1200 = 0x90;
    static public final int COMMANDBYTE_SET_BAUDRATE_9600 = 0x93;

    /** Creates a new instance of ShortFormatCommand */
    public ShortFormatCommand(AlphaConnection alphaConnection) {
        super(alphaConnection);
    }
    protected int getExpectedFrameType() {
        return AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
    }

    public void terminateSession() throws IOException {
        sendCommandWithoutResponse(COMMANDBYTE_TERMINATE_SESSION);
    }

    public void continueRead() throws IOException {
        sendCommandWithoutResponse(COMMANDBYTE_CONTINUE_READ);
    }

    public void setBaudrate9600() throws IOException {
        sendCommandWithoutResponse(COMMANDBYTE_SET_BAUDRATE_9600);
    }
}
