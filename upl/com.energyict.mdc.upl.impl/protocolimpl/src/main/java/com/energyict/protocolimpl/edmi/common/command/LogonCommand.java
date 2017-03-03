package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author koen
 */
public class LogonCommand extends AbstractCommand {

    private static final String LOGON_COMMAND = "L";
    private String logon;

    private String password;
    
    /** Creates a new instance of LogonCommand */
    public LogonCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareBuild() {
        String data = LOGON_COMMAND + getLogon() + "," + getPassword();
        try {
            return ProtocolTools.concatByteArrays(data.getBytes("US-ASCII"), new byte[]{0});
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void invoke() {
        try {
            super.invoke();
        } catch (CommunicationException e) {
            if (e.getCause() instanceof CommandResponseException && ((CommandResponseException)e.getCause()).getResponseCANCode() == 4) {
                throw CommunicationException.protocolConnectFailed(e);
            } else {
                throw e; // Rethrow the original communication exception
            }
        }
    }

    protected void parse(byte[] data) {
        
    }
    
    public String getLogon() {
        return logon;
    }

    public void setLogon(String logon) {
        this.logon = logon;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}