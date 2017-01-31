/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.license;

import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.ProtocolFamily;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Models the known test protocols that are supported by the licensing mechanism.
 * A protocol is considered to be covered by the license if
 * the protocol is either explicitly mentioned as covered or if
 * the protocol's {@link ProtocolFamily} is covered by the license.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (12:10)
 */
public enum LicensedProtocolRule implements LicensedProtocol {

    //test with all properties
    EICT_TEST(5001, "com.energyict.protocolimpl.eicttest.EICTTestProtocol", ProtocolFamily.TEST),
    DLMS_SIMPLE(5002, "com.energyict.protocolimpl.dlms.SimpleDLMSProtocol", ProtocolFamily.TEST),
    SDK_SAMPLE_PROTOCOL(5003, "com.energyict.protocolimpl.sdksample.SDKSampleProtocol", ProtocolFamily.TEST),
    SDK_SMART_SAMPLE_PROTOCOL(5004, "com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol", ProtocolFamily.TEST),
    SDK_DEVICE_PROTOCOL(5005, "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol", ProtocolFamily.TEST),
    SDK_SAMPLE_PROTOCOL_TEST_WITH_ALL_PROPERTIES(5006, "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithAllProperties", ProtocolFamily.TEST),
    SDK_SAMPLE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY(5007, "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithMandatoryProperty", ProtocolFamily.TEST),;

    private int code;
    private String className;
    private Set<ProtocolFamily> families;

    LicensedProtocolRule(int code, String className, ProtocolFamily... families) {
        this.code = code;
        this.className = className;
        this.families = new HashSet<>(Arrays.asList(families));
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Set<ProtocolFamily> getFamilies() {
        return this.families;
    }

    @Override
    public String getName() {
        return name();
    }


    /**
     * Returns the LicensedProtocolRule that is uniquely identified by the code.
     *
     * @param code The code
     * @return The LicensedProtocolRule or <code>null</code> if no LicensedProtocolRule is uniquely identified by the code
     */
    public static LicensedProtocol fromCode(int code) {
        for (LicensedProtocolRule protocol : values()) {
            if (code == protocol.code) {
                return protocol;
            }
        }
        return null;
    }

    /**
     * Returns the LicensedProtocolRule that is implemented by the class
     * whose name is the specified <code>className</code>.
     *
     * @param className The name of a protocol class
     * @return The LicensedProtocolRule or <code>null</code> if no LicensedProtocolRule is implemented by a class by that name
     */
    public static LicensedProtocol fromClassName(String className) {
        for (LicensedProtocolRule protocol : values()) {
            if (className.equals(protocol.className)) {
                return protocol;
            }
        }
        return null;
    }
}