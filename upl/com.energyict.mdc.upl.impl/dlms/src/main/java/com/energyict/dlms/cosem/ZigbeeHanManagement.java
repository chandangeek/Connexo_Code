package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.ZigbeeHanManagementAttributes;

import java.io.IOException;

/**
 * Straightforward implementation of the Zigbee HAN Management object according the the SSWG documentation. (NOTE not officially from the BlueBook yet)
 * </br>
 * Copyrights EnergyICT</br>
 * Date: 22-jul-2011</br>
 * Time: 11:48:10</br>
 */
public class ZigbeeHanManagement extends AbstractCosemObject {

    public static final byte[] LN = new byte[]{0, 0, 35, 5, 0, (byte) 255};

    private Structure blackList;
    private Structure activeDevices;
    private Structure backupData;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public ZigbeeHanManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Getter for the dlms class id
     *
     * @return the id of the dlms class
     */
    @Override
    protected int getClassId() {
        return DLMSClassId.ZIGBEE_HAN_MANAGEMENT.getClassId();
    }

    /**
     * Read the blackList attribute from the Device
     *
     * @return the up-to-date blackList
     * @throws IOException if for some reason you could not read the attribute
     */
    public Structure readBlackList() throws IOException {
        this.blackList = new Structure(getResponseData(ZigbeeHanManagementAttributes.BLACK_LIST), 0, 0);
        return this.blackList;
    }

    /**
     * Write the blackList to the device
     *
     * @param blackList the blackList to write
     * @throws IOException if for some reason you could not write the attribute
     */
    public void writeBlackList(Structure blackList) throws IOException {
        write(ZigbeeHanManagementAttributes.BLACK_LIST, blackList.getBEREncodedByteArray());
        this.blackList = blackList;
    }

    /**
     * Get the blackList attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readBlackList()} method
     *
     * @return the 'cached' blackList attribute
     * @throws IOException if for some reason the attribute could not be read from the device
     */
    public Structure getBlackList() throws IOException {
        if (this.blackList == null) {
            readBlackList();
        }
        return this.blackList;
    }

    /**
     * Read the activeDevices attribute from the device
     *
     * @return the up-to-date structure with the active devices
     * @throws IOException if for some reason we could not read the attribute
     */
    public Structure readActiveDevices() throws IOException {
        this.activeDevices = new Structure(getResponseData(ZigbeeHanManagementAttributes.ACTIVE_DEVICES), 0, 0);
        return this.activeDevices;
    }

    /**
     * Write the given activeDevices to the attribute of the device
     *
     * @param activeDevices the activeDevices to write
     * @throws IOException if for some reason we could not write the attribute
     */
    public void writeActiveDevices(Structure activeDevices) throws IOException {
        write(ZigbeeHanManagementAttributes.ACTIVE_DEVICES, activeDevices.getBEREncodedByteArray());
        this.activeDevices = activeDevices;
    }

    /**
     * Get the activeDevices attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readActiveDevices()} method
     *
     * @return the 'cached' activeDevices attribute
     * @throws IOException if for some reason we could not read the attribute
     */
    public Structure getActiveDevices() throws IOException {
        if (this.activeDevices == null) {
            readActiveDevices();
        }
        return this.activeDevices;
    }

    /**
     * Read the backupData attribute from the device
     *
     * @return the up-to-date backupData attribute
     * @throws IOException if for some reason we could not read the attribute
     */
    public Structure readBackupData() throws IOException {
        this.backupData = new Structure(getResponseData(ZigbeeHanManagementAttributes.BACKUP_DATA), 0, 0);
        return this.backupData;
    }

    /**
     * Write the given backupData to the device attribute
     *
     * @param backupData the backupData to write
     * @throws IOException if for some reason we could not write the attribute
     */
    public void writeBackupData(Structure backupData) throws IOException {
        write(ZigbeeHanManagementAttributes.BACKUP_DATA, backupData.getBEREncodedByteArray());
        this.backupData = backupData;
    }

    /**
     * Get the backupData attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readBackupData()} method
     *
     * @return the 'cached' backupData attribute
     * @throws IOException if for some reason we could not fetch the attribute
     */
    public Structure getBackupData() throws IOException {
        if (this.backupData == null) {
            readBackupData();
        }
        return this.backupData;
    }
}