package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class GSMDiagnotiscsPsStatusVersion2Test {

    private static final String INACTIVE = "Inactive";
    private static final String GPRS = "GPRS";
    private static final String EDGE = "EDGE";
    private static final String UMTS = "UMTS";
    private static final String HSDPA = "HSDPA";
    private static final String LTE =  "LTE";
    private static final String CDMA = "CDMA";
    private static final String LTE_NB_IOT = "LTE NB-IoT";
    private static final String LTE_CAT_M1 = "LTE Cat M1";

    @Test
    public void testGetDescriptionForIdWithValue0ExpectInactive(){
        assertEquals(INACTIVE, GSMDiagnosticPSStatusVersion2.getDescriptionForId(0));
    }

    @Test
    public void testGetDescriptionForIdWithValue1ExpectGPRS(){
        assertEquals(GPRS, GSMDiagnosticPSStatusVersion2.getDescriptionForId(1));
    }

    @Test
    public void testGetDescriptionForIdWithValue2ExpectEdge(){
        assertEquals(EDGE, GSMDiagnosticPSStatusVersion2.getDescriptionForId(2));
    }

    @Test
    public void testGetDescriptionForIdWithValue3ExpectUmts(){
        assertEquals(UMTS, GSMDiagnosticPSStatusVersion2.getDescriptionForId(3));
    }

    @Test
    public void testGetDescriptionForIdWithValue4ExpectHsdpa(){
        assertEquals(HSDPA, GSMDiagnosticPSStatusVersion2.getDescriptionForId(4));
    }

    @Test
    public void testGetDescriptionForIdWithValue5ExpectLTE(){
        assertEquals(LTE, GSMDiagnosticPSStatusVersion2.getDescriptionForId(5));
    }

    @Test
    public void testGetDescriptionForIdWithValue6ExpectCDMA(){
        assertEquals(CDMA, GSMDiagnosticPSStatusVersion2.getDescriptionForId(6));
    }

    @Test
    public void testGetDescriptionForIdWithValue7ExpectLteCatM1(){
        assertEquals(LTE_CAT_M1, GSMDiagnosticPSStatusVersion2.getDescriptionForId(7));
    }

    @Test
    public void testGetDescriptionForIdWithValue8ExpectLteNBIot(){
        assertEquals(LTE_NB_IOT, GSMDiagnosticPSStatusVersion2.getDescriptionForId(8));
    }

}
