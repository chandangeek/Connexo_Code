package com.elster.protocolimpl.dlms.registers;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

public class RegisterMap {

    /**
     * Map to hold info of obis code to corresponding Lis200Register
     */
    private Map<ObisCode, com.elster.dlms.types.basic.ObisCode> instances =
            new HashMap<ObisCode, com.elster.dlms.types.basic.ObisCode>();


    public RegisterMap() {

    }

    public RegisterMap(DlmsRegisterMapping[] registers) {
        for (DlmsRegisterMapping register : registers) {
            add(register);
        }
    }

    /**
     * add new register to map
     *
     * @param register - LIS200 register to add to map
     */
    public void add(DlmsRegisterMapping register) {
        if (!instances.containsKey(register.getEiObisCode())) {
            instances.put(register.getEiObisCode(), register.getElObisCode());
        }
    }

    /**
     * Get the Lis200Register(definition) for the given ObisCode
     *
     * @param oc - the {@link com.energyict.obis.ObisCode} from the register
     * @return the requested ObisCode
     */
    public com.elster.dlms.types.basic.ObisCode forObisCode(ObisCode oc) {
        return instances.get(oc);
    }

    /**
     * @param oc - the {@link com.energyict.obis.ObisCode} to check
     * @return true if the given ObisCode is a part of the list, otherwise false
     */
    public boolean contains(ObisCode oc) {
        return instances.containsKey(oc);
    }

    public Map<ObisCode, com.elster.dlms.types.basic.ObisCode> getAsMap() {
        return instances;
    }
}
