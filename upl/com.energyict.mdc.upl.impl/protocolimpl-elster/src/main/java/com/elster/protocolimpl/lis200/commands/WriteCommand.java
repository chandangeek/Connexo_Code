/**
 * 
 */
package com.elster.protocolimpl.lis200.commands;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Default WriteCommand
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class WriteCommand extends AbstractCommand {

	private static String SIMPLE_WRITE_COMMAND = "W1";
	private byte[] writeData;
	private String address;

	/**
	 * Default constructor
     *
     * @param protocolLink - reference to protocol
	 */
	public WriteCommand(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	/**
	 * Setter for the startAddress
	 * 
	 * @param address
	 *            - the address to start reading
	 */
	public void setStartAddress(String address) {
		this.address = address;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command prepareBuild() {
		Command command = new Command(SIMPLE_WRITE_COMMAND.getBytes());
		command.setStartAddress(address);
		command.setData(writeData);
		return command;
	}

	/**
	 * Invoke the command
	 * 
	 * @return the response
	 * 
	 * @throws IOException
	 *             when a logical exception occurred
	 */
	public String invoke() throws IOException {
		Command command = prepareBuild();
		return checkResponseForErrors(getConnection().sendRawCommandFrameAndReturn(command.getCommand(), command.getConstructedData()));
	}

	/**
	 * Setter for the data to write to the device
	 * 
	 * @param setValue
	 *            - the raw data to write
	 */
	public void setDataValue(byte[] setValue) {
		if (setValue != null) {
			writeData = setValue.clone();
		} else {
			writeData = new byte[] { 0x30 };
		}
	}

}
