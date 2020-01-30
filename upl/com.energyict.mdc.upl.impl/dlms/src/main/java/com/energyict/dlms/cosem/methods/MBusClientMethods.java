package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.MBusClient;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:46:18
 */
public enum MBusClientMethods implements DLMSClassMethods {

                                                                  // VERSION0_D_S_M_R_23_SPEC
                                                                       // VERSION0_BLUE_BOOK_9TH_EDITION
                                                                             // VERSION0_BLUE_BOOK_10TH_EDITION
                                                                                   // VERSION1
    SLAVE_INSTALL(1,                      0x68, 0x68, 0x60, 0x70),
    SLAVE_DEINSTALL(2,                    0x70, 0x70, 0x68, 0x78),
    CAPTURE(3,                            0x78, 0x78, 0x70, 0x80),
    RESET_ALARM(4,                        0x80, 0x80, 0x78, 0x88),
    SYNCHRONIZE_CLOCK(5,                  0x88, 0x88, 0x80, 0x90),
    DATA_SEND(6,                          0x90, 0x90, 0x88, 0x98),
    SET_ENCRYPTION_KEY(7,                 0x98, 0x98, 0x90, 0xA0),
    TRANSFER_KEY(8,                       0xFF, 0xA0, 0x98, 0xA8),
    TRANSFER_FUAK(-1,                     0xFF, 0xFF, 0xFF, 0xB0),  //E.S.M.R. 5.0 SPECIFIC
    READ_DETAILED_VERSION_INFORMATION(-2, 0xFF, 0xFF, 0xFF, 0xB8 ); //E.S.M.R. 5.0 SPECIFIC


    private final int methodNumber;
    private final int[] shortNames;

    private MBusClient.VERSION version;

    MBusClientMethods(int methodNr, int... shortNames) {
        this.methodNumber = methodNr;
        this.shortNames = shortNames;
        this.version = MBusClient.VERSION.VERSION1;
    }

    /**
     * Getter for the current object with a specific version
     *
     * @param version the used version
     * @return this object with the version variable set to #version
     */
    public MBusClientMethods forVersion(MBusClient.VERSION version) {
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

    public int getShortName() {
        return shortNames[version.getIndex()];
    }
}

