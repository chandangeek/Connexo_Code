/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * General implementation of a DLxxx object
 * 
 * @author gna
 * @since 25-feb-2010
 *
 */
public class DLObject extends AbstractObject {
	
	public static final String ASTERISK = "[*]";
	public static final int valueIndex = 0;
	public static final int unitIndex = 1;
	
	/* Below is a list of StartAddresses of general objects */
	
	/** The StartAddress of the profile measurement period*/
	public static final String SA_PROFILEMEASUREMENT_PERIOD = "150.0";
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
	public static DLObject constructObject(ProtocolLink link, String startAddress){
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
	 * @throws IOException if read exception occurred
	 */
	public String getValue(int instanceNumber) throws IOException {
		this.instance = instanceNumber;
		return getValue();
	}
	
	/**
	 * Read the raw value form the device
	 * 
	 * @param instanceNumber
	 * 			- the number of the objects' instance which you want to rawread
	 * 
	 * @return the raw value as a string
	 * 
	 * @throws IOException if a read exception occurred
	 */
	public String readRawValue(int instanceNumber) throws IOException {
		this.instance = instanceNumber;
		return readRawValue();
	}
	
	/**
	 * Getter for the value without the Unit
	 * 
	 * @param instanceNumber
	 * 			- the number of the objects' instance which you want to read
	 * 
	 * @return the value as a String without the Unit
	 * 
	 * @throws IOException if read exception occurred
	 */
	public String getValueWithoutUnit(int instanceNumber) throws IOException {
		this.instance = instanceNumber;
		return getValue().split(ASTERISK)[valueIndex];
	}
	
	/**
	 * Getter for the Unit without the value
	 * 
	 * @param instanceNumber
	 * 			- the number of the objects' instance which you want to read
	 * 
	 * @return the unit as a String
	 * 
	 * @throws IOException if a read exception occurred
	 */
	public String getUnit(int instanceNumber) throws IOException {
		this.instance = instanceNumber;
		return getValue().split(ASTERISK)[unitIndex];
	}
}
