package com.energyict.protocolimpl.instromet.connection;

import java.io.IOException;

public class Command {
    
    private byte[] data;
    private char command;
    private int startAddress;
    private int length;
    
    /** Creates a new instance of Command */
    public Command(char command) {
        this.setCommand(command);
    }   
    
    public void validate() throws IOException {
    	if (!isReadCommand() && !isWriteCommand() && 
    	    !isLogoffCommand() && !isStatusCommand())
    		throw new IOException("Invalid Packet function, should be R, W, L or S");
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

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}
	
	public boolean isReadCommand() {
        return command == 'R';
    }
    public boolean isWriteCommand() {
        return command == 'W';
    }
    public boolean isLogoffCommand() {
        return command == 'L';
    }
    public boolean isStatusCommand() {
        return command == 'S';
    }
    

}

