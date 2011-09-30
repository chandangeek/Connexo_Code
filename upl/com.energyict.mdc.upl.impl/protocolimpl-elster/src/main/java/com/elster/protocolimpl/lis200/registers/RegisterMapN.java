package com.elster.protocolimpl.lis200.registers;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class RegisterMapN {

	/** Map to hold info of obis code to corresponding Lis200Register */
	private Map<Lis200ObisCode, Lis200RegisterN> instances = new HashMap<Lis200ObisCode, Lis200RegisterN>();


	public RegisterMapN() {
	}

	public RegisterMapN(Lis200RegisterN[] registers) {
		for (Lis200RegisterN reg: registers) {
			add(reg);
		}
	}

	/**
	 * add new register to map
	 *
	 * @param reg - LIS200 register to add to map
	 */
	public void add(Lis200RegisterN reg) {
		if (!instances.containsKey(reg.getLis200ObisCode())) {
			instances.put(reg.getLis200ObisCode(), reg);
		}
	}

	/**
	 * Get the Lis200Register(definition) for the given ObisCode
	 *
	 * @param oc
	 * 			- the {@link com.energyict.obis.ObisCode} from the register
	 *
	 * @return the requested {@link com.elster.protocolimpl.lis200.registers.Lis200RegisterN}
	 */
	public Lis200RegisterN forObisCode(ObisCode oc){
        String s = oc.toString();
        for(Map.Entry<Lis200ObisCode, Lis200RegisterN> entry: instances.entrySet()) {
            if (entry.getKey().matches(s)) {
                return entry.getValue();
            }
        }
		return null;
	}

	/**
	 * @param oc - Obis code to look for
	 *
	 * @return true if the given ObisCode is a part of the list, otherwise false
	 */
	public boolean contains(ObisCode oc){
        String s = oc.toString();
        for(Map.Entry<Lis200ObisCode, Lis200RegisterN> entry: instances.entrySet()) {
            if (entry.getKey().matches(s)) {
                return true;
            }
        }
		return false;
	}
}
