/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Default readCommand
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class ReadCommand extends AbstractCommand {

	/** Used for reading a simple object */
	protected static String SIMPLE_READ_COMMAND = "R1";
	
	protected String address;
	private static byte readData = 0x31;

	/**
	 * Default constructor
	 */
	public ReadCommand(ProtocolLink protocolLink) {
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
		Command command = new Command(SIMPLE_READ_COMMAND.getBytes());
		command.setStartAddress(address);
		command.setData(new byte[] { readData });
		return command;
	}

	/**
	 * Invoke the command
	 * 
	 * @return the response
	 * 
	 * @throws java.io.IOException
	 *             when a logical exception occurred
	 */
	public String invoke() throws IOException {
		Command command = prepareBuild();
		getConnection().sendRawCommandFrame(command.getCommand(), command.getConstructedData());
		return checkResponseForErrors(getConnection().receiveString());
	}

}
