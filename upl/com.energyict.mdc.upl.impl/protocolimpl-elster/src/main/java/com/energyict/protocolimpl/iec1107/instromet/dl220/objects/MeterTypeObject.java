/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * @author gna
 * @since 10-feb-2010
 * 
 */
public class MeterTypeObject extends AbstractObject {

	/** The startAddress of this object */
	private static String startAddress = "0181.0";

	/** The instance of the object */
	private int instance = 1;

	/**
	 * @param link
	 */
	public MeterTypeObject(ProtocolLink link) {
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

}
