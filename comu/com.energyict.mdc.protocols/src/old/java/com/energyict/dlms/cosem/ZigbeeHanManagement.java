/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.ZigbeeHanManagementAttributes;
import com.energyict.dlms.cosem.methods.ZigbeeHanManagementMethods;

import java.io.IOException;

public class ZigbeeHanManagement extends AbstractCosemObject {

    public static final byte[] LN = new byte[]{0, 0, 35, 5, 0, (byte) 255};

    private Array blackList;
    private Array activeDevices;
    private Structure backupData;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     */
    public ZigbeeHanManagement(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
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
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public Array readBlackList() throws IOException {
        this.blackList = new Array(getResponseData(ZigbeeHanManagementAttributes.BLACK_LIST), 0, 0);
        return this.blackList;
    }

    /**
     * Write the blackList to the device
     *
     * @param blackList the blackList to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writeBlackList(Array blackList) throws IOException {
        write(ZigbeeHanManagementAttributes.BLACK_LIST, blackList.getBEREncodedByteArray());
        this.blackList = blackList;
    }

    /**
     * Get the blackList attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readBlackList()} method
     *
     * @return the 'cached' blackList attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public Array getBlackList() throws IOException {
        if (this.blackList == null) {
            readBlackList();
        }
        return this.blackList;
    }

    /**
     * Read the activeDevices attribute from the device
     *
     * @return the up-to-date structure with the active devices
     * @throws java.io.IOException if for some reason we could not read the attribute
     */
    public Array readActiveDevices() throws IOException {
        this.activeDevices = new Array(getResponseData(ZigbeeHanManagementAttributes.ACTIVE_DEVICES), 0, 0);
        return this.activeDevices;
    }

    /**
     * Write the given activeDevices to the attribute of the device
     *
     * @param activeDevices the activeDevices to write
     * @throws java.io.IOException if for some reason we could not write the attribute
     */
    public void writeActiveDevices(Array activeDevices) throws IOException {
        write(ZigbeeHanManagementAttributes.ACTIVE_DEVICES, activeDevices.getBEREncodedByteArray());
        this.activeDevices = activeDevices;
    }

    /**
     * Get the activeDevices attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readActiveDevices()} method
     *
     * @return the 'cached' activeDevices attribute
     * @throws java.io.IOException if for some reason we could not read the attribute
     */
    public Array getActiveDevices() throws IOException {
        if (this.activeDevices == null) {
            readActiveDevices();
        }
        return this.activeDevices;
    }

    /**
     * Read the backupData attribute from the device
     *
     * @return the up-to-date backupData attribute
     * @throws java.io.IOException if for some reason we could not read the attribute
     */
    public Structure readBackupData() throws IOException {
        byte[] responseData = getResponseData(ZigbeeHanManagementAttributes.BACKUP_DATA);
        System.out.println(DLMSUtils.getHexStringFromBytes(responseData));
        this.backupData = new Structure(responseData, 0, 0);
        return this.backupData;
    }

    /**
     * Write the given backupData to the device attribute
     *
     * @param backupData the backupData to write
     * @throws java.io.IOException if for some reason we could not write the attribute
     */
    public void writeBackupData(Structure backupData) throws IOException {
        write(ZigbeeHanManagementAttributes.BACKUP_DATA, backupData.getBEREncodedByteArray());
        this.backupData = backupData;
    }

    /**
     * Get the backupData attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readBackupData()} method
     *
     * @return the 'cached' backupData attribute
     * @throws java.io.IOException if for some reason we could not fetch the attribute
     */
    public Structure getBackupData() throws IOException {
        if (this.backupData == null) {
            readBackupData();
        }
        return this.backupData;
    }

    /**
     * Inform the HUB to backup all ZigBee devices
     *
     * @param data additional data
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] backup(AbstractDataType data) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.BACKUP, data);
    }

    /**
     * Inform the HUB to backup all ZigBee devices. Default additional data will be a {@link com.energyict.dlms.axrdencoding.Integer8} with value '0'.
     *
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] backup() throws IOException {
        byte[] bytes = new byte[0];
        try {
            bytes = methodInvoke(ZigbeeHanManagementMethods.BACKUP, new NullData());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return bytes;
    }

    /**
     * Restore previously backup data to the HUB.
     *
     * @param backUp the data to restore, format:<br>
     *               structure:: = backUp<br>
     *               {<br>
     *               array[10]:: = Network Backup<br>
     *               {<br>
     *               Mac address = {43,0-0:25.2.xx.255,2,0}<br>
     *               Network_Link_Key = Octet-string[20] (# this is the old LINK_KEY#)<br>
     *               }<br>
     *               }
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] restore(Structure backUp) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.RESTORE, backUp);
    }

    /**
     * Inform the HUB to create a HAN network
     *
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] createHan() throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.CREATE_HAN, new Integer8(0));
    }

    /**
     * Inform the HUB to remove a HAN network
     *
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] removeHan() throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.REMOVE_HAN, new Integer8(0));
    }

    /**
     * Request the HUB to identify a certain device.
     *
     * @param identificationData defining which device needs to identify, format:<br>
     *                           {<br>
     *                           Mac address = {43,0-0:25.2.xx.255,2,0}<br>
     *                           IdentfiyTime = long-unsigned (seconds)<br>
     *                           }
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] identifyDevice(Structure identificationData) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.IDENTIFY_DEVICE, identificationData);
    }

    /**
     * Inform the HUB to remove his ZigBee mirror
     *
     * @param mirrorDataStructure additional data
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] removeMirror(Structure mirrorDataStructure) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.REMOVE_MIRROR, mirrorDataStructure);
    }

    /**
     * Inform the HUB to update his network key
     *
     * @param data additional data
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] updateNetworkKeys(AbstractDataType data) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.UPDATE_NETWORK_KEYS, data);
    }

    /**
     * Inform the HUB to update his link key
     *
     * @param data additional data
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] updateLinkKeys(AbstractDataType data) throws IOException {
        return methodInvoke(ZigbeeHanManagementMethods.UPDATE_LINK_KEYS, data);
    }
}