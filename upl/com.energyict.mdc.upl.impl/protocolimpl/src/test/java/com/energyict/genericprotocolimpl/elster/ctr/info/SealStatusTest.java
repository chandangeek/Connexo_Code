package com.energyict.genericprotocolimpl.elster.ctr.info;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 17:04:20
 */
public class SealStatusTest extends TestCase {

    static final String DESCRIPTION1 = "Reserved [0]";
    static final String DESCRIPTION2 = "Event log reset [1]";
    static final String DESCRIPTION3 = "Factory conditions [2]";
    static final String DESCRIPTION4 = "Default values [3]";
    static final String DESCRIPTION5 = "Status change [4]";
    static final String DESCRIPTION6 = "Reserved [5]";
    static final String DESCRIPTION7 = "Reserved [6]";
    static final String DESCRIPTION8 = "Reserved [7]";
    static final String DESCRIPTION9 = "Reserved [8]";
    static final String DESCRIPTION10 = "Remote volume configuration seal [9]";
    static final String DESCRIPTION11 = "Remote analysis configuration seal [10]";
    static final String DESCRIPTION12 = "Download program [11]";
    static final String DESCRIPTION13 = "Restore default password [12]";
    static final String DESCRIPTION14 = "Reserved [13]";
    static final String DESCRIPTION15 = "Reserved [14]";
    static final String DESCRIPTION16 = "Reserved [15]";
    static final String DESCRIPTION17 = "Invalid seal!";

    
    @Test
    public void testSealStatus() throws Exception {

        SealStatusBit s1 = SealStatusBit.RESERVED_0;
        SealStatusBit s2 = SealStatusBit.EVENT_LOG_RESET;
        SealStatusBit s3 = SealStatusBit.FACTORY_CONDITIONS;
        SealStatusBit s4 = SealStatusBit.DEFAULT_VALUES;
        SealStatusBit s5 = SealStatusBit.STATUS_CHANGE;
        SealStatusBit s6 = SealStatusBit.RESERVED_5;
        SealStatusBit s7 = SealStatusBit.RESERVED_6;
        SealStatusBit s8 = SealStatusBit.RESERVED_7;
        SealStatusBit s9 = SealStatusBit.RESERVED_8;
        SealStatusBit s10 = SealStatusBit.REMOTE_CONFIG_VOLUME;
        SealStatusBit s11 = SealStatusBit.REMOTE_CONFIG_ANALYSIS;
        SealStatusBit s12 = SealStatusBit.DOWNLOAD_PROGRAM;
        SealStatusBit s13 = SealStatusBit.RESTORE_DEFAULT_PASSWORDS;
        SealStatusBit s14 = SealStatusBit.RESERVED_13;
        SealStatusBit s15 = SealStatusBit.RESERVED_14;
        SealStatusBit s16 = SealStatusBit.RESERVED_15;
        SealStatusBit s17 = SealStatusBit.INVALID;

        assertEquals(s1.getDescription(), DESCRIPTION1);
        assertEquals(s2.getDescription(), DESCRIPTION2);
        assertEquals(s3.getDescription(), DESCRIPTION3);
        assertEquals(s4.getDescription(), DESCRIPTION4);
        assertEquals(s5.getDescription(), DESCRIPTION5);
        assertEquals(s6.getDescription(), DESCRIPTION6);
        assertEquals(s7.getDescription(), DESCRIPTION7);
        assertEquals(s8.getDescription(), DESCRIPTION8);
        assertEquals(s9.getDescription(), DESCRIPTION9);
        assertEquals(s10.getDescription(), DESCRIPTION10);
        assertEquals(s11.getDescription(), DESCRIPTION11);
        assertEquals(s12.getDescription(), DESCRIPTION12);
        assertEquals(s13.getDescription(), DESCRIPTION13);
        assertEquals(s14.getDescription(), DESCRIPTION14);
        assertEquals(s15.getDescription(), DESCRIPTION15);
        assertEquals(s16.getDescription(), DESCRIPTION16);
        assertEquals(s17.getDescription(), DESCRIPTION17);

    }
}
