/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Specific {@link ReadCommand} for reading archives
 * 
 * @author gna
 * @since 5-mrt-2010
 *
 */
public class ReadArchiveCommand extends ReadCommand {

	/** Used for reading large chunks of data (LoadProfile) */
	private static String BLOCK_READ_COMMAND = "R3";
	
	private String readCommand;
	private byte[] readData;
	
	/**
	 * Super constructor
	 * @param link
	 * 			- the used {@link ProtocolLink}
	 */
	public ReadArchiveCommand(ProtocolLink link) {
		super(link);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command prepareBuild() {
		Command command = new Command(readCommand.getBytes());
		command.setStartAddress(address);
		command.setData(readData);
		return command;
	}

	/**
	 * Invoke the command for one transaction
	 * 
	 * @param data
	 * 			- the request data, it should be of the form:<br>
	 * <blockquote>
	 *  <b>[p] ; [v] ; [b] ;</b>  - where :<br><br>
	 *  <i>[p]  "Position"</i> - The position (column) of the controlling value in the archive. With the values in this column the range 
	 *  of archive data to be read is determined. For this the device compares the data [v] and [b] (see below) to the archive 
	 *  values of this column. [p] is a maximum 2-digit decimal. If [p] is missing, then [p] = 1; <br><br>
	 *  <i>[v]  "Readout from" </i> - Lower limit (oldest data row) of the archive field to be read out. [v] may be up to 17 digits long. 
	 *  This means e.g. a complete time stamp can be showed. If [v] is missing, the oldest available data row is used as the lower limit.<br><br>
	 *  <i>[b]  "Readout up to" </i> - Upper limit (newest data row) of the archive field to be read out. Format as with [v]. 
	 *  If [b] is missing, the newest available data row is used as the upper limit.  
	 *  </blockquote>
	 * 
	 * @return the response
	 * 
	 * @throws IOException
	 *             when a logical exception occurred
	 */
	public String invokeForOneTransaction(String data) throws IOException {
		this.readCommand = SIMPLE_READ_COMMAND;
		this.readData = data.getBytes();
		return invoke();
	}
	
	/**
	 * Invoke the command for multiple requests
	 * 
	 * @param data
	 * 			- the request data, it should be of the form:<br>
	 * <blockquote>
	 *  <b> [p] ; [v] ; [b] ; [t]</b>  - where :<br><br>
	 *  <i>[p]  "Position"</i> - The position (column) of the controlling value in the archive. With the values in this column the range 
	 *  of archive data to be read is determined. For this the device compares the data [v] and [b] (see below) to the archive 
	 *  values of this column. [p] is a maximum 2-digit decimal. If [p] is missing, then [p] = 1; <br><br>
	 *  <i>[v]  "Readout from" </i> - Lower limit (oldest data row) of the archive field to be read out. [v] may be up to 17 digits long. 
	 *  This means e.g. a complete time stamp can be showed. If [v] is missing, the oldest available data row is used as the lower limit.<br><br>
	 *  <i>[b]  "Readout up to" </i> - Upper limit (newest data row) of the archive field to be read out. Format as with [v]. 
	 *  If [b] is missing, the newest available data row is used as the upper limit.  <br><br>
	 *  <i>[t]  "Number of data records per partial block" </i> - This part of the command is only used when reading out an archive 
	 *  with partial blocks. [t] gives the number of data rows which are to be transmitted per partial block. [t] is a max. 
	 *  3-digit decimal, which means that per partial block a maximum of 999 data rows can be transmitted. When reading out 
	 *  an archive without partial blocks (command code "R1"), [t] is omitted. 
	 *  </blockquote>
	 * 
	 * @return the response
	 * 
	 * @throws IOException
	 *             when a logical exception occurred
	 */
	public String invokeForMultiple(String data) throws IOException {
		this.readCommand = BLOCK_READ_COMMAND;
		this.readData = data.getBytes();
		return invoke();
	}

}
