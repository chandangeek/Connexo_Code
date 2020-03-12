package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.MBusClientAttributes;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;
import com.energyict.dlms.cosem.methods.MBusClientMethods;

import java.io.IOException;

import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.CAPTURE_DEFINITION;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.CAPTURE_PERIOD;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.CONFIGURATION;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.DEVICE_TYPE;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.ENCRYPTION_KEY_STATUS;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.IDENTIFICATION_NUMBER;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.MANUFACTURER_ID;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.MBUS_PORT_REFERENCE;
import static com.energyict.dlms.cosem.attributes.MBusClientAttributes.PRIMARY_ADDRESS;
import static com.energyict.dlms.cosem.methods.MBusClientMethods.READ_DETAILED_VERSION_INFORMATION;
import static com.energyict.dlms.cosem.methods.MBusClientMethods.SET_ENCRYPTION_KEY;
import static com.energyict.dlms.cosem.methods.MBusClientMethods.SLAVE_DEINSTALL;
import static com.energyict.dlms.cosem.methods.MBusClientMethods.SLAVE_INSTALL;
import static com.energyict.dlms.cosem.methods.MBusClientMethods.TRANSFER_KEY;

/** implementation of the MBUSClient object according to the Appendix A.1 of the D.S.M.R. 2.3 spec D.L.M.S. Blue Books version 9 and onwards
 *
 *  The MBUSClient object is
 *  classId=72, version=0 in the D.S.M.R. 2.3 spec in Appendix A which is based on Blue Book version 8 which does not contain this object
 *  classId=72, version=0 in the Blue Book 9th edition
 *  classId=72, version=0 in the Blue Book 10th edition
 *  classId=72, version=1 in the Blue Book 11th edition
 *  classId=72, version=1 in the Blue Book 12th edition
 *
 *  Since the Blue Book version 11 and the version increase to version 1 the definition is consistent,
 *
 *  This MBUSClient object needs to be constructed with a MBusClientVersion to reflect the proper changes within the object definition
 *
 *  The following versions are identified
 *  VERSION0_D_S_M_R_23_SPEC
 *  VERSION0_BLUE_BOOK_9TH_EDITION
 *  VERSION0_BLUE_BOOK_10TH_EDITION
 *  VERSION1
 *
 *  For Reference:
 *  D.S.M.R. 2.3 is based on Blue Book 8th edition and should use VERSION0_D_S_M_R_23_SPEC
 *  D.S.M.R. 4.2 is based on Blue Book 10th edition and should use VERSION0_BLUE_BOOK_10TH_EDITION
 *  E.S.M.R. 5.0 is based on Blue Book 12th edition and should use VERSION1
 *      the following E.S.M.R. 5.0 attributes and methods are topped on top of the Blue book
 *      Attributes:
 *      -1  FUAK Status                                 enum
 *
 *      Methods:
 *      -1  Transfer FUAK(data)                         octet string
 *      -2  Read Detailed version information(data)     unsigned
 *
 *
 *
 *  D.S.M.R. 2.3 Appendix A.1
 *
 *  Class_id=72, version=0
 *
 *  Attribute                               Data type                       Short name
 *  1.      logical name                    octet-string                    x
 *  2.      Mbus Port Reference             octet-string                    x + 0x10
 *  3.      Capture Definition              array                           x + 0x18
 *  4.      Capture Period                  double-long-unsigned            x + 0x20
 *  5.      Primary Address                 unsigned                        x + 0x28
 *  6.      Identification number           double-long-unsigned            x + 0x30
 *  7.      Manufacturer id                 long-unsigned                   x + 0x38
 *  8.      Version                         unsigned                        x + 0x40
 *  9.      Device Type                     unsigned                        x + 0x48
 *  10.     Access Number                   unsigned                        x + 0x50
 *  11.     Status                          unsigned                        x + 0x58
 *  12.     Alarm                           unsigned                        x + 0x60
 *
 *  Method
 *  1.      Slave Install                   integer                         x + 0x68
 *  2.      Slave de-install                integer                         x + 0x70
 *  3.      Capture                         integer                         x + 0x78
 *  4.      Reset Alarm                     integer                         x + 0x80
 *  5.      Synchronize Clock               long-unsigned                   x + 0x88
 *  6.      Data Send                       array data_definition_element   x + 0x90
 *  7.      Set Encryption Key              not specified                   x + 0x98
 *
 *
 *
 *
 *  Blue Book 9th edition Chapter 4.7.2
 *
 *  Class_id=72, version=0
 *
 *  Attribute                               Data type                       Short name
 *  1.      logical name                    octet-string                    x
 *  2.      Mbus Port Reference             octet-string                    x + 0x10
 *  3.      Capture Definition              array                           x + 0x18
 *  4.      Capture Period                  double-long-unsigned            x + 0x20
 *  5.      Primary Address                 unsigned                        x + 0x28
 *  6.      Identification number           double-long-unsigned            x + 0x30
 *  7.      Manufacturer id                 long-unsigned                   x + 0x38
 *  8.      Version                         unsigned                        x + 0x40
 *  9.      Device Type                     unsigned                        x + 0x48
 *  10.     Access Number                   unsigned                        x + 0x50
 *  11.     Status                          unsigned                        x + 0x58
 *  12.     Alarm                           unsigned                        x + 0x60
 *
 *  Method
 *  1.      Slave Install                   integer                         x + 0x68
 *  2.      Slave de-install                integer                         x + 0x70
 *  3.      Capture                         integer                         x + 0x78
 *  4.      Reset Alarm                     integer                         x + 0x80
 *  5.      Synchronize Clock               integer                         x + 0x88
 *  6.      Data Send                       array data_definition_element   x + 0x90
 *  7.      Set Encryption Key              octet string                    x + 0x98
 *  8.      Transfer Key                    octet string                    x + 0xA0
 *
 *
 *
 *
 *  Blue Book 10th edition Chapter 4.7.2
 *
 *  Class_id=72, version=0, NOTE: In this Edition 10, the mapping of the attributes short name has ben corrected without a change in the version
 *
 *  Attribute                               Data type                       Short name
 *  1.      logical name                    octet-string                    x
 *  2.      Mbus Port Reference             octet-string                    x + 0x00
 *  3.      Capture Definition              array                           x + 0x10
 *  4.      Capture Period                  double-long-unsigned            x + 0x18
 *  5.      Primary Address                 unsigned                        x + 0x20
 *  6.      Identification number           double-long-unsigned            x + 0x28
 *  7.      Manufacturer id                 long-unsigned                   x + 0x30
 *  8.      Version                         unsigned                        x + 0x38
 *  9.      Device Type                     unsigned                        x + 0x40
 *  10.     Access Number                   unsigned                        x + 0x48
 *  11.     Status                          unsigned                        x + 0x50
 *  12.     Alarm                           unsigned                        x + 0x58
 *
 *  Method
 *  1.      Slave Install                   unsigned                        x + 0x60
 *  2.      Slave de-install                integer                         x + 0x68
 *  3.      Capture                         integer                         x + 0x70
 *  4.      Reset Alarm                     integer                         x + 0x78
 *  5.      Synchronize Clock               integer                         x + 0x80
 *  6.      Data Send                       array data_definition_element   x + 0x88
 *  7.      Set Encryption Key              octet string                    x + 0x90
 *  8.      Transfer Key                    octet string                    x + 0x98
 *
 *
 *
 *
 *  Blue Book 11th edition Chapter 4.7.2
 *
 *  Class_id=72, version=1, NOTE: This new version 1 of the "MBUS Client" IC is in line with EN 13757-3-2013
 *
 *  Attribute                               Data type                       Short name
 *  1.      logical name                    octet-string                    x
 *  2.      Mbus Port Reference             octet-string                    x + 0x00
 *  3.      Capture Definition              array                           x + 0x10
 *  4.      Capture Period                  double-long-unsigned            x + 0x18
 *  5.      Primary Address                 unsigned                        x + 0x20
 *  6.      Identification number           double-long-unsigned            x + 0x28
 *  7.      Manufacturer id                 long-unsigned                   x + 0x30
 *  8.      Version                         unsigned                        x + 0x38
 *  9.      Device Type                     unsigned                        x + 0x40
 *  10.     Access Number                   unsigned                        x + 0x48
 *  11.     Status                          unsigned                        x + 0x50
 *  12.     Alarm                           unsigned                        x + 0x58
 *  13.     Configuration                   long-unsigned                   x + 0x60
 *  14.     Encryption Key Status           enum                            x + 0x68
 *
 *  Method
 *  1.      Slave Install                   unsigned                        x + 0x70
 *  2.      Slave de-install                unsigned                        x + 0x78
 *  3.      Capture                         integer                         x + 0x80
 *  4.      Reset Alarm                     integer                         x + 0x88
 *  5.      Synchronize Clock               integer                         x + 0x90
 *  6.      Data Send                       array data_definition_element   x + 0x98
 *  7.      Set Encryption Key              octet string                    x + 0xA0
 *  8.      Transfer Key                    octet string                    x + 0xA8
 *
 *
 *
 *  Blue Book 12th edition Chapter 4.8.3
 *
 *  Class_id=72, version=1,
 *
 *  Attribute                               Data type                       Short name
 *  1.      logical name                    octet-string                    x
 *  2.      Mbus Port Reference             octet-string                    x + 0x00
 *  3.      Capture Definition              array                           x + 0x10
 *  4.      Capture Period                  double-long-unsigned            x + 0x18
 *  5.      Primary Address                 unsigned                        x + 0x20
 *  6.      Identification number           double-long-unsigned            x + 0x28
 *  7.      Manufacturer id                 long-unsigned                   x + 0x30
 *  8.      Version                         unsigned                        x + 0x38
 *  9.      Device Type                     unsigned                        x + 0x40
 *  10.     Access Number                   unsigned                        x + 0x48
 *  11.     Status                          unsigned                        x + 0x50
 *  12.     Alarm                           unsigned                        x + 0x58
 *  13.     Configuration                   long-unsigned                   x + 0x60
 *  14.     Encryption Key Status           enum                            x + 0x68
 *
 *  Method
 *  1.      Slave Install                   unsigned                        x + 0x70
 *  2.      Slave de-install                unsigned                        x + 0x78
 *  3.      Capture                         integer                         x + 0x80
 *  4.      Reset Alarm                     integer                         x + 0x88
 *  5.      Synchronize Clock               integer                         x + 0x90
 *  6.      Data Send                       array data_definition_element   x + 0x98
 *  7.      Set Encryption Key              octet string                    x + 0xA0
 *  8.      Transfer Key                    octet string                    x + 0xA8
 *
 *
 *
 */

