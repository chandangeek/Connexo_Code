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
 * Test the readout strategy under different events
 * <p/>
 * User: heuckeg
 * Date: 02.02.11
 * Time: 11:29
 */
public class TestReadoutStrategies {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Test
    public void back1DayInsideMonthNoEvents() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.001");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.10.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.10.2007 08:58:35");
        assertEquals(d.toString(), cd.getReadDate().toString());
    }

    @Test
    public void back1DayBeginMonthNoEvents() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.002");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.09.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayPowerFailInBetween() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.003");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.10.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.10.2007 05:27:02");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayPowerFailInBetween2() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.010");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.9.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayTimeCorrFwInBetween() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.004");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.10.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.10.2007 05:27:02");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayTimeCorrFwInBetween2() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.011");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.9.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayOverMBROInBetween() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.020");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.9.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backOneDayOverMBROInBetween2() throws ParseException, IOException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.021");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("30.9.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    //@Test(expected = IOException.class)
    @Test
    public void back1DayBeginMonthWithFailure() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.030");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.09.2007 11:17:02");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.9.2007 06:00:00");
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backSomeDaysWithIntChange1() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.566");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.01.1993 06:00:00");
        System.out.println(cd.getReadDate());
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }

    @Test
    public void backSomeDaysWithIntChange2() throws IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.580");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);         deviceData.prepareDeviceData();         ChannelData cd = new ChannelData(deviceData);         cd.readChannelData();

        Date dateTo = sdf.parse("30.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("15.01.1993 06:00:00");
        System.out.println(cd.getReadDate());
        assertEquals(d.getTime(), cd.getReadDate().getTime());
    }
}
