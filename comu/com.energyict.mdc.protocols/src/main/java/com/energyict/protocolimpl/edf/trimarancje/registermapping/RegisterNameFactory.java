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

package com.energyict.protocolimpl.edf.trimarancje.registermapping;

import com.energyict.mdc.common.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Koen
 */
public class RegisterNameFactory {
	private static String[] zone = {"A", "B", "C", "D"};
	private static String[] tarif = {"Pointe Mobile", "Pointe Fixe", "Heures Pleine Hiver", "Heures Creuse Hiver", "Heures Pleine Ete", "Heures Creuses Ete"};
	private static int[] rate={6, 1, 4, 5, 11, 12};

    static Map<ObisCode, String> map = new HashMap<>();

    static {
        buildRegisterNames(255);
        buildRegisterNames(0);
        buildRegisterNames(1);
    }

    public static void buildRegisterNames(int fField) {
    	for(int eField = 17; eField <= 20; eField++){
    		map.put(ObisCode.fromString("1.1.1.129."+eField+"."+fField), "Puissances Souscrites dans la zone tarifaire " + zone[eField-17] );
    		map.put(ObisCode.fromString("1.1.1.6."+eField+"."+fField), "Puissances apparente maximal dans la zone tarifaire " + zone[eField-17] );
    		map.put(ObisCode.fromString("0.1.96.8."+eField+"."+fField), "Temps de fonctionnement pendant la zone tarifaire " + zone[eField-17]);
    		map.put(ObisCode.fromString("1.1.1.37."+eField+"."+fField), "Durée totale de dépassement de la puissance souscrite dans la zone tarifaire" + zone[eField-17]);
    		map.put(ObisCode.fromString("1.1.1.139."+eField+"."+fField), "Coefficients de dépassement dans la zone tarifaire " + zone[eField-17]);
    	}

    	for(int eField = 0; eField <= 5; eField++){
    		map.put(ObisCode.fromString("1.1.1.8."+rate[eField]+"."+fField), "Active Energy tarifaire " + tarif[eField]);
    	}
    }

    /** Creates a new instance of RegisterNameFactory */
    public RegisterNameFactory() {

    }

    public static String findObisCode(ObisCode obc) {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        return map.get(obisCode);

    }

}
