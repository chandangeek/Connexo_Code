package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.obis.ObisCode;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 9:27:51
 */
public class ObisCodeMapperTest extends TestCase {
                        

    private static final ObisCode OBISCODE1 = ObisCode.fromString("7.0.13.26.0.0");
    private static final ObisCode OBISCODE2 = ObisCode.fromString("7.0.13.0.0.255");
    private static final ObisCode OBISCODE3 = ObisCode.fromString("7.0.13.2.0.255");
    private static final ObisCode OBISCODE4 = ObisCode.fromString("7.0.128.1.0.255");
    private static final ObisCode OBISCODE5 = ObisCode.fromString("7.0.43.0.0.255");
    private static final ObisCode OBISCODE6 = ObisCode.fromString("7.0.43.1.0.255");
    private static final ObisCode OBISCODE7 = ObisCode.fromString("7.0.42.0.0.255");
    private static final ObisCode OBISCODE8 = ObisCode.fromString("7.0.41.0.0.255");
    private static final ObisCode OBISCODE9 = ObisCode.fromString("7.0.52.0.0.255");
    private static final ObisCode OBISCODE10 = ObisCode.fromString("7.0.53.0.0.255");
    private static final ObisCode OBISCODE11 = ObisCode.fromString("7.0.128.2.1.255");
    private static final ObisCode OBISCODE12 = ObisCode.fromString("7.0.128.2.2.255");
    private static final ObisCode OBISCODE13 = ObisCode.fromString("7.0.128.2.3.255");
    private static final ObisCode OBISCODE14 = ObisCode.fromString("7.0.128.4.0.255");
    private static final ObisCode OBISCODE15 = ObisCode.fromString("7.0.128.5.0.0");
    private static final ObisCode OBISCODE16 = ObisCode.fromString("7.0.128.6.1.0");
    private static final ObisCode OBISCODE17 = ObisCode.fromString("7.0.128.6.2.0");
    private static final ObisCode OBISCODE18 = ObisCode.fromString("7.0.128.6.3.0");
    private static final ObisCode OBISCODE19 = ObisCode.fromString("7.0.128.8.0.255");
    private static final ObisCode OBISCODE20 = ObisCode.fromString("7.0.13.2.1.255");
    private static final ObisCode OBISCODE21 = ObisCode.fromString("7.0.13.2.2.255");
    private static final ObisCode OBISCODE22 = ObisCode.fromString("7.0.13.2.3.255");
    private static final ObisCode OBISCODE23 = ObisCode.fromString("7.0.13.2.1.0");
    private static final ObisCode OBISCODE24 = ObisCode.fromString("7.0.13.2.2.0");
    private static final ObisCode OBISCODE25 = ObisCode.fromString("7.0.13.2.3.0");
    private static final ObisCode OBISCODE26 = ObisCode.fromString("0.0.96.10.1.255");
    private static final ObisCode OBISCODE27 = ObisCode.fromString("0.0.96.10.2.255");
    private static final ObisCode OBISCODE28 = ObisCode.fromString("0.0.96.10.3.255");
    private static final ObisCode OBISCODE29 = ObisCode.fromString("0.0.96.10.4.255");
    private static final ObisCode OBISCODE30 = ObisCode.fromString("0.0.96.12.5.255");
    private static final ObisCode OBISCODE31 = ObisCode.fromString("7.0.0.9.4.255");
    private static final ObisCode OBISCODE32 = ObisCode.fromString("0.0.96.6.6.255");
    private static final ObisCode OBISCODE33 = ObisCode.fromString("0.0.96.6.0.255");
    private static final ObisCode OBISCODE34 = ObisCode.fromString("0.0.96.6.3.255");
    private static final ObisCode OBISCODE35 = ObisCode.fromString("7.0.0.2.0.255");
    private static final ObisCode OBISCODE36 = ObisCode.fromString("7.0.0.2.1.255");
    private static final ObisCode OBISCODE37 = ObisCode.fromString("7.0.0.2.2.255");
    private static final ObisCode OBISCODE38 = ObisCode.fromString("7.0.0.2.3.255");

    private static final String ID1 = "2.1.6";
    private static final String ID2 = "2.0.0";
    private static final String ID3 = "2.1.0";
    private static final String ID4 = "2.3.0";
    private static final String ID5 = "1.0.0";
    private static final String ID6 = "1.2.0";
    private static final String ID7 = "4.0.0";
    private static final String ID8 = "7.0.0";
    private static final String ID9 = "A.0.0";
    private static final String ID10 = "A.1.6";
    private static final String ID11 = "2.3.7";
    private static final String ID12 = "2.3.8";
    private static final String ID13 = "2.3.9";
    private static final String ID14 = "C.0.0";
    private static final String ID15 = "2.3.6";
    private static final String ID16 = "2.3.A";
    private static final String ID17 = "2.3.B";
    private static final String ID18 = "2.3.C";
    private static final String ID19 = "10.1.0";
    private static final String ID20 = "2.5.0";
    private static final String ID21 = "2.5.1";
    private static final String ID22 = "2.5.2";
    private static final String ID23 = "2.5.3";
    private static final String ID24 = "2.5.4";
    private static final String ID25 = "2.5.5";
    private static final String ID26 = "12.0.0";
    private static final String ID27 = "D.9.0";
    private static final String ID28 = "12.1.0";
    private static final String ID29 = "12.2.0";
    private static final String ID30 = "E.C.0";
    private static final String ID31 = "8.1.2";
    private static final String ID32 = "F.5.0";
    private static final String ID33 = "F.5.1";
    private static final String ID34 = "F.5.2";
    private static final String ID35 = "9.0.3";
    private static final String ID36 = "9.0.4";
    private static final String ID37 = "9.0.7";
    private static final String ID38 = "9.0.5";

    @Test
    public void testObisCodes() {
        ObisCodeMapper obisCodeMapper = new ObisCodeMapper(null, null);
        List<CTRRegisterMapping> list = obisCodeMapper.getRegisterMapping();

        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE1).getId(), ID1);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE2).getId(), ID2);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE3).getId(), ID3);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE4).getId(), ID4);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE5).getId(), ID5);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE6).getId(), ID6);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE7).getId(), ID7);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE8).getId(), ID8);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE9).getId(), ID9);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE10).getId(), ID10);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE11).getId(), ID11);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE12).getId(), ID12);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE13).getId(), ID13);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE14).getId(), ID14);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE15).getId(), ID15);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE16).getId(), ID16);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE17).getId(), ID17);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE18).getId(), ID18);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE19).getId(), ID19);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE20).getId(), ID20);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE21).getId(), ID21);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE22).getId(), ID22);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE23).getId(), ID23);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE24).getId(), ID24);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE25).getId(), ID25);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE26).getId(), ID26);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE27).getId(), ID27);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE28).getId(), ID28);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE29).getId(), ID29);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE30).getId(), ID30);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE31).getId(), ID31);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE32).getId(), ID32);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE33).getId(), ID33);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE34).getId(), ID34);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE35).getId(), ID35);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE36).getId(), ID36);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE37).getId(), ID37);
        assertEquals(obisCodeMapper.searchRegisterMapping(OBISCODE38).getId(), ID38);
    }
    

}