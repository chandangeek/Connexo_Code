package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 11:08
 */
public class DownstreamVoltageMonitoringMapper extends G3Mapping {

    private final Unit unit;

    public DownstreamVoltageMonitoringMapper(ObisCode obisCode, Unit unit) {
        super(obisCode);
        this.unit = unit;
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        RegisterMonitor registerMonitor = as330D.getSession().getCosemObjectFactory().getRegisterMonitor(getObisCode());
        Array thresholds = registerMonitor.readThresholds();

        String separator = "";
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < thresholds.getAllDataTypes().size(); index++) {
            sb.append(separator);
            sb.append("Threshold");
            sb.append(String.valueOf(index + 1));
            sb.append(": ");
            sb.append(((Unsigned16) thresholds.getDataType(index)).getValue());
            sb.append(" ");
            sb.append(unit.toString());
            separator = ", ";
        }
        return new RegisterValue(getObisCode(), sb.toString());
    }
}
