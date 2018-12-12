package com.elster.protocolimpl.dlms.registers;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

public class RegisterMap {

    /**
     * Map to hold info of obis code to corresponding Lis200Register
     */
    private final Map<String, IReadableRegister> instances =
            new HashMap<String, IReadableRegister>();


    public RegisterMap(IReadableRegister[] registers) {
        for (IReadableRegister register : registers) {
            add(register);
        }
    }

    /**
     * add new register to map
     *
     * @param register - register to add to map
     */
    public void add(IReadableRegister register) {
        if (!instances.containsKey(register.getObisCode())) {
            instances.put(register.getObisCode(), register);
        }
    }

    public IReadableRegister forObisCode(String obisCode)
    {
        return forObisCode(ObisCode.fromString(obisCode));
    }

    /**
     * Get the Register(definition) for the given ObisCode
     *
     * @param oc - the {@link com.energyict.obis.ObisCode} from the register
     * @return the requested ObisCode
     */
    public IReadableRegister forObisCode(ObisCode oc) {
        String s = oc.toString();
        for(Map.Entry<String, IReadableRegister> entry: instances.entrySet())
        {
            if (s.matches(entry.getKey()))
            {
                return entry.getValue();
    }
    }
        return null;
    }
}
