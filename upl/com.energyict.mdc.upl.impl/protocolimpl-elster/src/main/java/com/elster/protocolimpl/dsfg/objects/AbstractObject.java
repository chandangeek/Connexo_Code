/**
 * 
 */
package com.elster.protocolimpl.dsfg.objects;

import com.elster.protocolimpl.dsfg.ProtocolLink;
import com.elster.protocolimpl.dsfg.telegram.DataBlock;

import java.io.IOException;

/**
 * Abstract class for all DSfG "Readout" Objects<br>
 * Due to dsfg is possible to read out more than one value in one request,<br>
 * AbstractObjects read out only <b>one</b> value in <b>one</b> request
 * 
 * 
 * @author gh
 * @since 5/14/2010
 * 
 */
public abstract class AbstractObject {

	/** The used {@link ProtocolLink} */
	protected ProtocolLink link;

	/**
	 * @return the address of the object
	 */
	protected abstract String getStartAddress();
	
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
		DataBlock db = new DataBlock(link.getRegistrationInstance(), 
				                     'A', 'J', 'M', getStartAddress());
		DataBlock in = link.getDsfgConnection().sendRequest(db);
		return in.getElementAt(0).getValue().toString();
	}

	/**
	 * Getter for the default value of this object
	 * 
	 * @return the value from the readCommand of this object
	 * @throws IOException
	 */
	public String getValue() throws IOException {
		return readRawValue();
	}

	/**
	 * Setter for the default value of this object
	 * 
	 * @throws IOException
	 */
	public void setValue(byte[] setValue) throws IOException {
		throw new IOException("setValue in DSfG not possible");
	}
}
