package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.dlms.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:31:21
 */
public class ObisCodeMapperTest {

    @Test
    public void errorRegisterTest() {
        try {
            DummyDLMSConnection connection = new DummyDLMSConnection();
            connection.setResponseByte(DLMSUtils.hexStringToByteArray("E6E7000C0100090400810019"));
            DLMSZMD protocol = new DLMSZMD();
            UniversalObject[] uos = new UniversalObject[1];
            List<Long> demandResetFields = new ArrayList<Long>();
            demandResetFields.add(0x2FA8L);
            demandResetFields.add(3L);
            demandResetFields.add(6L);
            demandResetFields.add(0L);
            demandResetFields.add(0L);
            demandResetFields.add(97L);
            demandResetFields.add(97L);
            demandResetFields.add(0L);
            demandResetFields.add(255L);
            uos[0] = new UniversalObject(demandResetFields, ObjectReference.SN_REFERENCE);
            protocol.getMeterConfig().setInstantiatedObjectList(uos);
            protocol.setDLMSConnection(connection);
            CosemObjectFactory cof = new CosemObjectFactory(protocol);
            ObisCodeMapper ocm = new ObisCodeMapper(cof, null, null);

            RegisterValue rv = ocm.getRegisterValue(ObisCode.fromString("0.0.97.97.0.255"));
            assertEquals("$00$81$00$19", rv.getText());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
