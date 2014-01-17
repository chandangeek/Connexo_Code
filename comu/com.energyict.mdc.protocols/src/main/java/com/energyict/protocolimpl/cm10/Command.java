package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class Command {

	private int activityIdentifier;
	private boolean read;
	private boolean isAck = false;
	private byte[] sourceCode = {0x00, 0x00};
	private byte[] destinationCode = new byte[2]; //= {0x1A, 0x00};
	private byte[] arguments;
	private boolean sendAckAfterThisCommand = true; // only for testing power fail details which are erased after ack

	private CM10 cm10Protocol;

	public Command(int activityIdentifier) {
        this.setActivityIdentifier(activityIdentifier);
	}

	public void setArguments(byte[] arguments) {
		this.arguments = arguments;
	}

    public Command(int activityIdentifier, int outStationId) {
        this(activityIdentifier);
        destinationCode[0] = (byte) (outStationId & 0xFF);
        destinationCode[1] = (byte) ((outStationId>>8) & 0xFF);
    }

    public void validate() throws IOException {

    }

    public void setSendAckAfterThisCommand(boolean value) {
    	sendAckAfterThisCommand = value;
    }

    public boolean sendAckAfterThisCommand() {
    	return sendAckAfterThisCommand;
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
		int size = 11;
		if (arguments != null)
			size = size + arguments.length;

		byte[] data = new byte[size];
		data[0] = getCM10Identifier();  // see p 5,6,7 CM10 doc
		data[1] = (byte) size;
		data[2] = getSourceCode()[0];
		data[3] = getSourceCode()[1];
		data[4] = (byte) 0x00; // source extension
		data[5] = getDestinationCode()[0];
		data[6] = getDestinationCode()[1];
		data[7] = 0x00; // destination extension
		data[8] = 0x00; // protocol type CM10
		data[9] = 0x00; // port (unused)

		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				data[10 + i] = arguments[i];
			}
		}

		data[size - 1] = getCrc(ProtocolUtils.getSubArray(data, 0, size - 1));


		return data;
	}

	protected byte getCrc(byte[] data) {
		int size = data.length;
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum = sum + (int) (data[i] & 0xFF); // make it unsigned!
		}
		int crc = 256 - (sum % 256);
        return (byte) crc;
	}




}
