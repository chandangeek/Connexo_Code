package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import java.io.IOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * The SuppliersCombinationObject lets you prepare the meter for data fetching/editing
 * 
 * @author gna
 * @since 10-feb-2010
 *
 */
public class SuppliersCombinationObject extends AbstractObject {

    /** The startAddress of this object */
    private static String startAddress = "0171.0";
    private static String lockAddress = "0170.0";

    /** The instance of the object */
    private int instance = 3;
    
    /** Indicates whether we need to lock the object again */
    private boolean lock = false;
    /**
     * @param link
     */
    public SuppliersCombinationObject(ProtocolLink link) {
	super(link);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getInitialAddress() {
	return lock ? lockAddress : startAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getObjectInstance() {
	return instance;
    }

    /**
     * Lock the object
     * 
     * @throws IOException
     * @throws ConnectionException
     * @throws FlagIEC1107ConnectionException
     */
    public void setLock() throws FlagIEC1107ConnectionException, ConnectionException, IOException {
	this.lock = true;
	setValue(new byte[] { 0x30 });
    }

}