public class MBusClient extends AbstractCosemObject {

    public enum VERSION {
        VERSION0_D_S_M_R_23_SPEC(0,0 ),             // used for D.S.M.R. 2.3 only
        VERSION0_BLUE_BOOK_9TH_EDITION(0, 1),
        VERSION0_BLUE_BOOK_10TH_EDITION(0, 2),      // used in D.S.M.R. 4.x
        VERSION1(1, 3);                             // used in E.S.M.R. 5.0

        private int version;
        private int index;

        VERSION(int version, int index) {
            this.version = version;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public int getVersion() {
            return version;
        }
    }

    private final VERSION version;

    /**
     * Constructor allowing you to set a BlueBook version
     *
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param objectReference the used {@link com.energyict.dlms.cosem.ObjectReference}
     * @param version         the used version
     */
    public MBusClient(ProtocolLink protocolLink, ObjectReference objectReference, VERSION version) {
        super(protocolLink, objectReference);
        this.version = version;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MBUS_CLIENT.getClassId();
    }

    public OctetString getMBusPortReference() throws IOException {
        return new OctetString(getResponseData(IDENTIFICATION_NUMBER.forVersion(getUsedVersion())));
    }

    public void setMBusPortReference(OctetString octetString) throws IOException {
        write(MBUS_PORT_REFERENCE.forVersion(getUsedVersion()), octetString.getBEREncodedByteArray());
    }

