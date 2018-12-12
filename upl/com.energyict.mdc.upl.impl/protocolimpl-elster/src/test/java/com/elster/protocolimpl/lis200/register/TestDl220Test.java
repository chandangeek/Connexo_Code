/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.protocolimpl.lis200.register;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl220Test extends AbstractDl2xxTest {

    @Ignore("Hardcoded CET date. Needs refactoring")
    @Test
    public void RegisterReaderTestWithDLData() throws IOException {
        TestDl220 testDl220 = new TestDl220(propertySpecService, nlsService);

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

        testDl220.setMeterIndex(1);
        for (int i = 1; i <= 14; i++) {
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        sb.append("\n");

        testDl220 = new TestDl220(propertySpecService, nlsService);
        testDl220.setMeterIndex(2);
        for (int i = 1; i <= 14; i++) {
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl220.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl220registertest.txt");
        assertEquals(compareData, sb.toString());
    }
}