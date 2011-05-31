package com.energyict.genericprotocolimpl.elster.ctr.tariff.rawobjects;

import com.energyict.genericprotocolimpl.elster.ctr.tariff.CodeTableBase64Parser;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.objects.CodeDayTypeObject;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.objects.CodeObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 21/04/11
 * Time: 8:01
 */
public class RawDayTypeBandTest {

    public static final int EXPECTED_LENGTH = 5;

    @Test
    public void testConstructorFromCodeDayTypeObject() throws Exception {
        List<CodeDayTypeObject> dayTypes = getCorrectCodeObject().getDayTypes();
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isHoliday() || dayType.isWeekday() || dayType.isSaturday()) {
                RawDayTypeBand rawDayTypeBand = new RawDayTypeBand(dayType);
                assertNotNull(rawDayTypeBand);
                assertNotNull(rawDayTypeBand.getBytes());
                assertEquals(EXPECTED_LENGTH, rawDayTypeBand.getBytes().length);
                assertEquals(EXPECTED_LENGTH, rawDayTypeBand.getLength());
            }
        }
    }

    @Test
    public void testConstructorFromIncorrectCodeDayTypeObject() throws Exception {
        List<CodeDayTypeObject> dayTypes = getCorrectCodeObject().getDayTypes();
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isDefault()) {
                try {
                    RawDayTypeBand rawDayTypeBand = new RawDayTypeBand(dayType);
                    fail("If you can see me, there is something wrong because the previous method should have thrown an exception but apparently it didn't!");
                } catch (IllegalArgumentException e) {
                    // Ok :) Great, we expected this one
                }
            }
        }
    }

    private CodeObject getCorrectCodeObject() throws IOException {
        File file = new File(getClass().getResource("/com/energyict/genericprotocolimpl/elster/ctr/tariff/rawobjects/correct_ct.b64").getFile());
        return CodeTableBase64Parser.getCodeTableFromBase64(file);
    }

    @Test
    public void testGetBytes() throws Exception {
        assertNotNull(new RawDayTypeBand().getBytes());
        assertEquals(EXPECTED_LENGTH, new RawDayTypeBand().getBytes().length);
    }

    @Test
    public void testGetLength() throws Exception {
        assertEquals(EXPECTED_LENGTH, new RawDayTypeBand().getLength());
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(new RawDayTypeBand().toString());
    }

}
