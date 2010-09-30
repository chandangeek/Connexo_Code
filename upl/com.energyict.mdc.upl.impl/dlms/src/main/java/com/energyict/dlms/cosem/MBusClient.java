package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;

import java.io.IOException;

/**
 *
 * @author gna
 * This is an object form the bluebook 9th_v05
 *
 * TODO object is not complete, feel free to complete the other attributes
 */
public class MBusClient extends AbstractCosemObject{

	/* Attributes */
	private OctetString mbusPortReference = null;
	private Array captureDefinition = null;
	private Unsigned32 capturePeriod = null;
	private Unsigned8 primaryAddress = null;
	private Unsigned32 identificationNumber = null;
	private Unsigned16 manufacturereId = null;
	private Unsigned8 version = null;
	private Unsigned8 deviceType = null;
	private Unsigned8 accessNumber = null;
	private Unsigned8 status = null;
	private Unsigned8 alarm = null;

	/* Attribute numbers */
	private static final int ATTRB_MBUS_PORT_REFERENCE = 2;
	private static final int ATTRB_CAPTURE_DEFINITION = 3;
	private static final int ATTRB_CAPTURE_PERIOD = 4;
	private static final int ATTRB_PRIMARY_ADDRESS = 5;
	private static final int ATTRB_IDENTIFICATION_NUMBER = 6;
	private static final int ATTRB_MANUFACTURER_ID = 7;
	private static final int ATTRB_VERSION = 8;
	private static final int ATTRB_DEVICE_TYPE = 9;
	private static final int ATTRB_ACCESS_NUMBER = 10;
	private static final int ATTRB_STATUS = 11;
	private static final int ATTRB_ALARM = 12;

	/* ShortName attribute offsets */
	private static final int ATTRB_MBUS_PORT_REFERENCE_SN = 0x10;
	private static final int ATTRB_CAPTURE_DEFINITION_SN = 0x18;
	private static final int ATTRB_CAPTURE_PERIOD_SN = 0x20;
	private static final int ATTRB_PRIMARY_ADDRESS_SN = 0x28;
	private static final int ATTRB_IDENTIFICATION_NUMBER_SN = 0x30;
	private static final int ATTRB_MANUFACTURER_ID_SN = 0x38;
	private static final int ATTRB_VERSION_SN = 0x40;
	private static final int ATTRB_DEVICE_TYPE_SN = 0x48;
	private static final int ATTRB_ACCESS_NUMBER_SN = 0x50;
	private static final int ATTRB_STATUS_SN = 0x58;
	private static final int ATTRB_ALARM_SN = 0x60;

	/* Method invoke */
	private static final int METHOD_SLAVE_INSTALL = 1;
	private static final int METHOD_SLAVE_DEINSTALL = 2;
	private static final int METHOD_CAPTURE = 3;
	private static final int METHOD_RESET_ALARM = 4;
	private static final int METHOD_SYNCHRONIZE_CLOCK = 5;
	private static final int METHOD_DATA_SEND = 6;
	private static final int METHOD_SET_ENCRYPTION_KEY = 7;
	private static final int METHOD_TRANSFER_KEY = 8;

	/* Method write SN */
	private static final int METHOD_SLAVE_INSTALL_SN = 0x68;
	private static final int METHOD_SLAVE_DEINSTALL_SN = 0x70;
	private static final int METHOD_CAPTURE_SN = 0x78;
	private static final int METHOD_RESET_ALARM_SN = 0x80;
	private static final int METHOD_SYNCHRONIZE_CLOCK_SN = 0x88;
	private static final int METHOD_DATA_SEND_SN = 0x90;
	private static final int METHOD_SET_ENCRYPTION_KEY_SN = 0x98;
	private static final int METHOD_TRANSFER_KEY_SN = 0xA0;

	public MBusClient(ProtocolLink protocolLink, ObjectReference objectReference){
		super(protocolLink, objectReference);
	}

	protected int getClassId(){
		return DLMSClassId.MBUS_CLIENT.getClassId();
	}

