package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.messages;

import com.energyict.dlms.axrdencoding.OctetString;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 21-mrt-2011
 * Time: 13:54:17
 */
public class ApolloActivityCalendarControllerTest {

    @Test
    public void createByteNameTest(){
        ApolloActivityCalendarController aacc = new ApolloActivityCalendarController(null);

        try {
            OctetString name = aacc.createByteName("1", 0);
            assertEquals(new byte[]{1}[0], name.getOctetStr()[0]);
            name = aacc.createByteName("a", 0);
            assertEquals(new byte[]{10}[0], name.getOctetStr()[0]);
            name = aacc.createByteName("f", 0);
            assertEquals(new byte[]{15}[0], name.getOctetStr()[0]);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            aacc.createByteName("1", 0);
        } catch (IOException e) {
            if(!e.getMessage().equals("ActivityCalendar did not contain a valid SEASONName.")){
                fail("Exception did not contain correct message.");
            }
        }
    }
}