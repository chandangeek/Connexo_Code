package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:46:18
 */
public enum MbusClientMethods implements DLMSClassMethods {

    SLAVE_INSTALL(1, 0x68, 0x60),
    SLAVE_DEINSTALL(2, 0x70, 0x68),
    CAPTURE(3, 0x78, 0x70),
    RESET_ALARM(4, 0x80, 0x78),
    SYNCHRONIZE_CLOCK(5, 0x88, 0x80),
    DATA_SEND(6, 0x90, 0x88),
    SET_ENCRYPTION_KEY(7, 0x98, 0x90),
    TRANSFER_KEY(8, 0xA0, 0x98);

    /**
     * Indicating BlueBook 9th or below is supported
     */
    public static final int VERSION9 = 9;
    /**
     * Indicating BlueBook 10th is supported
     */
    public static final int VERSION10 = 10;

    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortNameV9;
    /**
     * The shortName of this attribute according to BlueBook V10
     */
    private final int shortNameV10;

    /**
     * The used version
     */
    private int version;

    /**
     * Private constructor
     *
     * @param methodNr the method number
     * @param sn9      the value for the ShortName for version 9 or below
     * @param sn10     the value for the ShortName for version 10 or higher
     */
    private MbusClientMethods(int methodNr, int sn9, int sn10) {
        this.methodNumber = methodNr;
        this.shortNameV9 = sn9;
        this.shortNameV10 = sn10;
    }

    /**
     * Getter for the current object with a specific version
     *
     * @param version the used version
     * @return this object with the version variable set to #version
     */
    public MbusClientMethods forVersion(int version) {
        this.version = version;
        return this;
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
        return DLMSClassId.MBUS_CLIENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        if (version == VERSION9) {
            return shortNameV9;
        } else {
            return shortNameV10;
        }
    }
}

