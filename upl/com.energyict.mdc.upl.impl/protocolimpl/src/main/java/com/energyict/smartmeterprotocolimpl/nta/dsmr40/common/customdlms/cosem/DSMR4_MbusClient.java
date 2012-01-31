package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.attributes.DSMR4_MbusClientAttributes;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.methods.DSMR4_MbusClientMethods;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 31-aug-2011
 * Time: 14:54:13
 */
public class DSMR4_MbusClient extends MBusClient {

    private Integer8 encryptionStatus;
    private OctetString dsmrCompliancyLevel;
    private Integer8 keyStatus;

    /**
     * Constructor allowing you to set a BlueBook version
     *
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param objectReference the used {@link com.energyict.dlms.cosem.ObjectReference}
     */
    public DSMR4_MbusClient(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference, MbusClientAttributes.VERSION10);
    }

    /**
     * Constructor allowing you to set a BlueBook version
     *
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param objectReference the used {@link com.energyict.dlms.cosem.ObjectReference}
     * @param version         the used version
     */
    public DSMR4_MbusClient(ProtocolLink protocolLink, ObjectReference objectReference, int version) {
        super(protocolLink, objectReference, version);
    }

    /**
     * Read the EncryptionStatus attribute from the Device
     *
     * @return the up-to-date encryptionStatus
     * @throws IOException if for some reason you could not read the attribute
     */
    public Integer8 readEncryptionStatus() throws IOException {
        this.encryptionStatus = new Integer8(getResponseData(DSMR4_MbusClientAttributes.ENCRYPTION_STATUS), 0);
        return this.encryptionStatus;
    }

    /**
     * Get the EncryptionStatus attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readEncryptionStatus()}method
     *
     * @return the 'cached' EncryptionStatus attribute
     * @throws IOException if for some reason the attribute could not be read from the device
     */
    public Integer8 getEncryptionStatus() throws IOException {
        if (this.encryptionStatus == null) {
            readEncryptionStatus();
        }
        return this.encryptionStatus;
    }

    /**
     * Write the EncryptionStatus to the device
     *
     * @param encryptionStatus the EncryptionStatus to write
     * @throws IOException if for some reason you could not write the attribute
     */
    public void writeEncryptionStatus(Integer8 encryptionStatus) throws IOException {
        write(DSMR4_MbusClientAttributes.ENCRYPTION_STATUS, encryptionStatus.getBEREncodedByteArray());
        this.encryptionStatus = encryptionStatus;
    }

    /**
     * Read the dsmrCompliancyLevel attribute from the Device
     *
     * @return the up-to-date dsmrCompliancyLevel
     * @throws IOException if for some reason you could not read the attribute
     */
    public OctetString readDsmrCompliancyLevel() throws IOException {
        this.dsmrCompliancyLevel = new OctetString(getResponseData(DSMR4_MbusClientAttributes.DSMR_COMPLIANCY_LEVEL), 0);
        return this.dsmrCompliancyLevel;
    }

    /**
     * Get the dsmrCompliancyLevel attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readDsmrCompliancyLevel()} method
     *
     * @return the 'cached' dsmrCompliancyLevel attribute
     * @throws IOException if for some reason the attribute could not be read from the device
     */
    public OctetString getDsmrCompliancyLevel() throws IOException {
        if (this.dsmrCompliancyLevel == null) {
            readDsmrCompliancyLevel();
        }
        return this.dsmrCompliancyLevel;
    }

    /**
     * Write the dsmrCompliancyLevel to the device
     *
     * @param dsmrCompliancyLevel the dsmrCompliancyLevel to write
     * @throws IOException if for some reason you could not write the attribute
     */
    public void writeDsmrCompliancyLevel(OctetString dsmrCompliancyLevel) throws IOException {
        write(DSMR4_MbusClientAttributes.DSMR_COMPLIANCY_LEVEL, dsmrCompliancyLevel.getBEREncodedByteArray());
        this.dsmrCompliancyLevel = dsmrCompliancyLevel;
    }

    /**
     * Read the KeyStatus attribute from the Device
     *
     * @return the up-to-date KeyStatus
     * @throws IOException if for some reason you could not read the attribute
     */
    public Integer8 readKeyStatus() throws IOException {
        this.keyStatus = new Integer8(getResponseData(DSMR4_MbusClientAttributes.KEY_STATUS), 0);
        return this.keyStatus;
    }

    /**
     * Get the keyStatus attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readKeyStatus()} method
     *
     * @return the 'cached' keyStatus attribute
     * @throws IOException if for some reason the attribute could not be read from the device
     */
    public Integer8 getKeyStatus() throws IOException {
        if (this.keyStatus == null) {
            readKeyStatus();
        }
        return this.keyStatus;
    }

    /**
     * Write the keyStatus to the device
     *
     * @param keyStatus the keyStatus to write
     * @throws IOException if for some reason you could not write the attribute
     */
    public void writeKeyStatus(Integer8 keyStatus) throws IOException {
        write(DSMR4_MbusClientAttributes.KEY_STATUS, keyStatus.getBEREncodedByteArray());
        this.keyStatus = keyStatus;
    }

    /**
     * Force to install the mbus meter with the given primaryAddress
     *
     * @param primaryAddress
     * @throws java.io.IOException
     */
    @Override
    public void installSlave(final int primaryAddress) throws IOException {
        methodInvoke(DSMR4_MbusClientMethods.SLAVE_INSTALL, new Integer8(primaryAddress));
    }

    /**
     * Force to deinstall the current slave meter
     *
     * @throws java.io.IOException
     */
    @Override
    public void deinstallSlave() throws IOException {
        methodInvoke(DSMR4_MbusClientMethods.SLAVE_DEINSTALL, new Integer8(0));
    }
}
