/*
 * CCCommand.java
 *
 * Created on 10 augustus 2005, 16:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.*; 
import java.util.*;

import com.energyict.cbo.*;
/**
 *
 * @author koen
 */
public class CCCommand extends QuantitiesCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("CC",false,true);
  
    /** Creates a new instance of SCCommand */
    public CCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
 
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }
}
