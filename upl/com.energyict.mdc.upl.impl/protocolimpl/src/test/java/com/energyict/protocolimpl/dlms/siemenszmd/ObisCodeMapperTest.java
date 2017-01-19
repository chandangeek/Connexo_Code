package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:31:21
 */
@RunWith(MockitoJUnitRunner.class)
public class ObisCodeMapperTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private Formatter dateFormatter;
    @Mock
    private Extractor extractor;

    @Test
    public void errorRegisterTest() throws IOException {
        DummyDLMSConnection connection = new DummyDLMSConnection();
        connection.setResponseByte(DLMSUtils.hexStringToByteArray("E6E7000C0100090400810019"));
        DLMSZMD protocol = new DLMSZMD(this.propertySpecService, this.calendarFinder, this.messageFileFinder, this.dateFormatter, this.extractor);
        UniversalObject[] uos = new UniversalObject[1];
        List<Long> demandResetFields =
                Arrays.asList(
                        0x2FA8L,
                        3L,
                        6L,
                        0L,
                        0L,
                        97L,
                        97L,
                        0L,
                        255L);
        uos[0] = new UniversalObject(demandResetFields, ObjectReference.SN_REFERENCE);
        protocol.getMeterConfig().setInstantiatedObjectList(uos);
        protocol.setDLMSConnection(connection);
        CosemObjectFactory cof = new CosemObjectFactory(protocol);
        ObisCodeMapper ocm = new ObisCodeMapper(cof, null, null);

        RegisterValue rv = ocm.getRegisterValue(ObisCode.fromString("0.0.97.97.0.255"));
        assertEquals("$00$81$00$19", rv.getText());
    }

}