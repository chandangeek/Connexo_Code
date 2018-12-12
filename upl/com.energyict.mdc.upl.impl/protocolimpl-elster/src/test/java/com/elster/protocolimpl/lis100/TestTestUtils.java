package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.testutils.Lis100TestObjectFactory;
import com.elster.protocolimpl.lis100.testutils.TempReader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;

/**
 * test the classes and methods used in tests
 *
 * User: heuckeg
 * Date: 02.02.11
 * Time: 09:52
 */
public class TestTestUtils {

    @Test
    public void readTempTest1() {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/Temp.000");

        TempReader tr = new TempReader(stream);

        assertEquals("3113149", tr.getDeviceNo());
        assertEquals("9233", tr.getSwVersion());
        assertEquals("413.794250", tr.getTotalCounter());
        assertEquals("4136.9428", tr.getSetblCounter());
        assertEquals("0", tr.getStateRegister());
        assertEquals("2", tr.getCpValue());
        assertEquals("30", tr.getInterval());
        assertEquals("110", tr.getCustomerNo());
        assertEquals("390040", tr.getMeterNo());
        assertEquals("1", tr.getFactor());
        assertEquals("kWh", tr.getUnit());
        assertEquals("VZ", tr.getCalcType());

        assertEquals(0xFFC, (int)tr.getIvData().get(0));
        assertEquals(0x010, (int)tr.getIvData().get(1));
        assertEquals(0x012, (int)tr.getIvData().get(2));
        assertEquals(0x001, (int)tr.getIvData().get(3));
        assertEquals(0x010, (int)tr.getIvData().get(4));
    }

    @Test
    public void createTestObjectFactoryTest() throws IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/Temp.000");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        assertEquals("3113149", factory.getSerialNumberObject().getValue());
        assertEquals(413.794250, factory.getTotalCounterObject().getCounterValue());
        assertEquals(4136.9428, factory.getProgCounterObject().getCounterValue());
        assertEquals(0, factory.getStateRegisterObject().getIntValue());
        assertEquals(1.0, factory.getCpValueObject().getDoubleValue());
        assertEquals(30 * 60, factory.getIntervalObject().getIntervalSeconds());
        assertEquals("110", factory.getCustomerNoObject().getValue());
        assertEquals("390040", factory.getMeterNoObject().getValue());
        assertEquals(1.0, factory.getCalcFactorObject().getDoubleValue());
        assertEquals("kWh", factory.getUnitObject().getValue());
        assertEquals("VZ", factory.getCalcTypeObject().getValue());
    }
}
