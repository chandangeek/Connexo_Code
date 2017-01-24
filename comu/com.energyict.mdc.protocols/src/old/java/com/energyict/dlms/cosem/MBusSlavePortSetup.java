package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * This {@link AbstractCosemObject} with class_id 25 allows to model and
 * configure communication channels according to EN13757-2 �M-Bus� Several
 * communication channels can be configured.
 *
 * @author jme
 */
public class MBusSlavePortSetup extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.24.0.0.255").getLN();

	private static final int	ATTRB_LOGICAL_NAME			= 0x00;
	private static final int	ATTRB_DEFAULT_BAUD_RATE		= 0x08;
	private static final int	ATTRB_AVAILABLE_BAUD_RATES	= 0x10;
	private static final int	ATTRB_ADDRESS_STATE			= 0x18;
	private static final int	ATTRB_BUS_ADDRESS			= 0x20;

	private OctetString logicalName					= null;
	private TypeEnum defaultBaudRate				= null;
	private TypeEnum availableBaudRates			= null;
	private TypeEnum addressState				= null;
	private Unsigned8 busAddress					= null;

	/**
	 * Creates a new instance of MBusSlavePortSetup
	 *
	 * @param protocolLink
	 * @param objectReference
	 */
	public MBusSlavePortSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * Get the default ObisCode of the object
	 * @return
	 */
	public static ObisCode getDefaultObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.MBUS_SLAVE_PORT_SETUP.getClassId();
	}

	/**
	 * Identifies the �M-Bus Port setup object instance.
	 *
	 * @return the logical name as {@link com.energyict.dlms.axrdencoding.OctetString}
	 */
	public OctetString getLogicalName() {
		try {
			logicalName = new OctetString(getResponseData(ATTRB_LOGICAL_NAME), 0);
		} catch (IOException e) {}
		return logicalName;
	}

	/**
	 * Defines the baud rate for the opening sequence
	 *
	 * @return the default baud rate as {@link com.energyict.dlms.axrdencoding.TypeEnum}
	 */
	public TypeEnum getDefaultBaudRate() {
		try {
			defaultBaudRate = new TypeEnum(getResponseData(ATTRB_DEFAULT_BAUD_RATE), 0);
		} catch (IOException e) {}
		return defaultBaudRate;
	}

	/**
	 * Defines the baud rates that can be negotiated after startup
	 *
	 * @return the available baud rates as {@link com.energyict.dlms.axrdencoding.TypeEnum}
	 */
	public TypeEnum getAvailableBaudRates() {
		try {
			availableBaudRates = new TypeEnum(getResponseData(ATTRB_AVAILABLE_BAUD_RATES), 0);
		} catch (IOException e) {}
		return availableBaudRates;
	}

	/**
	 * Defines whether or not the device has been assigned an address since last
	 * power up of the device.
	 *
	 * @return the address state as {@link com.energyict.dlms.axrdencoding.TypeEnum}
	 * @throws java.io.IOException
	 */
	public TypeEnum getAddressState() {
		try {
			addressState = new TypeEnum(getResponseData(ATTRB_ADDRESS_STATE), 0);
		} catch (IOException e) {}
		return addressState;
	}

	/**
	 * The currently assigned address on the bus for the device
	 *
	 * @return the bus address as {@link com.energyict.dlms.axrdencoding.Unsigned8}
	 * @throws java.io.IOException
	 */
	public Unsigned8 getBusAddress() {
		try {
			busAddress = new Unsigned8(getResponseData(ATTRB_BUS_ADDRESS), 0);
		} catch (IOException e) {}
		return busAddress;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		TypeEnum defaultBaudRate = getDefaultBaudRate();
		TypeEnum availableBaudRates = getAvailableBaudRates();
		TypeEnum addressState = getAddressState();
		Unsigned8 busAddress = getBusAddress();

		StringBuffer sb = new StringBuffer();
		sb.append("MBusSlavePortSetup").append(crlf);
		sb.append(" > defaultBaudRate = ").append(defaultBaudRate != null ? defaultBaudRate : null).append(crlf);
		sb.append(" > availableBaudRates = ").append(availableBaudRates != null ? availableBaudRates : null).append(crlf);
		sb.append(" > addressState = ").append(addressState != null ? addressState : null).append(crlf);
		sb.append(" > busAddress = ").append(busAddress != null ? busAddress : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getDefaultObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		return asRegisterValue();
	}

}
