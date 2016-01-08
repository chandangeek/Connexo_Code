/*
 * WriteCommand.java
 *
 * Created on 27 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author koen
 */
public class WriteCommand extends AbstractCommand {

	private int registerId;
	private byte[] data;

	/** Creates a new instance of WriteCommand */
	public WriteCommand(CommandFactory commandFactory) {
		super(commandFactory);
	}

	public String toString() {
		// Generated code by ToStringBuilder
		return "WriteCommand:\n" + "   data=" + ProtocolUtils.outputHexString(getData()) + "\n";
	}

	private final char COMMAND='W'; // 'W'

	protected byte[] prepareBuild() {
		byte[] data2;

		if (COMMAND == 'N') {
			data2 = new byte[getData().length+1+4];
			data2[0] = 'N';
			data2[1]=(byte)(getRegisterId()>>24);
			data2[2]=(byte)(getRegisterId()>>16);
			data2[3]=(byte)(getRegisterId()>>8);
			data2[4]=(byte)getRegisterId();
			System.arraycopy(getData(),0,data2,5,getData().length);
		}

		if (COMMAND == 'W') {
			data2 = new byte[getData().length+1+2];
			data2[0] = 'W';
			data2[1]=(byte)(getRegisterId()>>8);
			data2[2]=(byte)getRegisterId();
			System.arraycopy(getData(),0,data2,3,getData().length);
		}

		return data2;
	}

	protected void parse(byte[] rawData) throws IOException {
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getRegisterId() {
		return registerId;
	}

	public void setRegisterId(int registerId) {
		this.registerId = registerId;
	}
}
