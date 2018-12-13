/*
 * Command.java
 *
 * Created on 8 september 2006, 11:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;



/**
 *
 * @author Koen
 */
public class Command {
    
    private byte[] data;
    private char command;
    private int expectedDataLength;
    
    /** Creates a new instance of Command */
    public Command(char command) {
        this.setCommand(command);
    }   

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public char getCommand() {
        return command;
    }

    private void setCommand(char command) {
        this.command = command;
    }
    
    public boolean isICommand() {
        return command == 'I';
    }
    public boolean isDCommand() {
        return command == 'D';
    }
    public boolean isUCommand() {
        return command == 'U';
    }
    public boolean isSCommand() {
        return command == 'S';
    }
    public boolean isENQCommand() {
        return command == SchlumbergerConnection.ENQ;
    }

    public int getExpectedDataLength() {
        return expectedDataLength;
    }

    public void setExpectedDataLength(int expectedDataLength) {
        this.expectedDataLength = expectedDataLength;
    }
}
