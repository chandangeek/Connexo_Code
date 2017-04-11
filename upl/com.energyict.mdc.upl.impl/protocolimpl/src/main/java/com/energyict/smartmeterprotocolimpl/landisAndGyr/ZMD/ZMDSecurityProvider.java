package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 11:36
 */

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

import java.util.Properties;

/**
 * Default implementation of the securityProvider for the ZMD Meters.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification
 *
 */
public class ZMDSecurityProvider extends NTASecurityProvider {

    private static final int MAN_SPEC_RESPONDING_VALUE_TOTAL_LENGTH = 8;
    private static final int MAN_SPEC_RESPONDING_VALUE_LENGTH = 7;
    private static final int MAN_SPEC_TYPE_INDEX = 7;

    private static final int VARIANT0_ADD = 0;
    private static final int VARIANT1_OR = 1;
    private static final int VARIANT2_XOR = 2;
    private static final int VARIANT3_ADD_OR = 3;
    private static final int VARIANT4_ADD_XOR = 4;
    private static final int VARIANT5_ADD = 5;
    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public ZMDSecurityProvider(Properties properties) {
        super(properties);
    }
}