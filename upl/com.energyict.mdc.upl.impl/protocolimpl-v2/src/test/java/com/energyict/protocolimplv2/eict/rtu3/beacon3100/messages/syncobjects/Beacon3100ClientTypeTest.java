package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testObisCodesCast() {
        Set<SchedulableItem> items = new HashSet<>();
        items.add(new SchedulableItem(new ObisCode(1,1,1,1,1,1,true), new Unsigned32(1)));
        items.add(new SchedulableItem(new ObisCode(2,2,2,2,2,2,false), new Unsigned16(1)));
        List<SchedulableItem> profiles = new ArrayList<>(items);
        Beacon3100Schedulable schedulable = new Beacon3100Schedulable(0L, 1, 1, 1, profiles, null, null);
        schedulable.updateBufferSizeForAllLoadProfiles(new Unsigned32(10));
        schedulable.toStructure();
    }
}