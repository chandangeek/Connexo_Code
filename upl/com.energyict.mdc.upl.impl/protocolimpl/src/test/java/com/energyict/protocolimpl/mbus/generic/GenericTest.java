package com.energyict.protocolimpl.mbus.generic;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.coreimpl.DirectDialer;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.mbus.core.CIField51h;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.CIField7Ah;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericTest {
    protected Logger logger = Logger.getAnonymousLogger();

    private byte[] data1 = ProtocolTools.getBytesFromHexString("6887876808087278563412B43401050010000004174D370000053E00000000040764E20000052E00000000041F59000000055300000000055B00004C43055F0000D64205630000C242056BCA9B39404568F8BD17BE37FD1700020000000000000421EA8600000C6D20092A220CFD0C110202104C6D0000C181441700000000440700000000441F000000003816", 2);
    private byte[] data2 = ProtocolTools.getBytesFromHexString("68E6E6680823723503220042042002350000000E84000244000000008E1084006711000000008E2084003532000000008E3084000000000000008E801084000000000000008E4084008431030000008E5084008431030000008E6084000000000000008E7084000000000000008EC010840000000000000001FF93000104FFA0150000000004FFA1150000000004FFA2150000000004FFA3150000000007FFA600000000000000000007FFA700000000000000000007FFA800002000000000000007FFA90001000000000000000DFD8E0007302E31322E31420DFFAA000B3030312D333133203332421FCB16", 2);


    public TestMBusInputStream inputStream;

    @Mock
    DiscoverTools discoverTools;

    @Mock
    private OutputStream outputStream;

    @Mock
    private Dialer dialer;

    @Mock
    ProtocolConnection protocolConnection;

    public Generic mbus = new Generic();
    protected Properties properties;


    @Test
    public void testData1() throws IOException, LinkException {
        CIField72h obj72 = new CIField72h(TimeZone.getDefault());
        obj72.parse(data1);

        log(obj72);

        CIField7Ah obj7A = new CIField7Ah();
        obj7A.parse(data1);
        //logger.info(obj7A.toString());

        RegisterFactory registerFactory = new RegisterFactory(mbus);
        registerFactory.init(obj72);

        //logger.info(registerFactory.getRegisterValues().toString());

        assertEquals(7, obj72.getDataRecords().size());
        assertEquals(11,registerFactory.getRegisterValues().size());

    }

    private void log(CIField72h ciField72h) {
        logger.info(ciField72h.header());
        for(Object obj : ciField72h.getDataRecords()){
            DataRecord dataRecord = (DataRecord) obj;
            logger.info("> "+dataRecord.getQuantity()+"\t"+dataRecord.getText()+"\t"+dataRecord.getDate());
        }
    }



    @Test
    public void testData2() throws IOException, LinkException {
        CIField72h obj72 = new CIField72h(TimeZone.getDefault());
        obj72.parse(data2);
        List dataRecords = obj72.getDataRecords();

        log(obj72);

        CIField7Ah obj7A = new CIField7Ah();
        obj7A.parse(data2);
       // logger.info(obj7A.toString());


        RegisterFactory registerFactory = new RegisterFactory(mbus);
        registerFactory.init(obj72);

       // logger.info(registerFactory.getRegisterValues().toString());

        assertEquals(17, dataRecords.size());
        assertEquals(19, registerFactory.getRegisterValues().size());
    }


    private void obsolete() throws LinkException, IOException {
        Properties properties = new Properties();

        properties.setProperty("ProfileInterval", "60");
        properties.setProperty("SecondaryAddressing", "0");
        properties.setProperty(MeterProtocol.ADDRESS,"253");


        properties.setProperty("SerialNumber","1234FFFF");
        properties.setProperty("HeaderManufacturerCode","FFFF");
        properties.setProperty("HeaderVersion","FF");
        properties.setProperty("HeaderMedium","FF");

        properties.setProperty("Retries","2");

        inputStream = new TestMBusInputStream();
        spy(inputStream);
        spy(mbus);

        when(discoverTools.getDialer()).thenReturn(dialer);

        when(dialer.getInputStream()).thenReturn(inputStream);
        when(dialer.getOutputStream()).thenReturn(outputStream);

        when(discoverTools.getDialer().getInputStream()).thenReturn(inputStream);

        when(discoverTools.getAddress()).thenReturn(253);
        when(discoverTools.getProperties()).thenReturn(properties);

        discoverTools.setProperties(properties);
        discoverTools.setAddress(253);
        discoverTools.init();
        discoverTools.connect();

        //mbus.discoverPrimaryAddresses(discoverTools);

        // RegisterFactory registerFactory = (RegisterFactory) mbus.getRegisterFactory();
        // List registerValues = registerFactory.getRegisterValues();
        // logger.info(registerValues.toString());
    }




}
