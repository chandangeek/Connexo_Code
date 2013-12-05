package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.cosem.methods.MbusClientMethods;

import java.io.IOException;

/**
 * A straightforward implementation of the MbusClient object according to the DLMS BlueBooks. Versioning is applied because changes have
 * been made in the mapping of the shortnames
 */
public class MBusClient extends AbstractCosemObject {

    /**
     * Contains the version of the used BlueBook implementation
     */
    private final int version;

    /**
     * Constructor allowing you to set a BlueBook version
     *
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param objectReference the used {@link com.energyict.dlms.cosem.ObjectReference}
     * @param version         the used version
     */
    public MBusClient(ProtocolLink protocolLink, ObjectReference objectReference, int version) {
        super(protocolLink, objectReference);
        this.version = version;
    }

    /**
     * Constructor based on the BlueBook 9th edition
     *
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param objectReference the used {@link com.energyict.dlms.cosem.ObjectReference}
     * @deprecated use {@link #MBusClient(com.energyict.dlms.ProtocolLink, ObjectReference, int)} instead.
     */
	public MBusClient(ProtocolLink protocolLink, ObjectReference objectReference){
		super(protocolLink, objectReference);
        this.version = MbusClientAttributes.VERSION9;
	}

    /**
     * @inheritdoc
     */
	protected int getClassId(){
		return DLMSClassId.MBUS_CLIENT.getClassId();
	}

	/**
	 * Get the identification number from the MBus device.
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned32 getIdentificationNumber() throws IOException {
        return new Unsigned32(getResponseData(MbusClientAttributes.IDENTIFICATION_NUMBER.forVersion(getUsedVersion())), 0);
	    }

	/**
	 * Get the manufacturer ID from the MBus device.
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getManufacturerID() throws IOException {
        return new Unsigned16(getResponseData(MbusClientAttributes.MANUFACTURER_ID.forVersion(getUsedVersion())), 0);
	    }

	/**
	 * Force to install the mbus meter with the given primaryAddress
     *
	 * @param primaryAddress
	 * @throws java.io.IOException
	 */
	public void installSlave(int primaryAddress) throws IOException {
        methodInvoke(MbusClientMethods.SLAVE_INSTALL.forVersion(getUsedVersion()), new Integer8(primaryAddress));
		}

	/**
	 * Force to deinstall the current slave meter
     *
	 * @throws java.io.IOException
	 */
	public void deinstallSlave() throws IOException {
        methodInvoke(MbusClientMethods.SLAVE_DEINSTALL.forVersion(getUsedVersion()), new Integer8(0));
		}

	/**
	 * Write the open key to the mbus device, this key is choosen by the manuf and used together with the default key
	 * of the device to generate an encrypted key(transportkey)
	 * If null is given as open key, then the encryption is disabled
     *
	 * @param openKey
	 * @throws java.io.IOException
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
     *
	 * @param openKey
	 * @throws java.io.IOException
	 */
	public void setEncryptionKey(byte[] openKey) throws IOException {
        methodInvoke(MbusClientMethods.SET_ENCRYPTION_KEY.forVersion(getUsedVersion()), OctetString.fromByteArray(openKey));
 		}

	/**
	 * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
	 * of the device
     *
	 * @param encryptedkey
	 * @throws java.io.IOException
	 */
	public void setTransportKey(String encryptedkey) throws IOException {
		setTransportKey(OctetString.fromString(encryptedkey).getBEREncodedByteArray());
	}

