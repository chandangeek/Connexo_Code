/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.upl.UPLConnectionFunction;

import java.util.Arrays;
import java.util.List;

/**
 * Dummy DeviceProtocol for PluggableClassTestUsages, including support for ConnectionFunctions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (10:57)
 */
public class MockDeviceProtocolHavingSupportForConnectionFunctions extends MockDeviceProtocol {

    public static final int PROVIDED_CF_1_ID = 1;
    public static final String PROVIDED_CF_1_NAME = "Provided_CF_1";
    public static final int PROVIDED_CF_2_ID = 2;
    public static final String PROVIDED_CF_2_NAME = "Provided_CF_2";

    public static final int CONSUMABLE_CF_3_ID = 3;
    public static final String CONSUMABLE_CF_3_NAME = "Consumable_CF_3";
    public static final int CONSUMABLE_CF_4_ID = 4;
    public static final String CONSUMABLE_CF_4_NAME = "Consumable_CF_4";

    @Override
    public List<UPLConnectionFunction> getProvidedConnectionFunctions() {
        return Arrays.asList(new UPLConnectionFunction() {
            @Override
            public String getConnectionFunctionName() {
                return PROVIDED_CF_1_NAME;
            }

            @Override
            public long getId() {
                return PROVIDED_CF_1_ID;
            }
        }, new UPLConnectionFunction() {
            @Override
            public String getConnectionFunctionName() {
                return PROVIDED_CF_2_NAME;
            }

            @Override
            public long getId() {
                return PROVIDED_CF_2_ID;
            }
        });
    }

    @Override
    public List<UPLConnectionFunction> getConsumableConnectionFunctions() {
        return Arrays.asList(new UPLConnectionFunction() {
            @Override
            public String getConnectionFunctionName() {
                return CONSUMABLE_CF_3_NAME;
            }

            @Override
            public long getId() {
                return CONSUMABLE_CF_3_ID;
            }
        }, new UPLConnectionFunction() {
            @Override
            public String getConnectionFunctionName() {
                return CONSUMABLE_CF_4_NAME;
            }

            @Override
            public long getId() {
                return CONSUMABLE_CF_4_ID;
            }
        });
    }
}