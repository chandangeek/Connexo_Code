package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.profiles;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.MeterTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 4-mrt-2011
 * Time: 16:33:12
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileBuilderTest {

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private TariffCalendarExtractor calendarExtractor;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private DeviceMessageFileExtractor deviceMessageFileExtractor;
    @Mock
    private NumberLookupFinder numberLookupFinder;
    @Mock
    private NumberLookupExtractor numberLookupExtractor;
    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void isDataObisCodeTest() throws PropertyValidationException {
        TypedProperties props = TypedProperties.empty();
        props.setProperty(SmartMeterProtocol.Property.SERIALNUMBER.getName(), "MasterSerialNumber");
        WebRTUZ3 meterProtocol = new WebRTUZ3(calendarFinder, calendarExtractor, messageFileFinder, deviceMessageFileExtractor, propertySpecService, numberLookupFinder, numberLookupExtractor);
        meterProtocol.setUPLProperties(props);
        LoadProfileBuilder lpb = new LoadProfileBuilder(meterProtocol);
        assertNotNull(lpb.isDataObisCode(ObisCode.fromString("1.0.1.8.0.255"), "MasterSerialNumber"));
        assertTrue(lpb.isDataObisCode(ObisCode.fromString("1.0.1.8.0.255"), "MasterSerialNumber"));

        try {
            lpb.isDataObisCode(ObisCode.fromString("1.0.1.8.0.255"), "SomeOtherSerialNumber");
        } catch (DeviceConfigurationException e) {
            assertEquals("[PRTCL-142] Unexpected value ''SomeOtherSerialNumber'' for property ''SerialNumber''", e.getMessage());
        }
        try {
            lpb.isDataObisCode(LoadProfileBuilder.MbusMeterStatusObisCode, "SomeOtherSerialNumber");
        } catch (DeviceConfigurationException e) {
            assertEquals("[PRTCL-142] Unexpected value ''SomeOtherSerialNumber'' for property ''SerialNumber''", e.getMessage());
        }

        assertFalse(lpb.isDataObisCode(Clock.getDefaultObisCode(), "MasterSerialNumber"));
        assertFalse(lpb.isDataObisCode(LoadProfileBuilder.MbusMeterStatusObisCode, "MasterSerialNumber"));
        assertFalse(lpb.isDataObisCode(LoadProfileBuilder.EmeterStatusObisCode, "MasterSerialNumber"));
        assertFalse(lpb.isDataObisCode(ObisCode.fromString("1.2.1.8.0.255"), "MasterSerialNumber"));
    }

    @Test
    public void constructLoadProfileConfigComposedCosemObjectTest() throws PropertyValidationException {
        TypedProperties props = TypedProperties.empty();
        props.setProperty(SmartMeterProtocol.Property.SERIALNUMBER.getName(), "MasterSerialNumber");
        WebRTUZ3 meterProtocol = new WebRTUZ3(calendarFinder, calendarExtractor, messageFileFinder, deviceMessageFileExtractor, propertySpecService, numberLookupFinder, numberLookupExtractor);
        meterProtocol.setUPLProperties(props);
        try {
            meterProtocol.getDlmsSession().init();
            LoadProfileBuilder lpb = new LoadProfileBuilder(meterProtocol);
            List<LoadProfileReader> loadProfileReaders = new ArrayList<LoadProfileReader>();
            assertNotNull(lpb.constructLoadProfileConfigComposedCosemObject(loadProfileReaders, true));

            UniversalObject[] uos = new UniversalObject[2];
            uos[0] = new UniversalObject(ObisCode.fromString("0.33.99.1.0.255").getLN(), 7, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("0.1.24.3.0.255").getLN(), 7, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            List<ChannelInfo> ciList = new ArrayList<ChannelInfo>();
            loadProfileReaders.add(new LoadProfileReader(ObisCode.fromString("0.x.99.1.0.255"), null, null, loadProfileReaders.size(), "35022968", ciList));
            loadProfileReaders.add(new LoadProfileReader(ObisCode.fromString("0.x.24.3.0.255"), null, null, loadProfileReaders.size(), "123456789", ciList));

            MeterTopology mt = new MeterTopology(meterProtocol);
            DeviceMapping dmSlave1 = new DeviceMapping("123456789", 1);
            mt.setMbusDeviceMappings(Arrays.asList(dmSlave1));
            DeviceMapping dmSlave2 = new DeviceMapping("35022968", 33);
            mt.setEmeterDeviceMappings(Arrays.asList(dmSlave2));
            meterProtocol.setMeterTopology(mt);

            assertEquals(4, lpb.constructLoadProfileConfigComposedCosemObject(loadProfileReaders, true).getNrOfAttributes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
