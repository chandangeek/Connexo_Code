package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class Beacon3100ClientTypeTest {

    @Test
    public void testEquals() throws Exception {


        Beacon3100ClientType beacon3100ClientType1 = new Beacon3100ClientType(1,1,0,5,3);
        Beacon3100ClientType beacon3100ClientType2 = new Beacon3100ClientType(1,1,0,3,0);
        Beacon3100ClientType beacon3100ClientType3 = new Beacon3100ClientType(1,1,0,3,0);
        Beacon3100ClientType beacon3100ClientType4 = new Beacon3100ClientType(1,1,0,5,3);

        assertTrue(beacon3100ClientType1.equals(beacon3100ClientType4.toStructure()));
        assertTrue(beacon3100ClientType2.equals(beacon3100ClientType3.toStructure()));
        assertFalse(beacon3100ClientType1.equals(beacon3100ClientType2.toStructure()));
        assertFalse(beacon3100ClientType1.equals(beacon3100ClientType3.toStructure()));

        assertTrue(beacon3100ClientType1.equals(beacon3100ClientType4));
        assertTrue(beacon3100ClientType2.equals(beacon3100ClientType3));
        assertFalse(beacon3100ClientType1.equals(beacon3100ClientType2));
        assertFalse(beacon3100ClientType1.equals(beacon3100ClientType3));
    }
}