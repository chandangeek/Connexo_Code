package com.energyict.protocolimpl.elster.a1800;

import com.energyict.protocolimpl.elster.a1800.tables.PowerQualityMonitorLog;
import com.energyict.protocolimpl.elster.a1800.tables.PowerQualityMonitorTests;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 24-jan-2011
 * Time: 15:55:38
 */
public class A1800Test extends TestCase {

    public void test() throws IOException {
        byte[] bytes = new byte[]{0, 1, 2, 3};
        assertArrayEquals(ProtocolTools.reverseByteArray(bytes), new byte[]{3, 2, 1, 0});

        byte[] data = ProtocolTools.getBytesFromHexString("$05$04$00$00$00$01$00$01$00$FF$0A$07$0C$0F$26$13$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF");
        PowerQualityMonitorLog powerQualityMonitorLog = new PowerQualityMonitorLog(null, false);
        powerQualityMonitorLog.parse(data);
        assertTrue(powerQualityMonitorLog.getMeterEvents().size() == 1);

        byte[] valueBytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        valueBytes = ProtocolTools.reverseByteArray(valueBytes);
        int value = ProtocolTools.getUnsignedIntFromBytes(valueBytes);

        byte[] data2 = ProtocolTools.getBytesFromHexString("$05$0D$00$00$00$0D$00$0D$00$FF$0A$07$0C$0F$26$13$11$00$00$00$81$0A$07$0C$0F$26$13$11$00$00$00$82$0A$07$0C$0F$26$13$11$00$00$00$03$0A$07$0C$0F$26$13$11$00$00$00$04$0A$07$0C$0F$26$13$11$00$00$00$05$0A$07$0C$0F$26$13$11$00$00$00$06$0A$07$0C$0F$26$13$11$00$00$00$07$0A$07$0C$0F$26$13$11$00$00$00$08$0A$07$0C$0F$26$13$11$00$00$00$09$0A$07$0C$0F$26$13$11$00$00$00$0A$0A$07$0C$0F$26$13$11$00$00$00$0B$0A$07$0C$0F$26$13$11$00$00$00$0C$0A$07$0C$0F$26$13$11$00$00$00");
        PowerQualityMonitorLog powerQualityMonitorLog2 = new PowerQualityMonitorLog(null, true);
        powerQualityMonitorLog2.parse(data2);

        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(0).getMessage(), "PQM log cleared, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(1).getMessage(), "Start of low voltage, high voltage or voltage phase angle errors on any phase, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(2).getMessage(), "Start of exceeding low voltage threshold, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(3).getMessage(), "End of exceeding high voltage threshold, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(4).getMessage(), "End of reverse power or bad power factor on any phase, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(5).getMessage(), "End of low current on any phase, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(6).getMessage(), "End of Power Factor test failure, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(7).getMessage(), "End of Second harmonic current test failure, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(8).getMessage(), "End of total harmonic distortion current test failure, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(9).getMessage(), "End of total harmonic distortion voltage test failure, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(10).getMessage(), "End of voltage imbalance, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(11).getMessage(), "End of current imbalance, optional value not supported");
        assertEquals(powerQualityMonitorLog2.getMeterEvents().get(12).getMessage(), "End of total demand distortion test failure, optional value not supported");

        assertEquals(powerQualityMonitorLog2.getSequenceNumber(), 13);
        assertEquals(powerQualityMonitorLog2.getUnreadEntries(), 13);
        assertEquals(powerQualityMonitorLog2.getValidEntries(), 13);
        assertEquals(powerQualityMonitorLog2.isLongFormat(), true);

        byte[] data3 = ProtocolTools.getBytesFromHexString("02000103FFF00000F2020000FFF000003000101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        PowerQualityMonitorTests powerQualityMonitorTests = new PowerQualityMonitorTests(null);
        powerQualityMonitorTests.parse(data3);





    }
}