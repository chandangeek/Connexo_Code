package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.dlms.DLMSUtils;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test the BatterySupport status field. Two fields indicate the remaining batteryLife in days, the other presents the battery installationDate
 */
public class BatterySupportStatusTest {

    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void batterySupportTest() throws IOException {
        byte[] response = DLMSUtils.hexStringToByteArray("304337354138344443303445434631324330344543463132");
        ABBA1700 protocol = new ABBA1700(propertySpecService);
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussels"), Logger.getAnonymousLogger());
        BatterySupportStatus bss = new BatterySupportStatus(protocol, ProtocolUtils.convert2ascii(response));
        assertEquals(3652, bss.getRemainingBatterySupportTime());
        assertEquals(3652, bss.getRemainingPowerDownBatteryLife());
        assertEquals(new Date(1302878444000L), bss.getBatteryInstallDate());
    }

}
