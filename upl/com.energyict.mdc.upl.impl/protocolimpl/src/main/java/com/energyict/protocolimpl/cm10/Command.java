package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.protocol.ProtocolUtils;

public class Command {
	
	private int activityIdentifier;
	private boolean read;
	private boolean isAck = false;
	private byte[] sourceCode = {0x00, 0x00};
	private byte[] destinationCode = {0x1A, 0x00};
    
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

	public byte[] getDestinationCode() {
		return destinationCode;
	}

	public void setDestinationCode(byte[] destinationCode) {
		this.destinationCode = destinationCode;
	}
	public byte getCM10Identifier() {
		int singleBlockVal = 32 + 64; // see p7 CM10 doc
		int ackVal = (isAck) ? 128 : 0;
		int readVal= (read) ? 0 : 16;
		return (byte) (activityIdentifier + readVal + singleBlockVal + ackVal);
	}
	
	public Command getAckCommand() {
		Command command = new Command(this.getActivityIdentifier());
		command.setDestinationCode(this.getDestinationCode());
		command.setSourceCode(this.getSourceCode());
		command.setRead(this.isRead());
		command.setAck(true);
		this.setAck(true);
		return command;
	}
	
	public byte[] getBytes() {
		byte[] data = new byte[11];
		data[0] = getCM10Identifier();  // see p 5,6,7 CM10 doc
		data[1] = 0x0B; // block size
		data[2] = getSourceCode()[0];
		data[3] = getSourceCode()[1];
		data[4] = (byte) 0x00; // source extension
		data[5] = getDestinationCode()[0];
		data[6] = getDestinationCode()[1];
		data[7] = 0x00; // destination extension
		data[8] = 0x00; // protocol type CM10
		data[9] = 0x00; // port (unused)
		data[10] = getCrc(ProtocolUtils.getSubArray(data, 0, 10));
		return data;
	}
	
	protected byte getCrc(byte[] data) {
		int size = data.length;
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum = sum + (int) data[i];
		}
		int crc = 256 - (sum % 256);
        return (byte) crc;
	}
    
  
    

}
