package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import java.io.IOException;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Additional implementation of a commands functionality
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public abstract class AbstractCommand {

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
}
