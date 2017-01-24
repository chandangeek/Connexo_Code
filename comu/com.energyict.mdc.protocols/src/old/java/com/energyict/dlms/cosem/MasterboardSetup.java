package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.MasterboardSetupAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class MasterboardSetup extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.6.255");

    private static NullData SYSTEM_TITLE_TO_WRITE = new NullData();

    private Structure masterboardConfigParameters;
    private Unsigned16 localMacAddress;
    private Unsigned8 maxCredit;
    private Unsigned16 zeroCrossDelay;
    private Unsigned8 synchronisationBit;
    private OctetString localSystemTitle;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public MasterboardSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MASTERBOARD_SETUP.getClassId();
    }

    /**
     * Read the masterboard config parameters from the device
     *
     * @return the masterboard config parameters
     * @throws java.io.IOException
     */
    public Structure readMasterboardConfigParameters() throws IOException {
        this.masterboardConfigParameters = new Structure(getResponseData(MasterboardSetupAttributes.MASTERBOARD_CONFIG_PARAMETERS), 0, 0);
        try {
            this.localMacAddress = masterboardConfigParameters.getDataType(0).getUnsigned16();
            this.maxCredit = masterboardConfigParameters.getDataType(1).getUnsigned8();
            this.zeroCrossDelay = masterboardConfigParameters.getDataType(2).getUnsigned16();
            this.synchronisationBit = masterboardConfigParameters.getDataType(3).getUnsigned8();
            this.localSystemTitle = masterboardConfigParameters.getDataType(4).getOctetString();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("MasterboardSetup - Failed to parse the config parameters");

        }
        return this.masterboardConfigParameters;
    }

    /**
     * Getter for the masterborad config parameters
     *
     * @return the masterboard config parameters
     * @throws java.io.IOException
     */
    public Structure getMasterboardParameters() throws IOException {
        if (this.masterboardConfigParameters == null) {
            readMasterboardConfigParameters();
        }
        return this.masterboardConfigParameters;
    }

    /**
     * Setter for the masterboard config parameters
     *
     * @param localMacAddress
     * @throws java.io.IOException
     */
    public void writeMasterboardConfigParameters(Unsigned16 localMacAddress) throws IOException {
        checkMacAddressRange(localMacAddress);

        Structure structure = new Structure();
        structure.addDataType(localMacAddress);
        structure.addDataType(getMaxCredit());
        structure.addDataType(getZeroCrossDelay());
        structure.addDataType(getSynchronisationBit());
        structure.addDataType(SYSTEM_TITLE_TO_WRITE);

        write(MasterboardSetupAttributes.MASTERBOARD_CONFIG_PARAMETERS, structure.getBEREncodedByteArray());
        this.masterboardConfigParameters = structure;
    }

    /**
     * Setter for the masterboard config parameters
     *
     * @param localMacAddress
     * @param maxCredit
     * @throws java.io.IOException
     */
    public void writeMasterboardConfigParameters(Unsigned16 localMacAddress, Unsigned8 maxCredit, Unsigned16 zeroCrossDelay, Unsigned8 synchronisationBit) throws IOException {
        if (localMacAddress == null) {
            localMacAddress = getLocalMacAddress();
        }
        if (maxCredit == null) {
            maxCredit = getMaxCredit();
        }
        if (zeroCrossDelay == null) {
            zeroCrossDelay = getZeroCrossDelay();
        }
        if (synchronisationBit == null) {
            synchronisationBit = getSynchronisationBit();
        }
        checkMacAddressRange(localMacAddress);
        checkMaxCreditRange(maxCredit);
        checkZeroCrossDelayRange(zeroCrossDelay);
        checkSynchronisationBitRange(synchronisationBit);

        Structure structure = new Structure();
        structure.addDataType(localMacAddress);
        structure.addDataType(maxCredit);
        structure.addDataType(zeroCrossDelay);
        structure.addDataType(synchronisationBit);
        structure.addDataType(SYSTEM_TITLE_TO_WRITE);

        write(MasterboardSetupAttributes.MASTERBOARD_CONFIG_PARAMETERS, structure.getBEREncodedByteArray());
        this.masterboardConfigParameters = structure;
    }

    /**
     * Getter for the local mac address
     *
     * @return the local mac address
     * @throws java.io.IOException
     */
    public Unsigned16 getLocalMacAddress() throws IOException {
        if (this.localMacAddress == null) {
            readMasterboardConfigParameters();
        }
        return this.localMacAddress;
    }


    /**
     * Getter for the max credit
     *
     * @return the local mac address
     * @throws java.io.IOException
     */
    public Unsigned8 getMaxCredit() throws IOException {
        if (this.maxCredit == null) {
            readMasterboardConfigParameters();
        }
        return this.maxCredit;
    }

    /**
     * Getter for the zero cross delay
     *
     * @return the zero cross delay
     * @throws java.io.IOException
     */
    public Unsigned16 getZeroCrossDelay() throws IOException {
        if (this.zeroCrossDelay == null) {
            readMasterboardConfigParameters();
        }
        return this.zeroCrossDelay;
    }

    /**
     * Getter for the synchronisation bit
     *
     * @return the synchronisation bit
     * @throws java.io.IOException
     */
    public Unsigned8 getSynchronisationBit() throws IOException {
        if (this.synchronisationBit == null) {
            readMasterboardConfigParameters();
        }
        return this.synchronisationBit;
    }

    /**
     * Getter for the local system title
     *
     * @return the local system title
     * @throws java.io.IOException
     */
    public OctetString getLocalSystemTitle() throws IOException {
        if (this.localSystemTitle == null) {
            readMasterboardConfigParameters();
        }
        return this.localSystemTitle;
    }

    private void checkMacAddressRange(Unsigned16 localMacAddress) throws IOException {
        if (localMacAddress.getValue() < 0x0C00 || localMacAddress.getValue() > 0x0DFF) {
            throw new IOException("Invalid local mac address (" + localMacAddress.getValue() + "). The address should be in range [0x0C00 0x0DFF].");
        }
    }

    private void checkMaxCreditRange(Unsigned8 maxCredit) throws IOException {
        if (maxCredit.getValue() < 0 || maxCredit.getValue() > 0x07) {
            throw new IOException("Invalid max credit (" + maxCredit.getValue() + "). It should be in range 0 to 7.");
        }
    }

    private void checkZeroCrossDelayRange(Unsigned16 zeroCrossDelay) throws IOException {
        if (zeroCrossDelay.getValue() < 0 || zeroCrossDelay.getValue() > 0xFF) {
            throw new IOException("Invalid max credit (" + zeroCrossDelay.getValue() + "). It should be in range 0 to 255.");
        }
    }

    private void checkSynchronisationBitRange(Unsigned8 synchronisationBit) throws IOException {
        if (synchronisationBit.getValue() < 0 || synchronisationBit.getValue() > 0x07) {
            throw new IOException("Invalid max credit (" + synchronisationBit.getValue() + "). It should be in range 0 to 7.");
        }
    }
}
