package com.elster.protocolimpl.dlms;


import com.elster.protocolimpl.dlms.util.ProtocolLink;

/**
 * Straightforward implementation of common objects of DLMS
 *
 * @author gh
 * @since 5/18/2010
 *
 */
@SuppressWarnings({"unused"})
public class DlmsObjectFactory {

    /** an object for the serial number */
	private Object serialNumberObject = null;
	/** The used ManufacturerObject */
	private Object manufacurerObject = null;
	/** The used MeterTypeObject */
	private Object meterTypeObject = null;
	/** The used SoftwareVersionObject */
	private Object softwareVersionObject = null;
	/** the used {@link com.elster.protocolimpl.dsfg.objects.ClockObject} */
	private Object clockObject = null;

	/**
	 * Default constructor
     *
     * @param link - reference to ProtocolLink
	 */
	public DlmsObjectFactory(ProtocolLink link) {
	}

	/**
	 * Getter for the SerialNumberObject}
	 *
	 * @return the SerialNumberObject
	 */
	public Object getSerialNumberObject() {
		if (this.serialNumberObject == null) {
			this.serialNumberObject = new Object();
		}
		return this.serialNumberObject;
	}

	/**
	 * Getter for the ManufacturerObject}
	 *
	 * @return the ManufacturerObject
	 */
	public Object getManufacturerObject() {
		if (this.manufacurerObject == null) {
			this.manufacurerObject = new Object();
		}
		return this.manufacurerObject;
	}

	/**
	 * Getter for the MeterTypeObject}
	 *
	 * @return the MeterTypeObject
	 */
	public Object getMeterTypeObject() {
		if (this.meterTypeObject == null) {
			this.meterTypeObject = new Object();
		}
		return this.meterTypeObject;
	}

	/**
	 * Getter for the SoftwareVersionObject}
	 *
	 * @return the SoftwareVersionObject
	 */
	public Object getSoftwareVersionObject() {
		if (this.softwareVersionObject == null) {
			this.softwareVersionObject = new Object();
		}
		return this.softwareVersionObject;
	}

	/**
	 * Getter for the {@link com.elster.protocolimpl.dsfg.objects.ClockObject}
	 *
	 * @return the ClockObject
	 */
	public Object getClockObject() {
		if (this.clockObject == null) {
			this.clockObject = new Object();
		}
		return this.clockObject;
	}
}
