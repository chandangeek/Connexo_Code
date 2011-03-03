package com.energyict.smartmeterprotocolimpl.webrtuz3;

import com.energyict.dlms.UniversalObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 11:24:36
 */
public class WebRTUZ3RegisterFactoryTest {

    Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void constructComposedObjectFromRegisterListTest() {
        try {
            WebRTUZ3 meterProtocol = new WebRTUZ3();
            meterProtocol.getDlmsSession().init();
            WebRTUZ3RegisterFactory registerFactory = new WebRTUZ3RegisterFactory(meterProtocol);

            UniversalObject[] uos = new UniversalObject[3];
            uos[0] = new UniversalObject(ObisCode.fromString("1.0.1.8.0.255").getLN(), 3, 6);
            uos[1] = new UniversalObject(ObisCode.fromString("1.0.2.8.0.255").getLN(), 3, 6);

            Register reg1 = new Register(ObisCode.fromString("1.0.1.8.0.255"), "Master");
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);

            assertNotNull(registerFactory.constructComposedObjectFromRegisterList(new ArrayList<Register>(), true));
            assertEquals(2, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1), true).getNrOfAttributes());

            Register reg2 = new Register(ObisCode.fromString("1.0.1.8.1.255"), "Master");
            assertEquals(2, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1, reg2), true).getNrOfAttributes());

            uos[2] = new UniversalObject(ObisCode.fromString("0.0.13.0.0.255").getLN(), 13, 6);
            meterProtocol.getDlmsSession().getMeterConfig().setInstantiatedObjectList(uos);
            Register reg3 = new Register(ObisCode.fromString("0.0.13.0.0.255"), "Master");
            assertEquals(3, registerFactory.constructComposedObjectFromRegisterList(Arrays.asList(reg1, reg2, reg3), true).getNrOfAttributes());

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