	/**
	 * Get the identification number from the MBus device.
	 *
	 * @return
	 * @throws IOException
	 */
	public Unsigned32 getIdentificationNumber() throws IOException {
	    if(getObjectReference().isLNReference()){
	    	return new Unsigned32(getLNResponseData(ATTRB_IDENTIFICATION_NUMBER), 0);
	    } else {
	    	return new Unsigned32(getResponseData(ATTRB_IDENTIFICATION_NUMBER_SN), 0);
	    }
	}

	/**
	 * Get the manufacturer ID from the MBus device.
	 *
	 * @return
	 * @throws IOException
	 */
	public Unsigned16 getManufacturerID() throws IOException {
	    if(getObjectReference().isLNReference()){
	    	return new Unsigned16(getLNResponseData(ATTRB_MANUFACTURER_ID), 0);
	    } else {
	    	return new Unsigned16(getResponseData(ATTRB_MANUFACTURER_ID_SN), 0);
	    }
	}

	/**
	 * Force to install the mbus meter with the given primaryAddress
	 * @param primaryAddress
	 * @throws IOException
	 */
	public void installSlave(int primaryAddress) throws IOException{
		if(getObjectReference().isLNReference()){
			invoke(METHOD_SLAVE_INSTALL, new Integer8(primaryAddress).getBEREncodedByteArray());
		} else {
			write(METHOD_SLAVE_INSTALL_SN, new Integer8(primaryAddress).getBEREncodedByteArray());
		}
	}
	/**
	 * Force to deinstall the current slave meter
	 * @throws IOException
	 */
	public void deinstallSlave() throws IOException {
		if(getObjectReference().isLNReference()){
			invoke(METHOD_SLAVE_DEINSTALL, new Integer8(0).getBEREncodedByteArray());
		} else {
			write(METHOD_SLAVE_DEINSTALL_SN, new Integer8(0).getBEREncodedByteArray());
		}
	}

	/**
	 * Write the open key to the mbus device, this key is choosen by the manuf and used together with the default key
	 * of the device to generate an encrypted key(transportkey)
	 * If null is given as open key, then the encryption is disabled
	 * @param openKey
	 * @throws IOException
	 */
	public void setEncryptionKey(String openKey) throws IOException {
		if(openKey.equals("")){
			setEncryptionKey(new NullData().getBEREncodedByteArray());
		} else {
			setEncryptionKey(OctetString.fromString(openKey).getBEREncodedByteArray());
		}
	}

	/**
	 * Write the open key to the mbus device, this key is choosen by the manuf and used together with the default key
	 * of the device to generate an encrypted key(transportkey)
	 * If null is given as open key, then the encryption is disabled
	 * @param openKey
	 * @throws IOException
	 */
	public void setEncryptionKey(byte[] openKey) throws IOException {
		if(getObjectReference().isLNReference()){
			invoke(METHOD_SET_ENCRYPTION_KEY, new OctetString(openKey).getBEREncodedByteArray());
 		} else {
 			write(METHOD_SET_ENCRYPTION_KEY_SN, new OctetString(openKey).getBEREncodedByteArray());
 		}
	}

	/**
	 * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
	 * of the device
	 * @param encryptedkey
	 * @throws IOException
	 */
	public void setTransportKey(String encryptedkey) throws IOException {
		setTransportKey(OctetString.fromString(encryptedkey).getBEREncodedByteArray());
	}

	/**
	 * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
	 * of the device
	 * @param encryptedkey
	 * @throws IOException
	 */
	public void setTransportKey(byte[] encryptedkey) throws IOException {
		if(getObjectReference().isLNReference()){
			invoke(METHOD_TRANSFER_KEY, new OctetString(encryptedkey).getBEREncodedByteArray());
		} else {
			write(METHOD_TRANSFER_KEY_SN, new OctetString(encryptedkey).getBEREncodedByteArray());
		}
	}

	/**
	 * The AM500 modules required that the both keys are sent in one message.
	 * The structure of the message is:
	 * <pre>
	 * 		rawEncryptedKeys ::= structure
	 *			{
	 * 				OpenKey		:  OctetString
	 * 				TransferKey	:  OctetString
	 *			}
	 * </pre>
	 *
	 * @param rawEncryptedKeys
	 * 					- the rawDataStructure with the two keys
	 *
	 * @throws IOException if something went wrong during the setting of the keys
	 */
	public void setTransportKeyRawData(byte[] rawEncryptedKeys) throws IOException {
		write(METHOD_TRANSFER_KEY_SN, rawEncryptedKeys);
	}

