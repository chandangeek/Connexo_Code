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
public class TestDl240Test extends AbstractDl2xxTest {

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        TestDl240 dl240 = new TestDl240(propertySpecService, nlsService);

        StringBuilder sb = new StringBuilder();
        RegisterValue rv;

        dl240.setMeterIndex(1);
        for (int i = 1; i <= 15; i++) {
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        sb.append("\n");

        dl240 = new TestDl240(propertySpecService, nlsService);
        dl240.setMeterIndex(2);
        for (int i = 1; i <= 15; i++) {
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        sb.append("\n");

        dl240 = new TestDl240(propertySpecService, nlsService);
        dl240.setMeterIndex(3);
        for (int i = 1; i <= 15; i++) {
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        sb.append("\n");

        dl240 = new TestDl240(propertySpecService, nlsService);
        dl240.setMeterIndex(4);
        for (int i = 1; i <= 15; i++) {
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 128, 23, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = dl240.readRegister(new ObisCode(7, 0, 23, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/dl240registertest.txt");
        assertEquals(compareData, sb.toString());
    }
}