package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

/**
 * Contains the Backed-Up HAN network information
 */
public class HanBackupRestoreData extends Array {

    private static final int NumberOfElements = 2;

    private static final int ExtendedPanIdIndex = 0;
    private static final int LinkKeyIndex = 1;
    private static final int NetworkKeyIndex = 2;
    private static final int BackupDataIndex = 3;

    /**
     * Creates a new instance of a HanBackupData Array
     */
    public HanBackupRestoreData() {
        super(NumberOfElements);
    }

    public HanBackupRestoreData(final byte[] berEncodedData, final int offset, final int level) throws IOException {
        super(berEncodedData, offset, level);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        if (getExtendedPanId() == null || getLinkKey() == null || getNetworkKey() == null || getBackupData() == null) {
            throw new IllegalArgumentException("Some of the HanBackupData fields are empty.");
        }
        return super.doGetBEREncodedByteArray();
    }

    public OctetString getExtendedPanId() {
        return (OctetString) getDataType(ExtendedPanIdIndex);
    }

    public void setExtendedPanId(final OctetString extendedPanId) {
        setDataType(ExtendedPanIdIndex, extendedPanId);
    }

    public OctetString getLinkKey() {
        return (OctetString) getDataType(LinkKeyIndex);
    }

    public void setLinkKey(final OctetString linkKey) {
        setDataType(LinkKeyIndex, linkKey);
    }

    public OctetString getNetworkKey() {
        return (OctetString) getDataType(NetworkKeyIndex);
    }

    public void setNetworkKey(final OctetString networkKey) {
        setDataType(NetworkKeyIndex, networkKey);
    }

    public Structure getBackupData() {
        return (Structure) getDataType(BackupDataIndex);
    }

    public void setBackupData(final Structure backupData) {
        setDataType(BackupDataIndex, backupData);
    }

    /**
     * The data to restore can not contain the DateTime OctetString
     *
     * @return the same BackupData Structure as {@link #getBackupData()} provides, but without the DateTime OctetString
     */
    public Structure getRestoreData() {
        Structure data = getBackupData();

        Structure restoreData = new Structure();
        restoreData.addDataType(data.getDataType(0));

        Array deviceBackups = (Array) data.getDataType(1);
        Array restoreBackups = new Array();

        for (AbstractDataType abstractDataType : deviceBackups.getAllDataTypes()) {
            Structure struct = (Structure) abstractDataType;
            Structure rStruct = new Structure();
            rStruct.addDataType(struct.getNextDataType());
            rStruct.addDataType(struct.getNextDataType());
            restoreBackups.addDataType(rStruct);
        }
        
        restoreData.addDataType(restoreBackups);
        return restoreData;
    }
}
