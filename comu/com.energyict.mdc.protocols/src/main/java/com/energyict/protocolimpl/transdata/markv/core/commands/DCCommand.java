/*
 * DCCommand.java
 *
 * Created on 11 augustus 2005, 9:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author koen
 */
public class DCCommand extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("DC");

    private ProtocolChannelMap protocolChannelMap;

    /** Creates a new instance of DCCommand */
    public DCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        return "protocolChannelMap: "+protocolChannelMap;
    }

    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        String temp = br.readLine();
        setProtocolChannelMap(new ProtocolChannelMap(temp,true));
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    public void setProtocolChannelMap(ProtocolChannelMap protocolChannelMap) {
        this.protocolChannelMap = protocolChannelMap;
    }

}
