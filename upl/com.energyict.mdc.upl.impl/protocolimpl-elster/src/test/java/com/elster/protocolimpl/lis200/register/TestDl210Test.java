/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.protocolimpl.lis200.register;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl210Test extends AbstractDl2xxTest {

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {
        TestDl210 testDl210 = new TestDl210(propertySpecService, nlsService);

        StringBuilder sb = new StringBuilder();

        RegisterValue rv;

        for (int i = 1; i <= 15; i++) {
            rv = testDl210.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl210.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl210.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = testDl210.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }
        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl210registertest.txt");
        assertEquals(compareData, sb.toString());
    }
}