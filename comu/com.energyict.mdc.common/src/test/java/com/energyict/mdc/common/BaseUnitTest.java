/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaseUnitTest {

    @Test
    public void testBaseUnits() {
        BaseUnit unit = BaseUnit.get(BaseUnit.AMPERE);
        assertNotNull(unit);
        assertEquals("A", unit.toString());
        unit = BaseUnit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
        assertNotNull(unit);
        assertEquals("varh", unit.toString());
        unit = BaseUnit.get(BaseUnit.WATTHOUR);
        assertNotNull(unit);
        assertEquals("Wh", unit.toString());
        unit = BaseUnit.get(BaseUnit.NORMALCUBICMETER);
        assertNotNull(unit);
        assertEquals("Nm3", unit.toString());
        unit = BaseUnit.get(BaseUnit.QUANTITYPOWER);
        assertNotNull(unit);
        assertEquals("Q", unit.toString());
        unit = BaseUnit.get(BaseUnit.QUANTITYPOWERHOUR);
        assertNotNull(unit);
        assertEquals("Qh", unit.toString());
        unit = BaseUnit.get(BaseUnit.HERTZ);
        assertNotNull(unit);
        assertEquals("Hz", unit.toString());
    }

}
