package com.energyict.mdc.device.topology;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (14:01)
 */
public enum Modulation {
    D8PSK(ModulationScheme.DIFFERENTIAL),
    DQPSK(ModulationScheme.DIFFERENTIAL),
    DBPSK(ModulationScheme.DIFFERENTIAL),
    C8PSK(ModulationScheme.COHERENT),
    CQPSK(ModulationScheme.COHERENT),
    CBPSK(ModulationScheme.COHERENT);

    private ModulationScheme modulationScheme;

    Modulation(ModulationScheme modulationScheme) {
        this.modulationScheme = modulationScheme;
    }

    public final ModulationScheme getModulationScheme() {
        return this.modulationScheme;
    }

}