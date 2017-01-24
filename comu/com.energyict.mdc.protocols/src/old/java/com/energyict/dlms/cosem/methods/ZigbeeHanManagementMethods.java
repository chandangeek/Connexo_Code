package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Contains functionality to access methods of the Zigbee HAN Management object (independent whether or not shortName or logicalNames are used)
 * <br/>
 * <p><b>
 * TODO the SN method numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, <br>
 * TODO so additional methods may be added in the future
 * </b></p>
 * <p/>
 * Copyrights EnergyICT<br/>
 * Date: 22-jul-2011<br/>
 * Time: 11:41:07<br/>
 */
public enum ZigbeeHanManagementMethods implements DLMSClassMethods {

    BACKUP(1, 0x20),
    RESTORE(2, 0x28),
    CREATE_HAN(3, 0x30),
    IDENTIFY_DEVICE(4, 0x38),
    REMOVE_MIRROR(5, 0x40),
    UPDATE_NETWORK_KEYS(6, 0x48),
    UPDATE_LINK_KEYS(7, 0x50),
    REMOVE_HAN(8, 0x58);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ZigbeeHanManagementMethods(final int methodNumber, final int shortName) {
        this.methodNumber = methodNumber;
        this.shortName = shortName;
    }

    /**
     * Getter for the method number
     *
     * @return the method number as int
     */
    public int getMethodNumber() {
        return this.methodNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.ZIGBEE_HAN_MANAGEMENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
}
