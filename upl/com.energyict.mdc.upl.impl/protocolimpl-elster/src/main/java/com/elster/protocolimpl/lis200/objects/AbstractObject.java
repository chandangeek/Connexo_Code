/**
 * 
 */
package com.elster.protocolimpl.lis200.objects;

import com.elster.protocolimpl.lis200.commands.ReadCommand;
import com.elster.protocolimpl.lis200.commands.WriteCommand;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * Abstract class for all DL220 Objects
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
@SuppressWarnings({"unused"})
public abstract class AbstractObject {

	/** A static string colon */
	protected static String COLON = ":";
	/** A static string semiColon */
	protected static String SEMI_COLON = ";";
	/** A static string dot */
	protected static String DOT = ".";

	/** The used {@link ProtocolLink} */
	protected ProtocolLink link;

	/**
	 * @return the requested instance of the object
	 */
	protected abstract int getObjectInstance();

	/**
	 * @return the initial address
	 */
	protected abstract String getInitialAddress();

	/**
	 * Initial Constructor
	 * 
	 * @param link
	 * 			- the {@link ProtocolLink}
	 */
	public AbstractObject(ProtocolLink link) {
		this.link = link;
	}
	
	/**
	 * Read the raw Value from the device (including the brackets)
	 * 
	 * @return the raw value
	 * 
	 * @throws IOException if an error occurred during the read
	 */
	public String readRawValue() throws IOException {
		ReadCommand rc = new ReadCommand(link);
		rc.setStartAddress(getStartAddress());
        return rc.invoke();
	}

	/**
	 * Getter for the default value of this object
	 * 
	 * @return the value from the readCommand of this object
	 * @throws IOException - in case of an error
	 */
	public String getValue() throws IOException {
		ReadCommand rc = new ReadCommand(link);
		rc.setStartAddress(getStartAddress());
		return ProtocolUtils.stripBrackets(rc.invoke());
	}

	/**
	 * Setter for the default value of this object
	 * @param setValue - array of bytes to write
     *
     * @throws IOException - in case of an error
	 */
	public void setValue(byte[] setValue) throws IOException {
		WriteCommand wc = new WriteCommand(link);
		wc.setStartAddress(getStartAddress());
		wc.setDataValue(setValue);
		wc.invoke();
	}

	/**
	 * Getter for the startAddress of the object
	 * 
	 * @return the StartAddress
	 */
	protected String getStartAddress() {
        return constructInstanceString() + COLON + getInitialAddress();
	}

	/**
	 * Construct a string from the instance. If only 1 digit is used add a leading zero.
	 * 
	 * @return a two digit string
	 */
	protected String constructInstanceString() {
		String instance = Integer.toString(getObjectInstance());
		if (instance.length() == 1) {
			instance = 0 + instance;
		}
		return instance;
	}
	
	/** 
	 * set link if it has been constructed with null
	 * 
	 * @param link - reference to ProtocolLink
	 */
	public void setLink(ProtocolLink link) {
		this.link = link;
	}

	/**
	 * Getter for link object
	 *  
	 * @return link - reference to ProtocolLink
	 */
	public ProtocolLink getLink() {
		return this.link;
	}

}
