package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.testutils.Lis100TestObjectFactory;
import com.elster.protocolimpl.lis100.testutils.TempReader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * test class to test im tracking of changed values is ok
 *
 * User: heuckeg
 * Date: 08.02.11
 * Time: 10:15
 */
public class TestReadoutValueTracking {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Test
    public void trackingCpValueTest() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/Temp.050");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData(); 

        assertEquals(10.0, cd.getCpValue());

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        assertEquals(1.0, cd.getCpValue());
    }

    @Test
    public void trackingCalcFactorTest() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/Temp.051");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData(); 

        assertEquals(0.1, cd.getCalcFactor());

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        assertEquals(0.01, cd.getCalcFactor());
    }

}
