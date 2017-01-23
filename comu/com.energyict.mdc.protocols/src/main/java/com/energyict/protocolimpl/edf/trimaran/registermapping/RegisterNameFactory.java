/*
 * RegisterNameFactory.java
 *
 * Created on 27 juni 2006, 14:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.registermapping;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Koen
 */
public class RegisterNameFactory {

    static Map<ObisCode, String> map= new HashMap<>();

    static {
        buildRegisterNames(255);
        buildRegisterNames(0);
    }

    public static void buildRegisterNames(int fField) {
        for (int eField=1;eField<=3;eField++) {
            map.put(ObisCode.fromString("1.1.1.38."+eField+"."+fField), "Dépassement Quadratique");
            map.put(ObisCode.fromString("0.1.96.8."+eField+"."+fField), "Temps de fonctionnement");
            map.put(ObisCode.fromString("1.1.1.128."+eField+"."+fField), "Nombre de dépassements");

        }
        map.put(ObisCode.fromString("1.1.1.10.1."+fField), "Energie de depassement, SUM(dP) (en option EJP)");
        map.put(ObisCode.fromString("1.1.1.129.1."+fField), "Puissances Souscrites, PSP (Heures de Pointe)");
        map.put(ObisCode.fromString("1.1.1.130.1."+fField), "Puissances Souscrites, PS HPH (Heures Pleines d'Hiver)");
        map.put(ObisCode.fromString("1.1.1.131.1."+fField), "Puissances Souscrites, PS HCH (Heures Creuses d'Hiver");
        map.put(ObisCode.fromString("1.1.1.132.1."+fField), "Puissances Souscrites, PS HPE (Heures Pleines d'Eté");
        map.put(ObisCode.fromString("1.1.1.133.1."+fField), "Puissances Souscrites, PS HCE (Heures Creuses d'Eté");
        map.put(ObisCode.fromString("1.1.1.134.1."+fField), "Puissances Souscrites, PS PM (Heures de Pointe Mobile)");
        map.put(ObisCode.fromString("1.1.1.135.1."+fField), "Puissances Souscrites, PS HPD (Heures Pleines de Demi-Saison)");
        map.put(ObisCode.fromString("1.1.1.136.1."+fField), "Puissances Souscrites, PS HCD (Heures Creuses de Demi-Saison)");
        map.put(ObisCode.fromString("1.1.1.137.1."+fField), "Puissances Souscrites, PS CC (Heures Creuses de Saison Creuse)");
        map.put(ObisCode.fromString("1.1.1.138.1."+fField), "Coefficient, Rapport TCxTT");

    }

    /** Creates a new instance of RegisterNameFactory */
    public RegisterNameFactory() {

    }

    public static String findObisCode(ObisCode obc) {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        return map.get(obisCode);

    }

}