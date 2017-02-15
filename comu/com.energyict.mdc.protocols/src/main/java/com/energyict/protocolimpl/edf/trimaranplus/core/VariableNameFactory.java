/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * VariableNameFactory.java
 *
 * Created on 22 februari 2007, 11:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class VariableNameFactory {

    static List list = new ArrayList();

    static {

        list.add(new VariableName("ParametresPplus1",40,Unit.get("kW"),1,129,2,VariableName.ABSTRACT));
        list.add(new VariableName("ParametresP",48,Unit.get("kW"),1,129,255,VariableName.ABSTRACT));
        list.add(new VariableName("ParametresPmoins1",168,Unit.get("kW"),1,129,0,VariableName.ABSTRACT));
        list.add(new VariableName("ParametresPmoins2",288,Unit.get("kW"),1,129,1,VariableName.ABSTRACT));

        list.add(new VariableName("EnergieActivePeriodeP",56, Unit.get("kWh"),1,8,255,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePeriodeP",64, Unit.get("kvarh"),3,8,255,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePeriodeP",72, Unit.get("kvarh"),4,8,255,VariableName.ENERGIE));
        list.add(new VariableName("EnergieActivePosteP",80, Unit.get("kWh"),1,8,255,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePosteP",88, Unit.get("kvarh"),3,8,255,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePosteP",96, Unit.get("kvarh"),4,8,255,VariableName.ENERGIE));
        list.add(new VariableName("PmaxPeriodeP",104, Unit.get("kW"),1,6,255,VariableName.PMAX));
        list.add(new VariableName("PmaxPosteP",112, Unit.get("kW"),1,6,255,VariableName.PMAX));
        list.add(new VariableName("DureeDepassementPeriodeP",120, Unit.get("min"),1,37,255,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DureeDepassementPosteP",128, Unit.get("min"),1,37,255,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DepassementQuadratiquePeriodeP",136, Unit.get("kW"),1,38,255,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePosteP",144, Unit.get("kW"),1,38,255,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePeriodeP",1136, Unit.get("kWh"),1,10,255,VariableName.DEPASSEMENT_QUADRATIUQUE)); // CUSTOM variable name + 1000
        list.add(new VariableName("DepassementQuadratiquePosteP",1144, Unit.get("kWh"),1,10,255,VariableName.DEPASSEMENT_QUADRATIUQUE));  // CUSTOM variable name + 1000
        list.add(new VariableName("TempsFonctionnementPeriodeP", 152, Unit.get("min"),0,96,8,255,VariableName.TEMPS_FONCTIONNEMENT));
        list.add(new VariableName("TempsFonctionnementPosteP", 160, Unit.get("min"),0,96,8,255,VariableName.TEMPS_FONCTIONNEMENT));

        list.add(new VariableName("EnergieActivePeriodePmoins1",176, Unit.get("kWh"),1,8,0,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePeriodePmoins1",184, Unit.get("kvarh"),3,8,0,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePeriodePmoins1",192, Unit.get("kvarh"),4,8,0,VariableName.ENERGIE));
        list.add(new VariableName("EnergieActivePostePmoins1",200, Unit.get("kWh"),1,8,0,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePostePmoins1",208, Unit.get("kvarh"),3,8,0,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePostePmoins1",216, Unit.get("kvarh"),4,8,0,VariableName.ENERGIE));
        list.add(new VariableName("PmaxPeriodePmoins1",224, Unit.get("kW"),1,6,0,VariableName.PMAX));
        list.add(new VariableName("PmaxPostePmoins1",232, Unit.get("kW"),1,6,0,VariableName.PMAX));
        list.add(new VariableName("DureeDepassementPeriodePmoins1",240, Unit.get("min"),1,37,0,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DureeDepassementPostePmoins1",248, Unit.get("min"),1,37,0,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DepassementQuadratiquePeriodePmoins1",256, Unit.get("kW"),1,38,0,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePostePmoins1",264, Unit.get("kW"),1,38,0,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePeriodePmoins1",1256, Unit.get("kWh"),1,10,0,VariableName.DEPASSEMENT_QUADRATIUQUE)); // CUSTOM variable name + 1000
        list.add(new VariableName("DepassementQuadratiquePostePmoins1",1264, Unit.get("kWh"),1,10,0,VariableName.DEPASSEMENT_QUADRATIUQUE));  // CUSTOM variable name + 1000
        list.add(new VariableName("TempsFonctionnementPeriodePmoins1", 272, Unit.get("min"),0,96,8,0,VariableName.TEMPS_FONCTIONNEMENT));
        list.add(new VariableName("TempsFonctionnementPostePmoins1", 280, Unit.get("min"),0,96,8,0,VariableName.TEMPS_FONCTIONNEMENT));

        list.add(new VariableName("EnergieActivePeriodePmoins2",296, Unit.get("kWh"),1,8,1,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePeriodePmoins2",304, Unit.get("kvarh"),3,8,1,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePeriodePmoins2",312, Unit.get("kvarh"),4,8,1,VariableName.ENERGIE));
        list.add(new VariableName("EnergieActivePostePmoins2",320, Unit.get("kWh"),1,8,1,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactivePositivePostePmoins2",328, Unit.get("kvarh"),3,8,1,VariableName.ENERGIE));
        list.add(new VariableName("EnergieReactiveNegativePostePmoins2",336, Unit.get("kvarh"),4,8,1,VariableName.ENERGIE));
        list.add(new VariableName("PmaxPeriodePmoins2",344, Unit.get("kW"),1,6,1,VariableName.PMAX));
        list.add(new VariableName("PmaxPostePmoins2",352, Unit.get("kW"),1,6,1,VariableName.PMAX));
        list.add(new VariableName("DureeDepassementPeriodePmoins2",360, Unit.get("min"),1,37,1,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DureeDepassementPostePmoins2",368, Unit.get("min"),1,37,1,VariableName.DUREE_DEPASSEMENT));
        list.add(new VariableName("DepassementQuadratiquePeriodePmoins2",376, Unit.get("kW"),1,38,1,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePostePmoins2",384, Unit.get("kW"),1,38,1,VariableName.DEPASSEMENT_QUADRATIUQUE));
        list.add(new VariableName("DepassementQuadratiquePeriodePmoins2",1376, Unit.get("kWh"),1,10,1,VariableName.DEPASSEMENT_QUADRATIUQUE)); // CUSTOM variable name + 1000
        list.add(new VariableName("DepassementQuadratiquePostePmoins2",1384, Unit.get("kWh"),1,10,1,VariableName.DEPASSEMENT_QUADRATIUQUE));  // CUSTOM variable name + 1000
        list.add(new VariableName("TempsFonctionnementPeriodePmoins2", 392, Unit.get("min"),0,96,8,1,VariableName.TEMPS_FONCTIONNEMENT));
        list.add(new VariableName("TempsFonctionnementPostePmoins2", 400, Unit.get("min"),0,96,8,1,VariableName.TEMPS_FONCTIONNEMENT));

    }
    /**
     * Creates a new instance of VariableNameFactory
     */
    private VariableNameFactory() {

    }

    static public VariableName getVariableName(int variableName) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            VariableName obj = (VariableName)it.next();
            if (obj.getCode() == variableName){
                return obj;
            }
        }
        throw new IOException("VariableNameFactory, invalid variableName code "+variableName);
    }
}
