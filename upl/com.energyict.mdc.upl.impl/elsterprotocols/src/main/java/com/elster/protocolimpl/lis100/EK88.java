package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.registers.Lis100Register;
import com.elster.protocolimpl.lis100.registers.RegisterMap;
import com.energyict.obis.ObisCode;

/**
 * driver class for LIS100 device EK88
 * User: heuckeg
 * Date: 08.08.11
 * Time: 11:11
 */
@SuppressWarnings({"unused"})
public class EK88 extends LIS100 {

    private final Lis100Register[] registers = {
            new Lis100Register(new ObisCode(7,0,96,5, 0,255), 0, Lis100Register.STATUS_REGISTER, "momentary status"),
            new Lis100Register(new ObisCode(7,0, 0,2, 2,255), 0, Lis100Register.SOFTWARE_VERSION, "software version"),
            new Lis100Register(new ObisCode(7,0, 0,2,14,255), 0, Lis100Register.SENSOR_NUMBER, "serial number of meter on input 1"),
            new Lis100Register(new ObisCode(7,0, 0,2,11,255), 2, Lis100Register.SENSOR_NUMBER, "serial number of pressure sensor"),
            new Lis100Register(new ObisCode(7,0, 0,2,12,255), 3, Lis100Register.SENSOR_NUMBER, "serial number of temperature sensor"),
            new Lis100Register(new ObisCode(7,0,13,0, 0,255), 0, Lis100Register.H1, "Volume at measurement conditions total"),
            new Lis100Register(new ObisCode(7,0,11,0, 0,255), 0, Lis100Register.H2, "Volume at measurement conditions (programmable counter"),
            new Lis100Register(new ObisCode(7,0,13,2, 0,255), 1, Lis100Register.H1, "Volume at base conditions total"),
            new Lis100Register(new ObisCode(7,0,11,2, 0,255), 1, Lis100Register.H2, "Volume at base conditions undisturbed"),
            new Lis100Register(new ObisCode(7,0,42,0, 0,255), 2, Lis100Register.H2, "Current pressure"),
            new Lis100Register(new ObisCode(7,0,41,0, 0,255), 3, Lis100Register.H2, "Current temperature"),
            new Lis100Register(new ObisCode(7,0,11,0, 0,101), 0, Lis100Register.H2BOM, "Volume at measurement conditions (programmable counter) at begin of month"),
            new Lis100Register(new ObisCode(7,0,11,2, 0,101), 1, Lis100Register.H2BOM, "Volume at base conditions undisturbed at begin of month")
    };

    public EK88() {
        super();
    }

    public String getProtocolVersion() {
        return "$Date: 2011-09-07 10:00:00 +0200 (Mi, 7. Sep 2011) $";
    }

    // *******************************************************************************************
    // *
    // * Interface RegisterProtocol
    // *
    // *******************************************************************************************/
    /**
     * Gets a register map for an ek88
     *
     * @return RegisterMap for ek88
     */
    protected RegisterMap getRegisterMap() {
        return new RegisterMap(registers);
    }
}
