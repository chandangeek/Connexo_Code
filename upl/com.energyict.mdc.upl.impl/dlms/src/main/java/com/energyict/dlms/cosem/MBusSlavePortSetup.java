package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;

/**
 * This {@link AbstractCosemObject} with class_id 25 allows to model and
 * configure communication channels according to EN13757-2 “M-Bus” Several
 * communication channels can be configured.
 *
 * @author jme
 */
public class MBusSlavePortSetup extends AbstractCosemObject {

	private static final int	ATTRB_LOGICAL_NAME			= 1;
	private static final int	ATTRB_DEFAULT_BAUD_RATE		= 2;
	private static final int	ATTRB_AVAILABLE_BAUD_RATES	= 3;
	private static final int	ATTRB_ADDRESS_STATE			= 4;
	private static final int	ATTRB_BUS_ADDRESS			= 5;

	private OctetString			logicalName					= null;
	private TypeEnum			defaultBaudRate				= null;
	private TypeEnum			availableBaudRates			= null;
	private TypeEnum			addressState				= null;
	private Unsigned8			busAddress					= null;

	/**
	 * Creates a new instance of MBusSlavePortSetup
	 *
	 * @param protocolLink
	 * @param objectReference
	 */
	public MBusSlavePortSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.MBUS_SLAVE_PORT_SETUP.getClassId();
	}

	/**
	 * Identifies the “M-Bus Port setup object instance.
	 *
	 * @return the logical name as {@link OctetString}
	 * @throws IOException
	 */
	public OctetString getLogicalName() throws IOException {
		logicalName = new OctetString(getLNResponseData(ATTRB_LOGICAL_NAME), 0);
		return logicalName;
	}

	/**
	 * Defines the baud rate for the opening sequence
	 *
	 * @return the default baud rate as {@link TypeEnum}
	 * @throws IOException
	 */
	public TypeEnum getDefaultBaudRate() throws IOException {
		defaultBaudRate = new TypeEnum(getLNResponseData(ATTRB_DEFAULT_BAUD_RATE), 0);
		return defaultBaudRate;
	}

	/**
	 * Defines the baud rates that can be negotiated after startup
	 *
	 * @return the available baud rates as {@link TypeEnum}
	 * @throws IOException
	 */
	public TypeEnum getAvailableBaudRates() throws IOException {
		availableBaudRates = new TypeEnum(getLNResponseData(ATTRB_AVAILABLE_BAUD_RATES), 0);
		return availableBaudRates;
	}

	/**
	 * Defines whether or not the device has been assigned an address since last
	 * power up of the device.
	 *
	 * @return the address state as {@link TypeEnum}
	 * @throws IOException
	 */
	public TypeEnum getAddressState() throws IOException {
		addressState = new TypeEnum(getLNResponseData(ATTRB_ADDRESS_STATE), 0);
		return addressState;
	}

	/**
	 * The currently assigned address on the bus for the device
	 *
	 * @return the bus address as {@link Unsigned8}
	 * @throws IOException
	 */
	public Unsigned8 getBusAddress() throws IOException {
		busAddress = new Unsigned8(getLNResponseData(ATTRB_BUS_ADDRESS), 0);
		return busAddress;
	}

}
