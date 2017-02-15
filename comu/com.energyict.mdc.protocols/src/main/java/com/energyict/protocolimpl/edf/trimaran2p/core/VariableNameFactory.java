/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author gna
 *
 */
public class VariableNameFactory implements Serializable{

	static List<VariableName> list = new ArrayList<>();

	static {
		list.add(new VariableName("ParametresPlus1", 40, Unit.get("W"), 1, 129, 2, VariableName.ABSTRACT));
		list.add(new VariableName("Parametres", 48, Unit.get("W"), 1, 129, 255, VariableName.ABSTRACT));

		list.add(new VariableName("Energies Brutes", 56, Unit.get("Wh"), 1, 8, 255, VariableName.ENERGIE));
		list.add(new VariableName("Energies Nettes", 64, Unit.get("Wh"), 1, 8, 255, VariableName.ENERGIE));
		list.add(new VariableName("Temp Fonctionnement", 152, Unit.get("min"), 1, 8, 255, VariableName.TEMPS_FONCTIONNEMENT));

		list.add(new VariableName("Arrete Journalier", 104, Unit.get("kWh"), 1, 8, 0, VariableName.ARRETE_JOURNALIER));
		list.add(new VariableName("Arrete Programmables", 112, Unit.get("kWh"), 0, 1, 255, VariableName.ARRETES_PROGRAMMABLES));	// billing perdiod counter
		list.add(new VariableName("Programmable jour", 120, Unit.get("kWh"), 1, 8, 255, VariableName.ARRETES_PROGRAMMABLES));
		list.add(new VariableName("Programmable mois", 128, Unit.get("kWh"), 1, 8, 255, VariableName.ARRETES_PROGRAMMABLES));

		list.add(new VariableName("PMaxMois", 104, Unit.get("kW"), 1, 6, 255, VariableName.PMAX));	// TODO watch out for the dubble coding of 104!
		list.add(new VariableName("Durees non Garantie", 118, Unit.get("min"), 1, 37, 255, VariableName.DUREE_DEPASSEMENT));

	}

	/**
	 * Creates a new instance of VariableNameFactory
	 */
	public VariableNameFactory() {
	}

    public static VariableName getVariableName(int variableName) throws IOException {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            VariableName obj = (VariableName)it.next();
            if (obj.getCode() == variableName) {
	            return obj;
            }
        }
        throw new IOException("VariableNameFactory, invalid variableName code "+variableName);
    }

}
