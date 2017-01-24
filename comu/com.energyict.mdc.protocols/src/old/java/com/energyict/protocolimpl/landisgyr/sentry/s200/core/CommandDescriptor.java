/*
 * CommandDescriptor.java
 *
 * Created on 27 juli 2006, 10:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

/**
 *
 * @author Koen
 */
public class CommandDescriptor {
    
    private int command;
            
    /** Creates a new instance of CommandDescriptor */
    public CommandDescriptor(int command) {
        this.setCommand(command);
    }

    public String toString() {
        return "command="+command;
    }
    
    public int getCommand() {
        return command;
    }

    private void setCommand(int command) {
        this.command = command;
    }
    
}