    /**
     * Write the captureDefinitionBlock.
     * The array contains one or more structures with two elements in them.
     * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
     *
     * @param capDef the array to write
     * @throws IOException when an IOException occurs
     */
    public void writeCaptureDefinition(Array capDef) throws IOException {
        write(CAPTURE_DEFINITION.forVersion(getUsedVersion()), capDef.getBEREncodedByteArray());
    }

    /**
     * Return the captureDefinitonBlock object
     * The array contains one or more structures with two elements in them.
     * The first is the DIB(DataInformationBlock) octetString, the other is the VIB(ValueInformationBlock) octetString
     *
     * @return an array containing the capture definition of the mbus slave device
     * @throws java.io.IOException when an IOException occurs
     */
    public Array getCaptureDefiniton() throws IOException {
        return new Array(getResponseData(CAPTURE_DEFINITION.forVersion(getUsedVersion())), 0, 0);
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
        return new Unsigned32(getResponseData(CAPTURE_PERIOD.forVersion(getUsedVersion())), 0);
    }

    /**
     * Setter for the capturePeriod
     *
     * @param period - the period in seconds
     * @throws java.io.IOException if something goes wrong during the setting
     */
    public void setCapturePeriod(int period) throws IOException {
        write(CAPTURE_PERIOD.forVersion(getUsedVersion()), new Unsigned32(period).getBEREncodedByteArray());
    }

