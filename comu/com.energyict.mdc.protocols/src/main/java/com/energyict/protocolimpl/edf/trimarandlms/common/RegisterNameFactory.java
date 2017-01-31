/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterNameFactory.java
 *
 * Created on 27 juni 2006, 14:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.common;

import com.energyict.mdc.common.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Koen
 */
public class RegisterNameFactory {

    static Map<ObisCode, String> map = new HashMap<>();

    static {
        buildRegisterNames(255);
        buildRegisterNames(0);
    }

    public static void buildRegisterNames(int fField) {
        for (int eField=1;eField<=16;eField++) {
            map.put(ObisCode.fromString("1.1.1.37."+eField+"."+fField), "Depassement Duration");
            map.put(ObisCode.fromString("1.1.1.38."+eField+"."+fField), "Depassement Quadratique");
            map.put(ObisCode.fromString("0.1.96.8."+eField+"."+fField), "Temps de fonctionnement");
            map.put(ObisCode.fromString("1.1.1.129."+eField+"."+fField), "Puissances Souscrites");

        }
        map.put(ObisCode.fromString("1.1.1.10.6."+fField), "Energie de depassement, SUM(dP) (en option EJP)");

    }

    public static String findObisCode(ObisCode obc) {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        return map.get(obisCode);

    }

}
