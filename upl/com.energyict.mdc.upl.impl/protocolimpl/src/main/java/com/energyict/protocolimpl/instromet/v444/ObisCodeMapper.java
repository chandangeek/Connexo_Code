package com.energyict.protocolimpl.instromet.v444;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.instromet.v444.tables.PeakHourPeakDayTable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

public class ObisCodeMapper {

    private Instromet444 instromet444;

    public ObisCodeMapper(Instromet444 instromet444) {
        this.instromet444 = instromet444;
    }

    public String getRegisterInfo() throws IOException {
        return instromet444.getRegisterFactory().getRegisterInfo();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (obisCode.getD() == 10) {
            BigDecimal value =
                    instromet444.getTableFactory().getCountersTable().getUnCorrectedVolume();
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("m3")));
        } else if (obisCode.getD() == 1) {
            BigDecimal value =
                    instromet444.getTableFactory().getCountersTable().getCorrectedVolume();
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("m3")));
        } else if (obisCode.getD() == 5) {
            PeakHourPeakDayTable peakTable =
                    instromet444.getTableFactory().getPeakHourPeakDayTable();
            BigDecimal value = peakTable.getPeak();
            Date peakTime = peakTable.getPeakTime();
            return new RegisterValue(
                    obisCode, new Quantity(value, Unit.get("m3/h")), peakTime);
        } else {
            throw new NoSuchRegisterException(
                    "ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

}