	/**
	 * Get the deviceType from the MBus device.
	 * Description of the different types can be found in 'EN 13757-3 sub-clause 5.7, Table 3'
	 * @return
	 * @throws IOException
	 */
	public Unsigned8 getDeviceType() throws IOException{
		return new Unsigned8(getLNResponseData(ATTRB_DEVICE_TYPE), 0);
	}

	/**
	 * Write the given unsigned8 deviceType to the device
	 * NOTE: setter may not be supported by all meters.
	 * @param deviceType
	 * @throws IOException
	 */
	public void setDeviceType(Unsigned8 deviceType) throws IOException{
		write(ATTRB_DEVICE_TYPE, deviceType.getBEREncodedByteArray());
	}

	/**
	 * Write the deviceType to the meter.
	 * NOTE: setter may not be supported by all meters.
	 * @param deviceType
	 * @throws IOException
	 */
	public void setDeviceType(int deviceType) throws IOException{
		setDeviceType(new Unsigned8(deviceType));
	}

	/**
	 * Get the version from the MBus device.
	 * @return
	 * @throws IOException
	 */
	public Unsigned8 getVersion() throws IOException{
	    if(getObjectReference().isLNReference()){
	    	return new Unsigned8(getLNResponseData(ATTRB_VERSION), 0);
	    } else {
	    	return new Unsigned8(getResponseData(ATTRB_VERSION_SN), 0);
	    }
	}

	/**
	 * Write the given unsigned8 version to the device
	 * @param version
	 * @throws IOException
	 */
	public void setVersion(Unsigned8 version) throws IOException{
		write(ATTRB_VERSION, version.getBEREncodedByteArray());
	}

	/**
	 * Write the version to the meter.
	 * @param version
	 * @throws IOException
	 */
	public void setVersion(int version) throws IOException{
		setVersion(new Unsigned8(version));
	}

	/**
	 * Write the captureDefinitionBlock.
	 * The array contains one or more structures with two elements in them.
	 * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
	 * @param capDef
	 * @throws IOException
	 */
	public void writeCaptureDefinition(Array capDef) throws IOException{
		write(ATTRB_CAPTURE_DEFINITION, capDef.getBEREncodedByteArray());
	}

	/**
	 * Return the captureDefinitonBlock object
	 * The array contains one or more structures with two elements in them.
	 * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
	 * @return an array containing the capture definition of the mbus slave device
	 * @throws IOException
	 */
	public Array getCaptureDefiniton() throws IOException {
	    if(getObjectReference().isLNReference()){
	    	return new Array(getLNResponseData(ATTRB_CAPTURE_DEFINITION), 0, 0);
	    } else {
	    	return new Array(getResponseData(ATTRB_CAPTURE_DEFINITION_SN), 0, 0);
	    }

	}

	/**
	 * Getter for the capturePeriod.
	 * <pre>
	 * <li> >= 1: Automatic capturing assumed. Specifies the capture period in seconds.
	 * <li> 0: No automatic capturing: capturing is triggered externally or capture events occur asynchronously.
	 * </pre>
	 *
	 * @return the capture period in seconds
	 *
	 * @throws IOException if something goes wrong during the read
	 */
	public Unsigned32 getCapturePeriod() throws IOException{
		if(getObjectReference().isLNReference()){
			return new Unsigned32(getLNResponseData(ATTRB_CAPTURE_PERIOD), 0);
		} else {
			return new Unsigned32(getResponseData(ATTRB_CAPTURE_PERIOD_SN), 0);
		}
	}

	/**
	 * Setter for the capturePeriod
	 *
	 * @param period
	 * 			- the period in seconds
	 *
	 * @throws IOException if something goes wrong during the setting
	 */
	public void setCapturePeriod(int period) throws IOException {
		write(ATTRB_CAPTURE_PERIOD, new Unsigned32(period).getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
