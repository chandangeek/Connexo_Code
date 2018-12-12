package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Contains functionality to access methods of the Zigbee CETS control object (independent whether or not shortName or logicalNames are used)
 * <br/>
 *
 * <p><b>
 * TODO the SN method numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, <br>
 * TODO so additional methods may be added in the future
 *</b></p>
 *
 * Copyrights EnergyICT<br/>
 * Date: 22-jul-2011<br/>
 * Time: 15:27:10<br/>
 */
public enum ZigbeeSETCControlMethods implements DLMSClassMethods {

    REGISTER_DEVICE(1, 0x18),
    UNREGISTER_DEVICE(2, 0x20),
    UNREGISTER_ALL_DEVICES(3, 0x28),
    BACKUP_HAN(4, 0x40),
    RESTORE_HAN(5, 0x48),
    REMOVE_MIRROR(7, 0x58),
    UPDATE_LINK_KEY(9, 0x68),
    CREATE_HAN(10, 0x70),
    REMOVE_HAN(11, 0x78);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ZigbeeSETCControlMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.ZIGBEE_SETC_CONTROL;
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