    public Unsigned8 getPrimaryAddress() throws IOException {
        return new Unsigned8(getResponseData(PRIMARY_ADDRESS.forVersion(getUsedVersion())), 0);
    }

    /**
     * Get the identification number from the MBus device.
     *
     * @return Unsigned32 which holds the identification Number
     * @throws IOException when an IOException occurs
     */
    public Unsigned32 getIdentificationNumber() throws IOException {
        return new Unsigned32(getResponseData(IDENTIFICATION_NUMBER.forVersion(getUsedVersion())), 0);
    }

    public void setIdentificationNumber(Unsigned32 identificationNumber) throws IOException {
        write(IDENTIFICATION_NUMBER.forVersion(getUsedVersion()), identificationNumber.getBEREncodedByteArray());
    }

    /**
     * Get the manufacturer ID from the MBus device.
     *
     * @return an Unsigned32 holding the value for read for Manufacturer ID
     * @throws java.io.IOException when an IOException occurs
     */
    public Unsigned16 getManufacturerID() throws IOException {
        return new Unsigned16(getResponseData(MANUFACTURER_ID.forVersion(getUsedVersion())), 0);
    }

    public void setManufacturerID(Unsigned16 manufacturerID) throws IOException {
        write(MANUFACTURER_ID.forVersion(getUsedVersion()), manufacturerID.getBEREncodedByteArray());
    }

    /**
     * Get the version from the MBus device.
     *
     * @return an Unsigned8 holding the value read for the attribute version
     * @throws IOException when an IOException occurs
     */
    public Unsigned8 getVersion() throws IOException {
        return new Unsigned8(getResponseData(MBusClientAttributes.VERSION.forVersion(getUsedVersion())), 0);
    }

    /**
     * Write the given unsigned8 version to the device
     *
     * @param version an Unsigned8 holding the version to write
     * @throws java.io.IOException when an IOException occurs
     */
    public void setVersion(Unsigned8 version) throws IOException {
        write(MBusClientAttributes.VERSION.forVersion(getUsedVersion()), version.getBEREncodedByteArray());
    }

    /**
     * Write the version to the meter.
     *
     * @param version an int holding the version to write
     * @throws IOException when an IOException occurs
     */
    public void setVersion(int version) throws IOException {
        setVersion(new Unsigned8(version));
    }

