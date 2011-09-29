package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.profile.Lis100Processing;
import com.elster.protocolimpl.lis100.profile.ProcessingException;
import com.elster.protocolimpl.lis100.testutils.*;
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
 * class to test lis100 processing with winlis test data
 * <p/>
 * User: heuckeg
 * Date: 07.02.11
 * Time: 11:01
 */
public class TestProcessing {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Test
    public void temp551Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.551");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800551.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp552Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.552");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.2.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.2.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.2.1993 06:00:00"), sdf.parse("10.3.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800552.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp553Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.553");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800553.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp554Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.554");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800554.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp555Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.555");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800555.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp556Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.556");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800556.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp557Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.557");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800557.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp558Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.558");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800558.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp559Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.559");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.12.1992 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.12.1992 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.12.1992 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800559.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp560Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.560");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800560.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp561Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.561");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800561.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp562Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.562");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800562.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp563Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.563");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800563.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp564Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.564");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800564.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp565Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.565");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800565.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp566Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.566");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800566.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp567Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.567");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800567.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp568Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.568");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800568.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp569Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.569");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.12.1992 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800569.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp570Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.570");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800570.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp571Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.571");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800571.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp572Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.572");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800572.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp573Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.573");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800573.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp574Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.574");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800574.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp575Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.575");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800575.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp576Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.576");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1992 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.2.1992 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1992 06:00:00"), sdf.parse("1.4.1992 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800576.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp577Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.577");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.2.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.2.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.2.1993 06:00:00"), sdf.parse("10.3.1993 06:00:00"));

        String c2 = "Channel 0 : Missing or too many interval values: should be Tue Mar 02 06:00:00 CET 1993 - is Mon Mar 01 06:00:00 CET 1993\r";
        assertEquals(c2, pr.eventsToString());

        c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800577.txt");
        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp578Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.578");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = "Channel 0 : Interval changed to 30 min\r" +
                    "Channel 0 : Missing or too many interval values: should be Tue Feb 02 13:00:00 CET 1993 - is Tue Feb 02 13:36:28 CET 1993\r";

        assertEquals(c2, pr.eventsToString());

        c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800578.txt");
        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp580Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.580");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("15.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("15.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800580.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp581Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.581");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("31.1.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("15.1.1993 06:00:00"), sdf.parse("10.2.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/800581.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp701Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.701");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.10.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1310351.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp702Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.702");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.10.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1320351.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp703Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.703");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.10.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1330351.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp704Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.704");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.10.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1340351.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp711Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.711");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.11.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1310351_2.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp712Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.712");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.10.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1320351_2.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp713Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.713");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.11.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1330351_2.txt");

        assertEquals(c2, pr.pivdToString());
    }

    @Test
    public void temp714Test() throws ProcessingException, IOException, ParseException {

        InputStream stream = TestTestUtils.class.getResourceAsStream("/com/elster/protocolimpl/lis100/TEMP.714");

        TempReader tr = new TempReader(stream);

        Lis100TestObjectFactory factory = new Lis100TestObjectFactory(tr);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);

        DeviceData deviceData = new DeviceData(factory, timeZone);
        deviceData.prepareDeviceData();
        ChannelData cd = new ChannelData(deviceData);
        cd.readChannelData();

        Date dateTo = sdf.parse("1.1.1993 06:00:00");

        cd.readChannelProfile(dateTo, factory);

        assertNotNull(cd.getReadDate());
        Date d = sdf.parse("1.11.1993 06:00:00");
        assertEquals(d.toString(), cd.getReadDate().toString());

        Lis100Processing pr = new Lis100Processing(cd);
        pr.processReadData(sdf.parse("1.10.1993 06:00:00"), sdf.parse("1.12.1993 06:00:00"));

        String c2 = CompareDataReader.getAsDataString("/com/elster/protocolimpl/lis100/1340351_2.txt");

        assertEquals(c2, pr.pivdToString());
    }
}
