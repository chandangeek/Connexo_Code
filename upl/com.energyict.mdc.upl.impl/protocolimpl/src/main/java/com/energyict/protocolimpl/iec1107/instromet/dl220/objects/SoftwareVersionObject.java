/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import java.io.IOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * @author gna
 * @since 10-feb-2010
 *
 */
public class SoftwareVersionObject extends AbstractObject {
    
    /** The startAddress of this object */
    private static String startAddress = "0190.0";

    /** The instance of the object */
    private int instance = 2;

    /**
     * @param link
     */
    public SoftwareVersionObject(ProtocolLink link) {
	super(link);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getInitialAddress() {
	return startAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getObjectInstance() {
	return instance;
    }
    
    /**
     * Getter for the value
     * 
     * @param instanceNumber
     *            - the number of the objects' instance which you want to read
     * @return the value as a String
     * @throws FlagIEC1107ConnectionException
     * @throws ConnectionException
     * @throws IOException
     */
    public String getValue(int instanceNumber) throws FlagIEC1107ConnectionException, ConnectionException, IOException {
	this.instance = instanceNumber;
	return getValue();
    }

}
