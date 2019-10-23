package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class LTEMonitoringWrapperVersion1Test {

    private static final int V_T3402 = 3402;
    private static final int V_T3412 = 3412;
    private static final int V_T3412ext2 = 34122;
    private static final int V_T3324 = 3324;
    private static final int V_TeDRX = 1000;
    private static final int V_TPTW = 12;
    private static final int V_N_RSRQ = 13;
    private static final int V_N_RSRP = 14;
    private static final int V_qRxlevMin = 15;
    private static final int V_qRxlevMinCEr13 = 16;
    private static final int V_qRxlevMinCE1r13 = 17;
    private static final int RSRQ_NOT_KNOWN_OR_NOT_DETECTABLE = 99;
    private static final int RSRP_NOT_KNOWN_OR_NOT_DETECTABLE = 127;

    private static final AbstractDataType T3402 = new Unsigned16(V_T3402);
    private static final AbstractDataType T3412 = new Unsigned16(V_T3412);
    private static final AbstractDataType T3412ext2 = new Unsigned32(V_T3412ext2);
    private static final AbstractDataType T3324 = new Unsigned16(V_T3324);
    private static final AbstractDataType TeDRX = new Unsigned32(V_TeDRX);
    private static final AbstractDataType TPTW = new Unsigned16(V_TPTW);
    private static final AbstractDataType N_RSRQ = new Integer8(V_N_RSRQ);
    private static final AbstractDataType N_RSRP = new Integer8(V_N_RSRP);
    private static final AbstractDataType qRxlevMin = new Integer8(V_qRxlevMin);
    private static final AbstractDataType qRxlevMinCEr13 = new Integer8(V_qRxlevMinCEr13);
    private static final AbstractDataType qRxlevMinCE1r13 = new Integer8(V_qRxlevMinCE1r13);

    @Test
    public void testDecodeWithSomeValues(){
        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(T3412ext2)
                .addDataType(T3324)
                .addDataType(TeDRX)
                .addDataType(TPTW)
                .addDataType(N_RSRQ)
                .addDataType(N_RSRP)
                .addDataType(qRxlevMin)
                .addDataType(qRxlevMinCEr13)
                .addDataType(qRxlevMinCE1r13);

        LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
        assertEquals(V_T3402,lteMonitoringWrapper.getT3402());
        assertEquals(V_T3412,lteMonitoringWrapper.getT3412());
        assertEquals(V_T3412ext2, lteMonitoringWrapper.getT3412ext2());
        assertEquals(V_T3324, lteMonitoringWrapper.getT3324());
        assertEquals(V_TeDRX, lteMonitoringWrapper.getTeDrx());
        assertEquals(V_TPTW, lteMonitoringWrapper.getTptw());
        assertEquals(V_N_RSRQ, lteMonitoringWrapper.getRsrq());
        assertEquals(V_N_RSRP, lteMonitoringWrapper.getRsrp());
        assertEquals(V_qRxlevMin, lteMonitoringWrapper.getqRxlevMin());
        assertEquals(V_qRxlevMinCEr13, lteMonitoringWrapper.getqRxlevMinCEr13());
        assertEquals(V_qRxlevMinCE1r13, lteMonitoringWrapper.getqRxlevMinCE1r13());
    }

    /*
        -	(N)RSRQ: represents the signal quality as defined in 3GPP TS 36.133:
        For Cat M1 a value range from -30 up to 46 is necessary to represent RSRQ. Refer to Release 13 version of 3GPP TS 36.133 v13.11.0 (2018-04) for details.
                For Cat NB-IoT a value range from -30 up to 46 is necessary to represent NRSRQ. Refer to Release 14 version of 3GPP TS 36.133 v14.4.0 (2017-08) for details.

        By means of the ps_status attribute (attribute 5 of the GSM Status object), the link can be made whether the RSRQ or NRSRQ level is represented.

                (-30)          -34 dB
                (-29)          -33,5 dB
                (-28..-1)     -33…-19,5 dB
                (0)             -19,5 dB or less
                (1)             -19 dB
                (2..31)       -18,5…-4 dB
                (32)           -3,5 dB
                (33)           -3 dB
                (34)           -3 dB or less
                (35)           -2,5 dB
                (36)           -2 dB
                (37..45)     -1,5…2,5 dB
                (46)            2,5 dB or better
                (99)            Not known or not detectable
        Remark: Reported values of (-30..-1) and (34..46) apply for UE that support extended RSRQ range (Coverage Enhancement Levels)
    */

    @Test
    public void testDecodeNRSRQWithAllPossibleValuesBetweenNegative30AndPostive46(){

        for (int nRSRQ=-30; nRSRQ<=46; nRSRQ++) {
            Structure structure = new Structure()
                    .addDataType(T3402)
                    .addDataType(T3412)
                    .addDataType(T3412ext2)
                    .addDataType(T3324)
                    .addDataType(TeDRX)
                    .addDataType(TPTW)
                    .addDataType(new Integer8(nRSRQ))
                    .addDataType(N_RSRP)
                    .addDataType(qRxlevMin)
                    .addDataType(qRxlevMinCEr13)
                    .addDataType(qRxlevMinCE1r13);

            LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
            assertEquals(nRSRQ, lteMonitoringWrapper.getRsrq());
        }
    }

    @Test
    public void testDecodeNRSRQWithNotKnownOrNotDetectableValue(){

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(T3412ext2)
                .addDataType(T3324)
                .addDataType(TeDRX)
                .addDataType(TPTW)
                .addDataType(new Integer8(RSRQ_NOT_KNOWN_OR_NOT_DETECTABLE))
                .addDataType(N_RSRP)
                .addDataType(qRxlevMin)
                .addDataType(qRxlevMinCEr13)
                .addDataType(qRxlevMinCE1r13);

        LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
        assertEquals(RSRQ_NOT_KNOWN_OR_NOT_DETECTABLE, lteMonitoringWrapper.getRsrq());
    }

    /*
    (N)RSRP: represents the signal level as defined in 3GPP TS 36.133:

    For Cat M1 a value range from -17 up to 97 is necessary to represent RSRP. Refer to Release 13 version of 3GPP TS 36.133 v13.11.0 (2018-04) for details

            (-17)          -156 dBm
            (-16)          -155 dBm
            (-15..-1)     -154…-140 dBm
            (0)             -140 dBm or less.
            (1)             -139 dBm
            (2)             -138 dBm
            (3..96)       -137…-44 dBm
            (97)           -44 or better
            (127)         Not known or not detectable
    Remark: depending on the use of Coverage Enhancement modes, a reported value of (0) will be used or not (for devices that do not use CE modes, values (-17..-1) will not be used. A Reported value of (0) means in that case a RSRP < -140 dBm).

    For NB-IoT a value range from 0 to 113 is necessary to represent NRSRP value. Refer to Release 14 version of 3GPP TS 36.133 v14.4.0 (2017-08) for details.

            (0)           -156 dBm
            (1)           -155 dBm
            (2)           -154 dBm
            (3..112)   -153… --44  dBm
            (113)       -44 dBm or better
            (127)       Not known or not detectable

    By means of the ps_status attribute (attribute 5 of the GSM Status object), the link can be made whether the RSRP or NRSRP level is represented.
    Remark: for the NRSRP value (NB-IoT) the mapping is different from the mapping for the RSRP value (Cat M1) !

    */

    @Test
    public void testDecodeNRSRPWithAllPossibleValuesBetweenNegative17AndPostive113(){

        for (int nRSRP=-17; nRSRP<=113; nRSRP++) {
            Structure structure = new Structure()
                    .addDataType(T3402)
                    .addDataType(T3412)
                    .addDataType(T3412ext2)
                    .addDataType(T3324)
                    .addDataType(TeDRX)
                    .addDataType(TPTW)
                    .addDataType(N_RSRQ)
                    .addDataType(new Integer8(nRSRP))
                    .addDataType(qRxlevMin)
                    .addDataType(qRxlevMinCEr13)
                    .addDataType(qRxlevMinCE1r13);

            LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
            assertEquals(nRSRP, lteMonitoringWrapper.getRsrp());
        }
    }

    @Test
    public void testDecodeNRSRPWithNotKnownOrNotDetectableValue(){

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(T3412ext2)
                .addDataType(T3324)
                .addDataType(TeDRX)
                .addDataType(TPTW)
                .addDataType(N_RSRQ)
                .addDataType(new Integer8(RSRP_NOT_KNOWN_OR_NOT_DETECTABLE))
                .addDataType(qRxlevMin)
                .addDataType(qRxlevMinCEr13)
                .addDataType(qRxlevMinCE1r13);

        LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
        assertEquals(RSRP_NOT_KNOWN_OR_NOT_DETECTABLE, lteMonitoringWrapper.getRsrp());
    }

    @Test
    public void testToStringWithSomeValues() {

        String expectedToString =
                "T3402:" + V_T3402 + ";\n" +
                        "T3412:" + V_T3412 + ";\n" +
                        "T3412ext2:" + V_T3412ext2 + ";\n" +
                        "T3324:" + V_T3324 + ";\n" +
                        "TeDRX:" + V_TeDRX + ";\n" +
                        "TPTW:" + V_TPTW + ";\n" +
                        "(N)RSRQ:" + V_N_RSRQ + ";\n" +
                        "(N)RSRP:" + V_N_RSRP + ";\n" +
                        "qRxlevMin:" + V_qRxlevMin + ";\n" +
                        "qRxlevMinCE-r13:" + V_qRxlevMinCEr13 + ";\n" +
                        "qRxlevMinCE1-r13:" + V_qRxlevMinCE1r13 + ";\n";

        Structure structure = new Structure()
                .addDataType(T3402)
                .addDataType(T3412)
                .addDataType(T3412ext2)
                .addDataType(T3324)
                .addDataType(TeDRX)
                .addDataType(TPTW)
                .addDataType(N_RSRQ)
                .addDataType(N_RSRP)
                .addDataType(qRxlevMin)
                .addDataType(qRxlevMinCEr13)
                .addDataType(qRxlevMinCE1r13);

        LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(structure);
        assertEquals(expectedToString, lteMonitoringWrapper.toString());
    }
}