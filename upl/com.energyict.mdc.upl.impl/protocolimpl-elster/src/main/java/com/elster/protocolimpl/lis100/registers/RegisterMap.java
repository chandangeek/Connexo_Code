package com.elster.protocolimpl.lis100.registers;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class RegisterMap {

    /**
     * Map to hold info of obis code to corresponding Lis200Register
     */
    private Map<ObisCode, Lis100Register> instances = new HashMap<ObisCode, Lis100Register>();


    public RegisterMap() {

    }

    public RegisterMap(Lis100Register[] registers) {
        for (Lis100Register reg: registers) {
            add(reg);
        }
    }

    /**
     * add new register to map
     *
     * @param reg - LIS100 register to add to map
     */
    public void add(Lis100Register reg) {
        if (!instances.containsKey(reg.getObisCode())) {
            instances.put(reg.getObisCode(), reg);
        }
    }

    /**
     * Get the Lis100Register(definition) for the given ObisCode
     *
     * @param oc - the {@link com.energyict.obis.ObisCode} from the register
     * @return the requested lis 100 register
     */
    public Lis100Register forObisCode(ObisCode oc) {
        return instances.get(oc);
    }

    /**
     * @param oc - the {@link com.energyict.obis.ObisCode} to check
     * @return true if the given ObisCode is a part of the list, otherwise false
     */
    public boolean contains(ObisCode oc) {
        return instances.containsKey(oc);
    }
}
