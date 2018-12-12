package com.elster.protocolimpl.lis200;

import com.elster.protocolimpl.lis200.objects.AbstractObject;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.objects.SimpleObject;
import com.elster.protocolimpl.lis200.objects.SoftwareVersionObject;
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

	/**
	 * Default constructor
	 *
	 * @param link - the ProtocolLink
	 */
	public Lis200ObjectFactory(ProtocolLink link) {
		this.link = link;
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
}
