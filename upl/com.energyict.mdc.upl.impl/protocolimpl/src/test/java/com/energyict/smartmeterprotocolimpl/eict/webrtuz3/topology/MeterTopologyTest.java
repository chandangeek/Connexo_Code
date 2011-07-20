package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import com.energyict.protocolimpl.utils.MockSecurityProvider;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 4-mrt-2011
 * Time: 15:02:13
 */
public class MeterTopologyTest {

    private static DummyDLMSConnection connection = new DummyDLMSConnection();

    @Test
    public void getSerialNumberTest() {
        Properties props = new Properties();
        props.put(SmartMeterProtocol.SERIALNUMBER, "MasterSerialNumber");
        WebRTUZ3 meterProtocol = new WebRTUZ3();
        meterProtocol.addProperties(props);
        MeterTopology mt = new MeterTopology(meterProtocol);
        assertNotNull(mt.getSerialNumber(ObisCode.fromString("1.0.1.8.0.255")));
        assertEquals("MasterSerialNumber", mt.getSerialNumber(ObisCode.fromString("1.0.1.8.0.255")));
        try {
            assertEquals("", mt.getSerialNumber(ObisCode.fromString("1.99.1.8.0.255")));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        DeviceMapping dmSlave1 = new DeviceMapping("MbusSlave1", 1);
        mt.setMbusDeviceMappings(Arrays.asList(dmSlave1));
        assertEquals("MbusSlave1", mt.getSerialNumber(ObisCode.fromString("1.1.1.8.0.255")));
        DeviceMapping dmSlave2 = new DeviceMapping("EmeterSlave1", 33);
        mt.setEmeterDeviceMappings(Arrays.asList(dmSlave2));
        assertEquals("EmeterSlave1", mt.getSerialNumber(ObisCode.fromString("1.33.1.8.0.255")));
    }

    @Test
    public void getPhysicalAddressTest() {
        Properties props = new Properties();
        props.put(SmartMeterProtocol.SERIALNUMBER, "MasterSerialNumber");
        WebRTUZ3 meterProtocol = new WebRTUZ3();
        meterProtocol.addProperties(props);
        MeterTopology mt = new MeterTopology(meterProtocol);
        assertNotNull(mt.getPhysicalAddress("MasterSerialNumber"));
        assertEquals(0, mt.getPhysicalAddress("MasterSerialNumber"));
        try {
            assertEquals(-1, mt.getPhysicalAddress("SomeSerialNumber"));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        DeviceMapping dmSlave1 = new DeviceMapping("MbusSlave1", 1);
        mt.setMbusDeviceMappings(Arrays.asList(dmSlave1));
        assertEquals(1, mt.getPhysicalAddress("MbusSlave1"));
        DeviceMapping dmSlave2 = new DeviceMapping("EmeterSlave1", 33);
        mt.setEmeterDeviceMappings(Arrays.asList(dmSlave2));
        assertEquals(33, mt.getPhysicalAddress("EmeterSlave1"));
    }

    @Test
    public void constructDiscoveryComposedCosemObjectTest() {
        Properties props = new Properties();
        props.put(SmartMeterProtocol.SERIALNUMBER, "MasterSerialNumber");
        WebRTUZ3 meterProtocol = new WebRTUZ3();
        meterProtocol.addProperties(props);
        try {
            meterProtocol.getDlmsSession().init();
            MeterTopology mt = new MeterTopology(meterProtocol);
            UniversalObject[] uos = new UniversalObject[2];
            uos[0] = new UniversalObject(ObisCode.fromString("0.33.96.1.0.255").getLN(), 1, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("0.1.96.1.0.255").getLN(), 1, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            mt.constructDiscoveryComposedCosemObject();
            assertNotNull(mt.getDiscoveryComposedCosemObject());
            assertEquals(2, mt.getDiscoveryComposedCosemObject().getNrOfAttributes());

        } catch (ConnectionException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getMeterMapperTests() {

        String expectedResponse1 = "01001BC40342020009093132333435363738390009083335303232393638";

        Properties props = new Properties();
        props.put(SmartMeterProtocol.SERIALNUMBER, "MasterSerialNumber");
        props.put(DlmsProtocolProperties.BULK_REQUEST, "1");
        WebRTUZ3 meterProtocol = new WebRTUZ3();
        meterProtocol.addProperties(props);

        // Test the EmeterMapper
        try {
            meterProtocol.getDlmsSession().init();

            MockSecurityProvider dsp = new MockSecurityProvider();
            SecurityContext sc = new SecurityContext(0, 3, 0, null, dsp, SecurityContext.CIPHERING_TYPE_GLOBAL);
            ApplicationServiceObject aso = new ApplicationServiceObject(null, meterProtocol.getDlmsSession(), sc, 1);
            ConformanceBlock cb = new ConformanceBlock();
            cb.setBit(ConformanceBlock.BIT_MULTIPLE_REFS);
            aso.getAssociationControlServiceElement().getXdlmsAse().setNegotiatedConformance((int) cb.getValue());
            connection.setApplicationServiceObject(aso);

            meterProtocol.getDlmsSession().setDlmsConnection(connection);
            MeterTopology mt = new MeterTopology(meterProtocol);
            assertNotNull(mt.getEmeterMapper());

            UniversalObject[] uos = new UniversalObject[2];
            uos[0] = new UniversalObject(ObisCode.fromString("0.33.96.1.0.255").getLN(), 1, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("0.1.96.1.0.255").getLN(), 1, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            mt.constructDiscoveryComposedCosemObject();

            //Set the expected response
            connection.setResponseByte(DLMSUtils.hexStringToByteArray(expectedResponse1));

            assertNotNull(mt.getEmeterMapper());
            assertEquals(1, mt.getEmeterMapper().size());
            assertEquals("35022968", mt.getEmeterMapper().get(0).getSerialNumber());
            assertEquals(33, mt.getEmeterMapper().get(0).getPhysicalAddress());

        } catch (ConnectionException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Test the MbusMeterMapper
        try {
            meterProtocol.getDlmsSession().init();

            MockSecurityProvider dsp = new MockSecurityProvider();
            SecurityContext sc = new SecurityContext(0, 3, 0, null, dsp, SecurityContext.CIPHERING_TYPE_GLOBAL);
            ApplicationServiceObject aso = new ApplicationServiceObject(null, meterProtocol.getDlmsSession(), sc, 1);
            ConformanceBlock cb = new ConformanceBlock();
            cb.setBit(ConformanceBlock.BIT_MULTIPLE_REFS);
            aso.getAssociationControlServiceElement().getXdlmsAse().setNegotiatedConformance((int) cb.getValue());
            connection.setApplicationServiceObject(aso);

            meterProtocol.getDlmsSession().setDlmsConnection(connection);
            MeterTopology mt = new MeterTopology(meterProtocol);
            assertNotNull(mt.getMbusMapper());

            UniversalObject[] uos = new UniversalObject[2];
            uos[0] = new UniversalObject(ObisCode.fromString("0.33.96.1.0.255").getLN(), 1, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("0.1.96.1.0.255").getLN(), 1, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            mt.constructDiscoveryComposedCosemObject();

            //Set the expected response
            connection.setResponseByte(DLMSUtils.hexStringToByteArray(expectedResponse1));

            assertNotNull(mt.getMbusMapper());
            assertEquals(1, mt.getMbusMapper().size());
            assertEquals("123456789", mt.getMbusMapper().get(0).getSerialNumber());
            assertEquals(1, mt.getMbusMapper().get(0).getPhysicalAddress());

        } catch (ConnectionException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}

