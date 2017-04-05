/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.protocolimpl.lis200.register;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestEk260_V111Test extends AbstractDl2xxTest {

    private Locale savedLocale;

    @Before
    public void setup() {
        savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        if (!Locale.getDefault().equals(savedLocale)) {
            Locale.setDefault(savedLocale);
        }
    }

    @Test
    public void RegisterReaderTestWithDLData() throws IOException {

        TestEk260_V111 ek260 = new TestEk260_V111(propertySpecService, nlsService);

        StringBuilder sb = new StringBuilder();
        RegisterValue rv;

        ek260.setSoftwareVersion(111);
        ek260.setMeterIndex(1);

        /* volume */
        for (int i = 1; i <= 15; i++) {
            rv = ek260.readRegister(new ObisCode(7, 0, 13, 0, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 13, 2, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 13, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 13, 60, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 13, 56, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 11, 62, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        /* analogue values */
        for (int i = 1; i <= 15; i++) {
            rv = ek260.readRegister(new ObisCode(7, 0, 43, 57, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 128, 43, 22, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 43, 55, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 128, 43, 20, 255, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 42, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 42, 57, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 42, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 41, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 41, 57, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 41, 54, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 52, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
            rv = ek260.readRegister(new ObisCode(7, 0, 53, 42, 0, i));
            sb.append(rv);
            sb.append("\n");
        }

        String compareData = getResourceAsString("/com/elster/protocolimpl/lis200/register/ek260_1V11_registertest.txt");

        Unit degCelsius = Unit.get(BaseUnit.DEGREE_CELSIUS);
        String c = compareData.replaceAll("--CC--", degCelsius.toString());

        assertEquals(c, sb.toString());
    }
}