package com.energyict.mdc.common;

import com.energyict.cbo.BaseUnit;
import org.junit.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 10:45:43
 */
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
