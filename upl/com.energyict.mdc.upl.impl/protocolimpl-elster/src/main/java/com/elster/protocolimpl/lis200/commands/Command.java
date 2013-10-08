package com.elster.protocolimpl.lis200.commands;

/**
 * Implementation of a simple command object.
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class Command {

	private static String OPEN_BRACKET = "(";
	private static String CLOSE_BRACKET = ")";

	private byte[] data;
	private byte[] command;
	private String startAddress;
	private int length;

	/** Creates a new instance of Command */
	public Command(byte[] command) {
		this.setCommand(command);
	}

	/**
	 * Getter for the data
	 * 
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Setter for the data
	 * 
	 * @param data
	 *            - the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Getter for the command
	 * 
	 * @return the command
	 */
	public byte[] getCommand() {
		return command;
	}

	/**
	 * Setter for the command
	 * 
	 * @param command
	 *            - the command
	 */
	private void setCommand(byte[] command) {
		this.command = command;
	}

	/**
	 * Getter for the length
	 * 
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Setter for the length
	 * 
	 * @param length
	 *            - the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Getter for the startAddress
	 * 
	 * @return the startAddress
	 */
	public String getStartAddress() {
		return startAddress;
	}

	/**
	 * Setter for the startAddress
	 * 
	 * @param startAddress
	 *            - the startAddress
	 */
	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	/**
	 * Construct the request. This includes the starting point and the value/unit
	 * 
	 * @return the request byteArray
	 */
	public byte[] getConstructedData() {
		StringBuilder constructedData = new StringBuilder();
		constructedData.append(getStartAddress());
		constructedData.append(OPEN_BRACKET);
		constructedData.append(new String(getData()));
		constructedData.append(CLOSE_BRACKET);
		return constructedData.toString().getBytes();
	}

}
