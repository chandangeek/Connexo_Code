package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 23-nov-2010
 * Time: 11:33:05
 */
public class CTRObjectInfoTest extends TestCase {

    @Test
    public void testGetSymbol() throws Exception {
        assertEquals(CTRObjectInfo.getSymbol("1.0.0"), "Qm");
        assertEquals(CTRObjectInfo.getSymbol("2.0.0"), "Tot_Vm");
        assertEquals(CTRObjectInfo.getSymbol("4.0.0"), "P");
        assertEquals(CTRObjectInfo.getSymbol("7.0.0"), "T");
    }

    @Test
    public void testGetUnit() throws Exception {
        assertEquals(CTRObjectInfo.getUnit("1.0.0"), Unit.get(BaseUnit.CUBICMETERPERHOUR));
        assertEquals(CTRObjectInfo.getUnit("2.0.0"), Unit.get(BaseUnit.CUBICMETER));
        assertEquals(CTRObjectInfo.getUnit("4.0.0"), Unit.get(BaseUnit.BAR));
        assertEquals(CTRObjectInfo.getUnit("7.0.0"), Unit.get(BaseUnit.KELVIN));
    }
}
