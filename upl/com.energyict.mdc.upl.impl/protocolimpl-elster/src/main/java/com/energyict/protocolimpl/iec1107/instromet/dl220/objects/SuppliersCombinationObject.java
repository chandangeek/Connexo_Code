package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

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
	 */
	public void setLock() throws IOException {
		this.lock = true;
		setValue(new byte[] { 0x30 });
	}

}
