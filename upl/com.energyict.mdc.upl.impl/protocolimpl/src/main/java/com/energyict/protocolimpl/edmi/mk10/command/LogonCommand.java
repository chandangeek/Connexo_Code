/*
 * LogonCommand.java
 *
 * Created on 21 maart 2006, 10:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author koen
 */
public class LogonCommand extends AbstractCommand {

	private String logon;

	private String password;

	/** Creates a new instance of LogonCommand */
	public LogonCommand(CommandFactory commandFactory) {
		super(commandFactory);
	}

    protected byte[] prepareBuild() {
//		String data = "L"+getLogon()+","+getPassword()+"\0";
        String data = "L" + getLogon() + "," + getPassword();
        try {
            return ProtocolTools.concatByteArrays(data.getBytes("US-ASCII"), new byte[]{0});
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
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


	public static void main(String[] args) {
		LogonCommand lc = new LogonCommand(null);
		lc.setLogon("koen");
		lc.setPassword("1234");
		lc.prepareBuild();

	}
}
