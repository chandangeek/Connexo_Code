/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.util.ArrayList;
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
		// String description, int code, Unit unit, int obisCField, int obisDField, int obisFField, int type
		list.add(new VariableName("ParametresPlus1", 40, Unit.get("W"), 1, 129, 2, VariableName.ABSTRACT));
		list.add(new VariableName("Parametres", 48, Unit.get("W"), 1, 129, 255, VariableName.ABSTRACT));
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

}
