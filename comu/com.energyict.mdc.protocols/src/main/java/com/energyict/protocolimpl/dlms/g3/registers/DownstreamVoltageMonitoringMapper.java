package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
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
    public RegisterValue readRegister(DlmsSession session) throws IOException {
        RegisterMonitor registerMonitor = session.getCosemObjectFactory().getRegisterMonitor(getObisCode());
        return parse(registerMonitor.readThresholds(), unit);
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Array thresholds = ((Array) abstractDataType);

        String separator = "";
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < thresholds.getAllDataTypes().size(); index++) {
            sb.append(separator);
            sb.append("Threshold");
            sb.append(String.valueOf(index + 1));
            sb.append(": ");
            sb.append(((Unsigned16) thresholds.getDataType(index)).getValue());
            sb.append(" ");
            sb.append(this.unit.toString());
            separator = ", ";
        }
        return new RegisterValue(getObisCode(), sb.toString());
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.REGISTER_MONITOR.getClassId();
    }

}
