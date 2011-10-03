/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author gna
 * @since 10-feb-2010
 *
 */
public class AbstractObjectTest {

    /**
     * Test method for {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.AbstractObject#getStartAddress()}.
     */
    @Test
    public final void testGetStartAddress() {
	ManufacturerObject mo = new ManufacturerObject(null);
	assertEquals("01:018A_4.0", mo.getStartAddress());
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.AbstractObject#constructInstanceString()}.
     */
    @Test
    public final void testConstructInstanceString() {
	ManufacturerObject mo = new ManufacturerObject(null);
	assertEquals("01", mo.constructInstanceString());
	
	// TODO make a test with a two digit instance number
    }

}
