/*
 * InitiateCommand.java
 *
 * Created on 4 december 2006, 14:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class Abort extends AbstractCommand {
    
    /** Creates a new instance of Abort */
    public Abort(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareInvoke() {
        return new byte[]{(byte)getId()};
    }
    
    protected int getId() {
        return 0x11;
    }

}
