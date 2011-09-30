package com.elster.protocolimpl.lis200;

import com.elster.protocolimpl.lis200.objects.*;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Straightforward implementation of common objects of the DL220
 *
 * @author gna
 * @since 9-feb-2010
 *
 * extended by gh
 *
 */
public class Lis200ObjectFactory {

	/** The {@link ProtocolLink} used */
	private ProtocolLink link;

	private AbstractObject serialNumberObject;
	/** The used ManufacturerObject} */
	private AbstractObject manufacurerObject;
	/** The used MeterTypeObject} */
	private AbstractObject meterTypeObject;
	/** The used SoftwareVersionObject} */
	private SoftwareVersionObject softwareVersionObject;
	/** The used SuppliersCombinationObject} */
	private LockObject[] locks;

	/**
	 * Default constructor
	 *
	 * @param link - the ProtocolLink
	 */
	public Lis200ObjectFactory(ProtocolLink link) {
		this.link = link;

		/* create all lock objects */
		locks = new LockObject[LockObject.MAXLOCKS];
		
		for (int i = 0; i < LockObject.MAXLOCKS; i++)
			locks[i] = new LockObject(link, LockObject.LockInstance[i]);
	}

	/**
	 * Getter for the ManufacturerObject
	 *
	 * @return the SerialNumberObject
	 */
	public AbstractObject getSerialNumberObject() {
		if (this.serialNumberObject == null) {
			this.serialNumberObject = new SimpleObject(link, 1, "180.0");
		}
		return this.serialNumberObject;
	}
	
	/**
	 * Getter for the ManufacturerObject
	 *
	 * @return the ManufacturerObject
	 */
	public AbstractObject getManufacturerObject() {
		if (this.manufacurerObject == null) {
			this.manufacurerObject = new SimpleObject(link, 2, "181.0");
		}
		return this.manufacurerObject;
	}

	/**
	 * Getter for the MeterTypeObject
	 *
	 * @return the MeterTypeObject
	 */
	public AbstractObject getMeterTypeObject() {
		if (this.meterTypeObject == null) {
			this.meterTypeObject = new SimpleObject(link, 1, "181.0");
		}
		return this.meterTypeObject;
	}

	/**
	 * Getter for the {@link SoftwareVersionObject}
	 *
	 * @return the SoftwareVersionObject
	 */
	public SoftwareVersionObject getSoftwareVersionObject() {
		if (this.softwareVersionObject == null) {
			this.softwareVersionObject = new SoftwareVersionObject(link);
		}
		return this.softwareVersionObject;
	}

	/**
	 * Getter for the {@link ClockObject}
	 *
	 * @return the ClockObject
	 */
	public ClockObject getClockObject() {
		return new ClockObject(link);
	}

	/**
	 * Getter for a LockObject (identified by lockNo
	 * 
	 * @param lockNo - identifies lock object
	 *
	 * @return a lock object for the manufacturer lock
	 */
	public LockObject getLock(int lockNo) {
		return locks[lockNo];
	}
	
	/**
	 * Getter for the manufacturerLockObject
	 *
	 * @return a lock object for the manufacturer lock
	 */
	public LockObject getManufacturerLock() {
		return locks[LockObject.MANUFACTURERLOCK];
	}

	/**
	 * Getter for the supplierLockObject
	 *
	 * @return a lock object for the supplier lock
	 */
	public LockObject getSupplierLock() {
		return locks[LockObject.SUPPLIERLOCK];
	}

	/**
	 * Getter for the customerLockObject
	 *
	 * @return a lock object for the customer lock
	 */
	public LockObject getCustomerLock() {
		return locks[LockObject.CUSTOMERLOCK];
	}

}
