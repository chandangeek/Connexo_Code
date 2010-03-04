/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import java.io.IOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * General implementation of a DLxxx object
 * 
 * @author gna
 * @since 25-feb-2010
 *
 */
public class DLObject extends AbstractObject {
	
	/* Below is a list of StartAddresses of general objects */
	
	/** The StartAddress of the SerialNumber */
	public static String SA_SERIALNUMBER = "180.0";
	

    /** The startAddress of this object */
    private String startAddress = "";
	/** The instance of the object */
	private int instance = 0;
	
	/**
	 * Private constructor
	 * @param link
	 */
	private DLObject(ProtocolLink link) {
		super(link);
	}

	/**
	 * Construct a basic DLxxx Object. 
	 * 
	 * @param link	
	 * 			- the {@link ProtocolLink} to use
	 * 
	 * @param startAddress
	 * 			- the startAddress of the Object
	 * @return
	 */
	public static final DLObject constructObject(ProtocolLink link, String startAddress){
		DLObject object = new DLObject(link);
		object.setInitialAddress(startAddress);
		return object;
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
	 * Setter of the initialAddress
	 * 
	 * @param initialAddress
	 * 				- the initialAddress
	 */
	protected void setInitialAddress(String initialAddress){
		this.startAddress = initialAddress;
	}

	/**
	 * Getter for the value
	 * 
	 * @param instanceNumber
	 *            - the number of the objects' instance which you want to read
	 *            
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
