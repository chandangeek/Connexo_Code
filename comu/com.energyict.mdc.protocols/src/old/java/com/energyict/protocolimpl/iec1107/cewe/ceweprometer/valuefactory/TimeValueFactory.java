package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 19/05/11
 * Time: 16:50
 */
public class TimeValueFactory extends AbstractValueFactory {

    public TimeValueFactory(ObisCode obisCode, CewePrometer proMeter) {
        super(obisCode, proMeter);
    }

    public Quantity getQuantity() throws IOException {
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        if (row == -1) {
            String msg = "No historical data for billing point: " + getBillingPointFromObisCode();
            throw new NoSuchRegisterException(msg);
        }
        Unit ms = Unit.get(BaseUnit.SECOND, -3);
        ProRegister r = getProMeter().getRegisters().getrTimestamp()[row].readAndFreeze();
        BigDecimal v = new BigDecimal(r.asDate().getTime());
        return new Quantity(v, ms);
    }

    public Date getToTime() throws IOException {
        if (getBillingPointFromObisCode() == 255) {
            return null;
        }
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        return getProMeter().getRegisters().getrTimestamp()[row].asDate();
    }

    public String getDescription() {
        return getCDescription() + ", " + getFDescription();
    }

}
