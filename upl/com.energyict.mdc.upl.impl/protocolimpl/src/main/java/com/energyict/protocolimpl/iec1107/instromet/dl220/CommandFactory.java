package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Command provider
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class CommandFactory {
    
    private ProtocolLink link;

    /**
     * Default constructor
     * 
     * @param link
     *            - the link to the protocol
     */
    public CommandFactory(ProtocolLink link) {
	this.link = link;
    }
    
    

}
