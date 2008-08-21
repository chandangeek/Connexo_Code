package com.energyict.protocolimpl.CM32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Command {
	
	private int activityIdentifier;
	private boolean read;
	private boolean isAck = false;
	private byte[] sourceCode = {0x00, 0x00};
	private byte[] destionationCode = {0x21, 0x00};
    
    public Command(int activityIdentifier) {
        this.setActivityIdentifier(activityIdentifier);
    }   
    
    public void validate() throws IOException {

    }

	public int getActivityIdentifier() {
		return activityIdentifier;
	}

	public void setActivityIdentifier(int activityIdentifier) {
		this.activityIdentifier = activityIdentifier;
	}

	public boolean isAck() {
		return isAck;
	}

	public void setAck(boolean isAck) {
		this.isAck = isAck;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public byte[] getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(byte[] sourceCode) {
		this.sourceCode = sourceCode;
	}

	public byte[] getDestionationCode() {
		return destionationCode;
	}

	public void setDestionationCode(byte[] destionationCode) {
		this.destionationCode = destionationCode;
	}
	public byte getCM10Identifier() {
		int singleBlockVal = 32 + 64; // see p7 CM10 doc
		int ackVal = (isAck) ? 128 : 0;
		int readVal= (read) ? 0 : 16;
		return (byte) (activityIdentifier + readVal + singleBlockVal + ackVal);
	}
    
  
    

}
