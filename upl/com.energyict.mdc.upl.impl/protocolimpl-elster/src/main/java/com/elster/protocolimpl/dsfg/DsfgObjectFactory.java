package com.elster.protocolimpl.dsfg;

import com.elster.protocolimpl.dsfg.objects.*;


/**
 * Straightforward implementation of common objects of DSfG
 *
 * @author gh
 * @since 5/18/2010
 *
 */
public class DsfgObjectFactory {

	/** The {@link ProtocolLink} used */
	private ProtocolLink link;

	/** an object for the serial number */
	private AbstractObject serialNumberObject = null;
	/** The used ManufacturerObject */
	private AbstractObject manufacurerObject = null;
	/** The used MeterTypeObject */
	private AbstractObject meterTypeObject = null;
	/** The used SoftwareVersionObject */
	private AbstractObject softwareVersionObject = null;
	/** the used {@link ClockObject} */
	private ClockObject clockObject = null;

	/**
	 * Default constructor
	 *
	 * @param link
	 */
	public DsfgObjectFactory(ProtocolLink link) {
		this.link = link;

	}

	/**
	 * Getter for the SerialNumberObject}
	 *
	 * @return the SerialNumberObject
	 */
	public AbstractObject getSerialNumberObject() {
		if (this.serialNumberObject == null) {
			this.serialNumberObject = new SimpleObject(link, "abc");
		}
		return this.serialNumberObject;
	}
	
	/**
	 * Getter for the ManufacturerObject}
	 *
	 * @return the ManufacturerObject
	 */
	public AbstractObject getManufacturerObject() {
		if (this.manufacurerObject == null) {
			this.manufacurerObject = new SimpleObject(link, "aba");
		}
		return this.manufacurerObject;
	}

	/**
	 * Getter for the MeterTypeObject}
	 *
	 * @return the MeterTypeObject
	 */
	public AbstractObject getMeterTypeObject() {
		if (this.meterTypeObject == null) {
			this.meterTypeObject = new SimpleObject(link, "abb");
		}
		return this.meterTypeObject;
	}

	/**
	 * Getter for the SoftwareVersionObject}
	 *
	 * @return the SoftwareVersionObject
	 */
	public AbstractObject getSoftwareVersionObject() {
		if (this.softwareVersionObject == null) {
			this.softwareVersionObject = new SimpleObject(link, "abe");
		}
		return this.softwareVersionObject;
	}

	/**
	 * Getter for the {@link ClockObject}
	 *
	 * @return the ClockObject
	 */
	public ClockObject getClockObject() {
		if (this.clockObject == null) {
			this.clockObject = new ClockObject(link, "aca");
		}
		return this.clockObject;
	}
}
