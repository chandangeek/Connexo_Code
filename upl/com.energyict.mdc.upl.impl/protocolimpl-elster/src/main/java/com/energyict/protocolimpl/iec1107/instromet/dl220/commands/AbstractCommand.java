package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Additional implementation of a commands functionality
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public abstract class AbstractCommand {
	
	/** Error indication string */
	public static String ERROR_INDICATION = "#";

	/**
	 * Default constructor
	 * 
	 * @param link
	 *            - The {@link ProtocolLink}
	 */
	public AbstractCommand(ProtocolLink link) {
		this.link = link;
	}

	/**
	 * The protocol using this command
	 */
	private ProtocolLink link;

	/**
	 * Prepare a command for execution
	 * 
	 * @return a prepared command
	 */
	protected abstract Command prepareBuild();

	/**
	 * Implementation for executing the command
	 * 
	 * @return a String if a response was needed
	 * 
	 * @throws IOException
	 *             when a logical exception occurred
	 */
	protected abstract String invoke() throws IOException;

	/**
	 * Getter for the protocolConnection
	 * 
	 * @return the {@link FlagIEC1107Connection}
	 */
	protected FlagIEC1107Connection getConnection() {
		return this.link.getFlagIEC1107Connection();
	}

	/**
	 * Check if an error was returned in the response (indicated by an '#')
	 * 
	 * @param response
	 * 			- the response to check
	 * 
	 * @return the given String if it contains no error
	 * 
	 * @throws IOException with the proper message if an error was returned
	 */
	protected String checkResponseForErrors(String response) throws IOException {
		if(response != null && response.indexOf(ERROR_INDICATION) > -1){
			int errorCode = Integer.parseInt(response.substring(response.indexOf(ERROR_INDICATION) + 1, response.indexOf(")")));
            if (errorCode != 103) {
			    throw new LisDeviceError("Error received during read : " + ErrorCodes.getMessageForCode(errorCode));
            }
            else {
                throw new ArchiveEmptyException(ErrorCodes.getMessageForCode(103));
            }
		} else {
			return response;
		}
	}
}
