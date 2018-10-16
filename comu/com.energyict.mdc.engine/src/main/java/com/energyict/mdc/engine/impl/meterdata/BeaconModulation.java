package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.topology.Modulation;

import java.util.HashMap;
import java.util.Map;

public class BeaconModulation {

    private static Map<Integer, String> ordinalToName = new HashMap<>();

    static {
        ordinalToName.put(0, "ROBO");
        ordinalToName.put(1, "DBPSK");
        ordinalToName.put(2, "DQPSK");
        ordinalToName.put(3, "D8PSK");
        ordinalToName.put(4, "QAM16");
        ordinalToName.put(5, "SUPERROBO");
        ordinalToName.put(99, "UNKNOWN");
    }

    public static int getConnexoModulation(int beaconModulation) {
        for (Modulation modulation : Modulation.values()) {
            if (modulation.name().equalsIgnoreCase( ordinalToName.get(beaconModulation) )) {
                return modulation.ordinal();
            }
        }
        throw new IllegalArgumentException(beaconModulation + " beacon modulation is not present!");
    }

}