    /**
     * Get the deviceType from the MBus device.
     * Description of the different types can be found in 'EN 13757-3 sub-clause 5.7, Table 3'
     *
     * @return Unsigned8 holding the Device Type read from the device
     * @throws IOException when an IOException occurs
     */
    public Unsigned8 getDeviceType() throws IOException {
        return new Unsigned8(getResponseData(DEVICE_TYPE.forVersion(getUsedVersion())), 0);
    }

    /**
     * Write the given unsigned8 deviceType to the device
     * NOTE: setter may not be supported by all meters.
     *
     * @param deviceType an Unsigned8 holding the DeviceType to write
     * @throws java.io.IOException when an IOException occurs
     */
    public void setDeviceType(Unsigned8 deviceType) throws IOException {
        write(DEVICE_TYPE.forVersion(getUsedVersion()), deviceType.getBEREncodedByteArray());
    }

    /**
     * Write the deviceType to the meter.
     * NOTE: setter may not be supported by all meters.
     *
     * @param deviceType an int holding the DeviceType to Write
     * @throws IOException when an IOException occurs
     */
    public void setDeviceType(int deviceType) throws IOException {
        setDeviceType(new Unsigned8(deviceType));
    }

    /**
     * Write the keyStatus to the device
     *
     * @param keyStatus the keyStatus to write
     * @throws IOException if for some reason you could not write the attribute
     */
    public void writeKeyStatus(TypeEnum keyStatus) throws IOException {
        write(ENCRYPTION_KEY_STATUS.forVersion(getUsedVersion()), keyStatus.getBEREncodedByteArray());
    }

    /**
     * Read the EncryptionStatus attribute from the Device
     *
     * @return the up-to-date encryptionStatus
     * @throws IOException if for some reason you could not read the attribute
     */
    public Unsigned16 readEncryptionStatus() throws IOException {
        Unsigned16 encryptionStatus = new Unsigned16(getResponseData(CONFIGURATION.forVersion(getUsedVersion())), 0);
        return encryptionStatus;
    }

    /**
     * Read the KeyStatus attribute from the Device
     *
     * @return the up-to-date KeyStatus
     * @throws IOException if for some reason you could not read the attribute
     */
    public TypeEnum readKeyStatus() throws IOException {
        return new TypeEnum(getResponseData(ENCRYPTION_KEY_STATUS.forVersion(getUsedVersion())), 0);
    }

    /**
     * Read the KeyStatus attribute from the Devices and returns the meaningful description
     * @return
     */
    public String readKeyStatusAsText() throws IOException {
        TypeEnum keyStatus = readKeyStatus();
        int value = keyStatus.getValue();
        switch (value){
            case 0: return "no encryption_key";
            case 1: return "encryption_key set but not in use by E-meter";
            case 2: return "encryption_key transferred";
            case 3: return "encryption_key set and transferred to G-meter and in use by E-mete";
            case 4: return "encryption_key set and in use by E and G-meter";
        }

        return "unknown status ("+value+")";
    }

    /**
     * Force to install the mbus meter with the given primaryAddress
     *
     * @param primaryAddress an int holding the primary address to write
     * @throws IOException when an IOException occurs
     */
    public void installSlave(int primaryAddress) throws IOException {
        switch (version) {
            case VERSION0_D_S_M_R_23_SPEC:
            case VERSION0_BLUE_BOOK_9TH_EDITION:
                methodInvoke(SLAVE_INSTALL.forVersion(getUsedVersion()), new Integer8(primaryAddress));
                break;
            case VERSION0_BLUE_BOOK_10TH_EDITION:
            case VERSION1:
                methodInvoke(SLAVE_INSTALL.forVersion(getUsedVersion()), new Unsigned8(primaryAddress));
                break;
        }
    }

