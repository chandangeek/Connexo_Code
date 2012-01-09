package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.telegram.DataElement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntervalDataMapTest {
	
	@Test
	public void testBuildMap1() {
		
		IntervalDataMap idm = new IntervalDataMap();
		
		idm.addElement(new DataElement("aaaaa", new Double(10), 0x11111L, 1L, 0), 0); 
		
		String c = "[11111<aaaaa,10.0,11111,1,0>]\n\r";
		
        String d = idm.toString();
        assertEquals(c, d);
	}
	
	@Test
	public void testBuildMap2() {
		
		IntervalDataMap idm = new IntervalDataMap();
		
		idm.addElement(new DataElement("aaaaa", new Double(10), 0x11111L, 1L, 0), 0); 
		idm.addElement(new DataElement("aaaaa", new Double(20), 0x11112L, 2L, 0), 0); 
		
		String c = "[11111<aaaaa,10.0,11111,1,0>]\n\r" +
		           "[11112<aaaaa,20.0,11112,2,0>]\n\r";
		
        String d = idm.toString();
        assertEquals(c, d);
	}
	
	@Test
	public void testBuildMap3() {
		
		IntervalDataMap idm = new IntervalDataMap();
		
		idm.addElement(new DataElement("aaaaa", new Double(30), 0x11113L, 3L, 0), 0); 
		idm.addElement(new DataElement("aaaaa", new Double(20), 0x11112L, 2L, 0), 0); 
		idm.addElement(new DataElement("aaaaa", new Double(10), 0x11111L, 1L, 0), 0); 
		
		String c = "[11111<aaaaa,10.0,11111,1,0>]\n\r" +
		           "[11112<aaaaa,20.0,11112,2,0>]\n\r" +
                   "[11113<aaaaa,30.0,11113,3,0>]\n\r";
		
        String d = idm.toString();
        assertEquals(c, d);
	}
	
	@Test
	public void testBuildMap4() {
		
		IntervalDataMap idm = new IntervalDataMap();
		
		idm.addElement(new DataElement("aaaaa", new Double(30), 0x11113L, 3L, 0), 0); 
		idm.addElement(new DataElement("aaaaa", new Double(20), 0x11112L, 2L, 0), 0); 
		idm.addElement(new DataElement("aaaaa", new Double(10), 0x11111L, 1L, 0), 0); 

		idm.addElement(new DataElement("aaaab", new Double(21), 0x11112L, 2L, 0), 1); 
		idm.addElement(new DataElement("aaaab", new Double(31), 0x11113L, 3L, 0), 1); 
		idm.addElement(new DataElement("aaaab", new Double(41), 0x11114L, 4L, 0), 1); 
		
		String c = "[" + "11111<" + "aaaaa,10.0,11111,1,0" + ">]\n\r" +
		           "[" + "11112<" + "aaaaa,20.0,11112,2,0" + ";aaaab,21.0,11112,2,0" + ">]\n\r" +
                   "[" + "11113<" + "aaaaa,30.0,11113,3,0" + ";aaaab,31.0,11113,3,0" + ">]\n\r" +
                   "[" + "11114<" + "-"                    + ";aaaab,41.0,11114,4,0" + ">]\n\r";

        String d = idm.toString();
        assertEquals(c, d);
	}
}
