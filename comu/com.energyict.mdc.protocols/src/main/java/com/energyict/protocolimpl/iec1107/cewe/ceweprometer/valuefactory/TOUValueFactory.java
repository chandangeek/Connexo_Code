package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 19/05/11
 * Time: 16:49
 */
public class TOUValueFactory extends AbstractValueFactory {

    private int touPhenomenon;

    public TOUValueFactory(ObisCode obisCode, int touPhenomenon, CewePrometer proMeter) {
        super(obisCode, proMeter);
        this.touPhenomenon = touPhenomenon;
    }

    public Quantity getQuantity() throws IOException {

        int phenomenon = getProMeter().getTouIndex(touPhenomenon);
        if (phenomenon == -1) {
            throw new NoSuchRegisterException();
        }

        int row = getProMeter().getRow(getBillingPointFromObisCode());
        if (row == -1) {
            String msg = "No historical data for billing point: " + getBillingPointFromObisCode();
            throw new NoSuchRegisterException(msg);
        }

        return new Quantity(getProMeter().getRegisters().getrTou()[row][phenomenon].asDouble(getRate()), getUnit());
    }

    public Date getToTime() throws IOException {
        if (getBillingPointFromObisCode() == 255) {
            return null;
        }
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        return getProMeter().getRegisters().getrTimestamp()[row].asDate();
    }

    public int getRate() {
        return getObisCode().getE() - 1;
    }

    /* meter rate is 0 based ... */
    public String getEDescription() {
        return "rate " + (getRate() + 1);
    }

    public String getDescription() {
        return getCDescription() + ", " + getEDescription() + ", " + getFDescription();
    }

}
