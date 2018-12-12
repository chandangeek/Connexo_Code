package com.elster.protocolimpl.dlms.profile.standardchecker;

import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CapturedObjects;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CheckingArchiveEntry;
import com.energyict.protocol.IntervalStateBits;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 09:19
 */
public class TestStandardChecker
{
    private static CaptureObjectDefinition[] a1profile60minDefinitions = {
            new CaptureObjectDefinition(8, new ObisCode(0,0,1,0,0,255), 2, 0),
            new CaptureObjectDefinition(1, new ObisCode(0,128,96,8,67,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(7,0,13,83,0,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(7,0,12,81,0,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(0,2,96,10,1,255), 2, 0),
    };

    @Test
    public void testEvtDlmsChecker()
    {
        CapturedObjects archive = new CapturedObjects(a1profile60minDefinitions);

        IArchiveLineChecker checker = new EvtDlmsChecker();

        CheckingArchiveEntry entry = new CheckingArchiveEntry(new ObisCode(0,2,96,10,1,255), 2, "EVT_DLMS", checker);

        checker.prepareChecker(entry, archive);

        Object[] data = new Object[5];
        IArchiveLineChecker.CheckResult result;

        // no status
        data[4] = new BitString(6, new byte[] {0});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(0, result.getEisStatus());

        // bit 0 set
        data[4] = new BitString(6, new byte[] {(byte)0x80});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.DEVICE_ERROR, result.getEisStatus());

        // bit 1 set
        data[4] = new BitString(6, new byte[] {0x40});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.DEVICE_ERROR, result.getEisStatus());

        // bit 2 set
        data[4] = new BitString(6, new byte[] {0x20});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.BADTIME, result.getEisStatus());

        // bit 3 set
        data[4] = new BitString(6, new byte[] {0x10});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.OTHER, result.getEisStatus());

        // bit 4 set
        data[4] = new BitString(6, new byte[] {0x08});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.OTHER, result.getEisStatus());

        // bit 5 set
        data[4] = new BitString(6, new byte[] {0x04});
        result = checker.check(data);
        assertTrue(result.getResult());
        assertEquals(IntervalStateBits.CORRUPTED, result.getEisStatus());
    }
}
