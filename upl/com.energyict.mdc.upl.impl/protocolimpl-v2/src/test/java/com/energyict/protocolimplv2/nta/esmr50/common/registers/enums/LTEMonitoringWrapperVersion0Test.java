package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LTEMonitoringWrapperVersion0Test {

    private static final int V_T3402 = 3402;
    private static final int V_T3412 = 3412;
    private static final int V_RSRQ = 13;
    private static final int V_RSRP = 14;
    private static final int V_qRxlevMin = 15;
    private static final int NOT_KNOWN_OR_NOT_DETECTABLE = 99;

    private static final AbstractDataType T3402 = new Unsigned16(V_T3402);
    private static final AbstractDataType T3412 = new Unsigned16(V_T3412);
    private static final AbstractDataType RSRQ = new Unsigned8(V_RSRQ);
    private static final AbstractDataType RSRP = new Unsigned8(V_RSRP);
    private static final AbstractDataType qRxlevMin = new Integer8(V_qRxlevMin);

    @Test
    public void testDecodeWithSomeValues() {
        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(RSRQ)
                .addDataType(RSRP)
                .addDataType(qRxlevMin);

        LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
        assertEquals(V_T3402, lteMonitoringWrapper.getT3402());
        assertEquals(V_T3412, lteMonitoringWrapper.getT3412());
        assertEquals(V_RSRQ, lteMonitoringWrapper.getRsrq());
        assertEquals(V_RSRP, lteMonitoringWrapper.getRsrp());
        assertEquals(V_qRxlevMin, lteMonitoringWrapper.getqRxlevMin());
    }

    @Test
    public void testDecodeRSRQWithAllPossibleValuesBetweenZEROAndPostive32(){

        for (int rsrq=0; rsrq<=32; rsrq++) {
            Structure structure = new Structure()
                    .addDataType(T3402)
                    .addDataType(T3412)
                    .addDataType(new Unsigned8(rsrq))
                    .addDataType(RSRP)
                    .addDataType(qRxlevMin);

            LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
            assertEquals(rsrq, lteMonitoringWrapper.getRsrq());
        }
    }

    @Test
    public void testDecodeRSRQWithNotKnownOrNotDetectableValue(){

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(new Unsigned8(NOT_KNOWN_OR_NOT_DETECTABLE))
                .addDataType(RSRP)
                .addDataType(qRxlevMin);

        LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
        assertEquals(NOT_KNOWN_OR_NOT_DETECTABLE, lteMonitoringWrapper.getRsrq());
    }

    @Test
    public void testDecodeRSRPWithAllPossibleValuesBetweenZEROAndPostive95(){

        for (int rsrp=0; rsrp<=95; rsrp++) {
            Structure structure = new Structure()
                    .addDataType(T3402)
                    .addDataType(T3412)
                    .addDataType(RSRQ)
                    .addDataType(new Unsigned8(rsrp))
                    .addDataType(qRxlevMin);


            LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
            assertEquals(rsrp, lteMonitoringWrapper.getRsrp());
        }
    }

    @Test
    public void testDecodeRSRPWithNotKnownOrNotDetectableValue(){

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(RSRQ)
                .addDataType(new Unsigned8(NOT_KNOWN_OR_NOT_DETECTABLE))
                .addDataType(qRxlevMin);

        LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
        assertEquals(NOT_KNOWN_OR_NOT_DETECTABLE, lteMonitoringWrapper.getRsrp());
    }

    @Test
    public void testToStringWithSomeValues() {

        String expectedToString =
                "T3402:" + V_T3402 + ";\n" +
                        "T3412:" + V_T3412 + ";\n" +
                        "RSRQ:" + V_RSRQ + ";\n" +
                        "RSRP:" + V_RSRP + ";\n" +
                        "qRxlevMin:" + V_qRxlevMin + ";\n";

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(RSRQ)
                .addDataType(RSRP)
                .addDataType(qRxlevMin);

        LTEMonitoringWrapperVersion0 lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(structure);
        assertEquals(expectedToString, lteMonitoringWrapper.toString());
    }
}