package com.energyict.protocolimpl.base;

import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 9/09/11
 * Time: 7:55
 */
public class DataDumpParserTest {

    @Test
    public void testGetBillingCounter() throws Exception {
        DataDumpParser dataDumpParser = new DataDumpParser(new byte[0]);
        Set<Integer> billingPoints = new TreeSet<Integer>();
        assertEquals(0, dataDumpParser.getBillingCounter(billingPoints));

        billingPoints = new TreeSet<Integer>();
        billingPoints.add(86);
        billingPoints.add(87);
        billingPoints.add(88);
        billingPoints.add(89);
        billingPoints.add(90);
        assertEquals(90, dataDumpParser.getBillingCounter(billingPoints));

        billingPoints = new TreeSet<Integer>();
        billingPoints.add(86);
        billingPoints.add(87);
        billingPoints.add(88);
        billingPoints.add(89);
        billingPoints.add(90);
        assertEquals(90, dataDumpParser.getBillingCounter(billingPoints));

        billingPoints = new TreeSet<Integer>();
        billingPoints.add(96);
        billingPoints.add(97);
        billingPoints.add(98);
        billingPoints.add(99);
        billingPoints.add(00);
        assertEquals(0, dataDumpParser.getBillingCounter(billingPoints));

        billingPoints = new TreeSet<Integer>();
        billingPoints.add(98);
        billingPoints.add(99);
        billingPoints.add(00);
        billingPoints.add(01);
        billingPoints.add(02);
        assertEquals(2, dataDumpParser.getBillingCounter(billingPoints));

        billingPoints = new TreeSet<Integer>();
        billingPoints.add(99);
        billingPoints.add(00);
        billingPoints.add(01);
        billingPoints.add(02);
        billingPoints.add(03);
        assertEquals(3, dataDumpParser.getBillingCounter(billingPoints));

    }
}
