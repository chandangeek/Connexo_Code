package com.energyict.genericprotocolimpl.elster.ctr.info;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 17:04:20
 */
public class EquipmentClassInfoTest extends TestCase {

    @Test
    public void testGetEquipmentClass() throws Exception {

        String s1 = "[000] " + "Equipment of non defined class";
        String s2 = "[AAA] " + "Class A equipment";
        String s3 = "[BBB] " + "Class B equipment";
        String s4 = "[CCC] " + "Class C equipment";
        String s5 = "[BVO] " + "Equipment of class B TYPE 1 (Volumetric)";
        String s6 = "[CVO] " + "Equipment of class C TYPE 2 (Volumetric)";
        String s7 = "[CVE] " + "Equipment of class C TYPE 2 (Venturi meter)";
        String s8 = "[CEM] " + "Equipment of class C Energy Meter";
        String s9 = "[AA1] " + "Equipment of type A1";
        String s10 ="[AA2] " + "Equipment of type A2";

        assertEquals(EquipmentClassInfo.getEquipmentClass("000"), s1);
        assertEquals(EquipmentClassInfo.getEquipmentClass("AAA"), s2);
        assertEquals(EquipmentClassInfo.getEquipmentClass("BBB"), s3);
        assertEquals(EquipmentClassInfo.getEquipmentClass("CCC"), s4);
        assertEquals(EquipmentClassInfo.getEquipmentClass("BVO"), s5);
        assertEquals(EquipmentClassInfo.getEquipmentClass("CVO"), s6);
        assertEquals(EquipmentClassInfo.getEquipmentClass("CVE"), s7);
        assertEquals(EquipmentClassInfo.getEquipmentClass("CEM"), s8);
        assertEquals(EquipmentClassInfo.getEquipmentClass("AA1"), s9);
        assertEquals(EquipmentClassInfo.getEquipmentClass("AA2"), s10);
        assertEquals(EquipmentClassInfo.getEquipmentClass("ABC"), "[ABC] Other");
    }
}