    /**
     * Force to deinstall the current slave meter
     *
     * @throws IOException when an IOException occurs
     */
    public void deinstallSlave() throws IOException {
        switch (version) {
            case VERSION0_D_S_M_R_23_SPEC:
            case VERSION0_BLUE_BOOK_9TH_EDITION:
            case VERSION0_BLUE_BOOK_10TH_EDITION:
                methodInvoke(SLAVE_DEINSTALL.forVersion(getUsedVersion()), new Integer8(0));
                break;
            case VERSION1:
                methodInvoke(SLAVE_DEINSTALL.forVersion(getUsedVersion()), new Unsigned8(0));
                break;
        }

    }

    /**
     * Write the open key to the mbus device, this key is choosen by the manuf and used together with the default key
     * of the device to generate an encrypted key(transportkey)
     * If null is given as open key, then the encryption is disabled
     *
     * @param openKey a String holding the key to write
     * @throws IOException when an IOException occurs
     */
    public void setEncryptionKey(String openKey) throws IOException {
        if (openKey.equals("")) {
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
     * @param openKey a byte[] holding the open key to write
     * @throws IOException when an IOException occurs
     */
    public void setEncryptionKey(byte[] openKey) throws IOException {
        methodInvoke(SET_ENCRYPTION_KEY.forVersion(getUsedVersion()), OctetString.fromByteArray(openKey));
    }

    /**
     * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
     * of the device
     *
     * @param encryptedkey a String holding the encrypted key to write
     * @throws IOException when an IOException occurs
     */
    public void setTransportKey(String encryptedkey) throws IOException {
        setTransportKey(OctetString.fromString(encryptedkey).getBEREncodedByteArray());
    }

    /**
     * Write the encrypted key to the mbus device, this key is generated by the open key and the default key
     * of the device
     *
     * @param encryptedKey a byte[] holding the encrypted key
     * @throws IOException when an IOException occurs
     */
    public void setTransportKey(byte[] encryptedKey) throws IOException {
        methodInvoke(TRANSFER_KEY.forVersion(getUsedVersion()), OctetString.fromByteArray(encryptedKey));
    }

    /**
     * The AM500 modules required that the both keys are sent in one message.
     * The structure of the message is:
     * <pre>
     * 		rawEncryptedKeys ::= structure
     *            {
     * 				OpenKey		:  OctetString
     * 				TransferKey	:  OctetString
     *            }
     * </pre>
     *
     * @param rawEncryptedKeys - the rawDataStructure with the two keys
     * @throws java.io.IOException if something went wrong during the setting of the keys
     */
    public void setTransportKeyRawData(byte[] rawEncryptedKeys) throws IOException {
        methodInvoke(TRANSFER_KEY.forVersion(getUsedVersion()), rawEncryptedKeys);
    }

    /**
     * E.S.M.R. 5.0 only method, not part of the Blue Book 12th Edition
     *
     * @param encryptedRequest - a byte[] with the encrypted key to transfer
     * @throws IOException if something went wrong during the setting of the keys
     */
    public void transferFUAK(byte[] encryptedRequest) throws IOException {
        DLMSClassMethods method = MBusClientMethods.TRANSFER_FUAK.forVersion(getUsedVersion());
        OctetString fuak = new OctetString(encryptedRequest);
        methodInvoke(method, fuak.getBEREncodedByteArray());
    }

    /**
     * E.S.M.R. 5.0 only method, not part of the Blue Book 12th Edition
     *
     * This method can be successfully invoked only if the encryption_key_status = 4 (encryption_key set and in use by E and G-meter).
     * If no encryption is active, other_reason must be sent.
     * If the read_detailed_version_info method is invoked with “0” as a parameter, then the detailed version information fields are all read
     * and stored in attribute 2 of the M-Bus Device configuration object (0-x:24.2.2.255), as soon as the FAC is available
     *
     * @param data - an Unsigned8 with the data
     * @throws IOException if something went wrong
     */
    public byte[] readDetailedVersionInformation(Unsigned8 data) throws IOException {
        return methodInvoke(READ_DETAILED_VERSION_INFORMATION.forVersion(getUsedVersion()), data.getBEREncodedByteArray());
    }


    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @return the used BlueBook Version
     */
    protected VERSION getUsedVersion() {
        return this.version;
    }
}
