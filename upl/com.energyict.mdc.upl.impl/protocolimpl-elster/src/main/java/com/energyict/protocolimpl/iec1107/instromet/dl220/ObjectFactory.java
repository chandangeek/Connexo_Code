/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.AbstractObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ManufacturerObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.MeterTypeObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.SoftwareVersionObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.SuppliersCombinationObject;

/**
 * Straightforward implementation of protocolcommon objects of the DL220
 * 
 * @author gna
 * @since 9-feb-2010
 * 
 */
public class ObjectFactory {

	/** The {@link ProtocolLink} used */
	private ProtocolLink link;

	/** The used {@link ManufacturerObject} */
	private AbstractObject manufacurerObject;
	/** The used {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.MeterTypeObject} */
	private AbstractObject meterTypeObject;
	/** The used {@link SuppliersCombinationObject} */
	private SuppliersCombinationObject suppliersCombination;
	/** The used {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.SoftwareVersionObject} */
	private SoftwareVersionObject softwareVersionObject;

	/**
	 * Default constructor
	 * 
	 * @param link
	 */
	public ObjectFactory(ProtocolLink link) {
		this.link = link;
	}

	/**
	 * Getter for the {@link ManufacturerObject}
	 * 
	 * @return the ManufacturerObject
	 */
	public AbstractObject getManufacturerObject() {
		if (this.manufacurerObject == null) {
			this.manufacurerObject = new ManufacturerObject(link);
		}
		return this.manufacurerObject;
	}

	/**
	 * Getter for the {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.MeterTypeObject}
	 * 
	 * @return the MeterTypeObject
	 */
	public AbstractObject getMeterTypeObject() {
		if (this.meterTypeObject == null) {
			this.meterTypeObject = new MeterTypeObject(link);
		}
		return this.meterTypeObject;
	}

	/**
	 * Getter for the {@link SuppliersCombinationObject}
	 * 
	 * @return the SuppliersCombinationObject
	 */
	public SuppliersCombinationObject getSuppliersCombination() {
		if (this.suppliersCombination == null) {
			this.suppliersCombination = new SuppliersCombinationObject(link);
		}
		return this.suppliersCombination;
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
	 * Getter for the {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject}
	 * 
	 * @return the ClockObject
	 */
	public ClockObject getClockObject() {
		return new ClockObject(link);
	}

}
