/*
 * CommandIdentification.java
 *
 * Created on 9 augustus 2005, 17:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

/**
 *
 * @author koen
 */
public class CommandIdentification {

    private String command;
    private String[] arguments;
    private boolean useProtocol;
    private boolean useBuffer;
    private boolean response;


    /** Creates a new instance of CommandIdentification */
    public CommandIdentification(String command) {
        this(command,false,false,null);
    }

    public CommandIdentification(String command,boolean useProtocol,boolean useBuffer) {
        this(command,useProtocol,useBuffer,null);
    }

    public CommandIdentification(String command,boolean useProtocol,boolean useBuffer,String[] arguments) {
        this.setUseProtocol(useProtocol);
        this.setCommand(command);
        this.setUseBuffer(useBuffer);
        arguments=null;
        response=true;
    }

    public boolean isLogOff() {
        return "LO".compareTo(command)==0;
    }

    public String toString() {
        return command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isUseProtocol() {
        return useProtocol;
    }

    public void setUseProtocol(boolean useProtocol) {
        this.useProtocol = useProtocol;
    }

    public boolean isUseBuffer() {
        return useBuffer;
    }

    public void setUseBuffer(boolean useBuffer) {
        this.useBuffer = useBuffer;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }


}