	/**
	 * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
	 * of the device
     *
	 * @param encryptedkey
	 * @throws java.io.IOException
	 */
	public void setTransportKey(byte[] encryptedkey) throws IOException {
        methodInvoke(MbusClientMethods.TRANSFER_KEY.forVersion(getUsedVersion()), OctetString.fromByteArray(encryptedkey));
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
     * @param rawEncryptedKeys - the rawDataStructure with the two keys
	 * @throws java.io.IOException if something went wrong during the setting of the keys
	 */
	public void setTransportKeyRawData(byte[] rawEncryptedKeys) throws IOException {
        methodInvoke(MbusClientMethods.TRANSFER_KEY.forVersion(getUsedVersion()), rawEncryptedKeys);
	}

	/**
	 * Get the deviceType from the MBus device.
	 * Description of the different types can be found in 'EN 13757-3 sub-clause 5.7, Table 3'
     *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 getDeviceType() throws IOException {
        return new Unsigned8(getResponseData(MbusClientAttributes.DEVICE_TYPE.forVersion(getUsedVersion())), 0);
	}

	/**
	 * Write the given unsigned8 deviceType to the device
	 * NOTE: setter may not be supported by all meters.
     *
	 * @param deviceType
	 * @throws java.io.IOException
	 */
	public void setDeviceType(Unsigned8 deviceType) throws IOException {
        write(MbusClientAttributes.DEVICE_TYPE.forVersion(getUsedVersion()), deviceType.getBEREncodedByteArray());
	}

	/**
	 * Write the deviceType to the meter.
	 * NOTE: setter may not be supported by all meters.
     *
	 * @param deviceType
	 * @throws java.io.IOException
	 */
	public void setDeviceType(int deviceType) throws IOException {
		setDeviceType(new Unsigned8(deviceType));
	}

	/**
	 * Get the version from the MBus device.
     *
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 getVersion() throws IOException {
        return new Unsigned8(getResponseData(MbusClientAttributes.VERSION.forVersion(getUsedVersion())), 0);
	    }

	/**
	 * Write the given unsigned8 version to the device
     *
	 * @param version
	 * @throws java.io.IOException
	 */
	public void setVersion(Unsigned8 version) throws IOException {
        write(MbusClientAttributes.VERSION.forVersion(getUsedVersion()), version.getBEREncodedByteArray());
	}

	/**
	 * Write the version to the meter.
     *
	 * @param version
	 * @throws java.io.IOException
	 */
	public void setVersion(int version) throws IOException {
		setVersion(new Unsigned8(version));
	}

	/**
	 * Write the captureDefinitionBlock.
	 * The array contains one or more structures with two elements in them.
	 * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
     *
	 * @param capDef
	 * @throws java.io.IOException
	 */
	public void writeCaptureDefinition(Array capDef) throws IOException {
        write(MbusClientAttributes.CAPTURE_DEFINITION.forVersion(getUsedVersion()), capDef.getBEREncodedByteArray());
	}

	/**
	 * Return the captureDefinitonBlock object
	 * The array contains one or more structures with two elements in them.
	 * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
     *
	 * @return an array containing the capture definition of the mbus slave device
	 * @throws java.io.IOException
	 */
	public Array getCaptureDefiniton() throws IOException {
        return new Array(getResponseData(MbusClientAttributes.CAPTURE_DEFINITION.forVersion(getUsedVersion())), 0, 0);
	    }

	/**
	 * Getter for the capturePeriod.
	 * <pre>
	 * <li> >= 1: Automatic capturing assumed. Specifies the capture period in seconds.
	 * <li> 0: No automatic capturing: capturing is triggered externally or capture events occur asynchronously.
	 * </pre>
	 *
	 * @return the capture period in seconds
	 * @throws java.io.IOException if something goes wrong during the read
	 */
	public Unsigned32 getCapturePeriod() throws IOException {
        return new Unsigned32(getResponseData(MbusClientAttributes.CAPTURE_PERIOD.forVersion(getUsedVersion())), 0);
		}

	/**
	 * Setter for the capturePeriod
	 *
     * @param period - the period in seconds
	 * @throws java.io.IOException if something goes wrong during the setting
	 */
	public void setCapturePeriod(int period) throws IOException {
        write(MbusClientAttributes.CAPTURE_PERIOD.forVersion(getUsedVersion()), new Unsigned32(period).getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		return super.toString();
	}

    /**
     * @return the used BlueBook Version
     */
    private int getUsedVersion(){
        return this.version;
}
}
