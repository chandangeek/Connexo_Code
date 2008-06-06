/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;

/**
 * @author gna
 *
 */
public class VariableNameFactory {
	
	static List list = new ArrayList();
	
	static {
		list.add(new VariableName("ParametresPlus1", 40, Unit.get("W"), 1, 129, 2, VariableName.ABSTRACT));
		list.add(new VariableName("Parametres", 48, Unit.get("W"), 1, 129, 255, VariableName.ABSTRACT));
		
		list.add(new VariableName("Energies Brutes", 56, Unit.get("Wh"), 1, 8, 255, VariableName.ENERGIE));
		list.add(new VariableName("Energies Nettes", 64, Unit.get("Wh"), 1, 8, 255, VariableName.ENERGIE));
		
		list.add(new VariableName("Arrete Journalier", 104, Unit.get("kWh"), 1, 8, 128, VariableName.ENERGIE));
		list.add(new VariableName("Arrete Programmables", 112, Unit.get(255), 0, 1, 255, VariableName.ABSTRACT));	// billing perdiod counter
		list.add(new VariableName("Programmable jour", 120, Unit.get("kWh"), 1, 8, 255, VariableName.ENERGIE));
		list.add(new VariableName("Programmable mois", 128, Unit.get("kWh"), 1, 8, 255, VariableName.ENERGIE));
		
	}

	/**
	 * Creates a new instance of VariableNameFactory
	 */
	public VariableNameFactory() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
    static public VariableName getVariableName(int variableName) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            VariableName obj = (VariableName)it.next();
            if (obj.getCode() == variableName)
                return obj;
        }
        throw new IOException("VariableNameFactory, invalid variableName code "+variableName);
    }

}
