/*
 * LOCommand.java
 *
 * Created on 11 augustus 2005, 14:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author koen
 */
public class LOCommand  extends AbstractCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("LO");

    /** Creates a new instance of MICommand */
    public LOCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected void prepareBuild() {
       commandIdentification.setResponse(false);      
    }
    
    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
    }
    
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    @Override
    protected String getCommandName() {
        return "LO";
    }
}
