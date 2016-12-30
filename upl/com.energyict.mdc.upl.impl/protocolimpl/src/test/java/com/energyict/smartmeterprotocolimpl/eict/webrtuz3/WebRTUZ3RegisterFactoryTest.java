package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.UniversalObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.SmartMeterProtocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 11:24:36
 */
@RunWith(MockitoJUnitRunner.class)
public class WebRTUZ3RegisterFactoryTest {

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private Extractor extractor;

    Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void constructComposedObjectFromRegisterListTest() {
        try {
            TypedProperties props = new TypedProperties();
            props.setProperty(SmartMeterProtocol.SERIALNUMBER, "Master");
            WebRTUZ3 meterProtocol = new WebRTUZ3(calendarFinder, messageFileFinder, extractor);
            meterProtocol.addProperties(props);
            meterProtocol.getDlmsSession().init();
            WebRTUZ3RegisterFactory registerFactory = new WebRTUZ3RegisterFactory(meterProtocol);

            UniversalObject[] uos = new UniversalObject[3];
            uos[0] = new UniversalObject(ObisCode.fromString("1.0.1.8.0.255").getLN(), 3, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("1.0.2.8.0.255").getLN(), 3, 6);

            Register reg1 = new Register(-1, ObisCode.fromString("1.0.1.8.0.255"), "Master");
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);

            assertNotNull(registerFactory.constructComposedObjectFromRegisterList(new ArrayList<Register>(), true));
            assertEquals("We expect two attribute requests, one for the value and one for the unit.", 2, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1), true).getNrOfAttributes());

            Register reg2 = new Register(-1, ObisCode.fromString("1.0.1.8.1.255"), "Master");
            assertEquals("We still expect just the two attribute requests, the additional register is not in the objectList.", 2, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1, reg2), true).getNrOfAttributes());

            uos[2] = new UniversalObject(ObisCode.fromString("0.0.13.0.0.255").getLN(), 13, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            Register reg3 = new Register(-1, ObisCode.fromString("0.0.13.0.0.255"), "Master");
            assertEquals("Next to the value and unit of the before register, we also want (only) the value of the new object.", 3, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1, reg2, reg3), true).getNrOfAttributes());

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}

