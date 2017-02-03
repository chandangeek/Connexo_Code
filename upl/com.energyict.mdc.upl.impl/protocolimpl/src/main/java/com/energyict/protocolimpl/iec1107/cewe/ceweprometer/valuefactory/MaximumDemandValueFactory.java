package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ObisCodeMapper;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;
import java.util.Date;

/**
* Copyrights EnergyICT
* Date: 19/05/11
* Time: 16:49
*/ /* Maximum demands
 *
 * (quantity,yympddhhmm,highest,yymmddhhmm,second,yymmddhhmm,third)
 *     0         1         2        3        4        5        6
 */
public class MaximumDemandValueFactory extends AbstractValueFactory {

    private ProRegister maximum = null;
    private int maximumPhenomenon;

    public MaximumDemandValueFactory(ObisCode obisCode, int maxPhenomenon, CewePrometer prometer) {
        super(obisCode, prometer);
        this.maximumPhenomenon = maxPhenomenon;
    }

    public ProRegister getMaximum() throws IOException {
        if (maximum == null) {

            int row = getProMeter().getRow(getBillingPointFromObisCode());
            if (row == -1) {
                String msg = "No historical data for billing point: " + getBillingPointFromObisCode();
                throw new NoSuchRegisterException(msg);
            }

            for (int i = 0; i < getProMeter().getRegisters().getrMaximumDemand()[row].length; i++) {
                if (getProMeter().getRegisters().getrMaximumDemand()[row][i].asInt(0) == maximumPhenomenon) {
                    maximum = getProMeter().getRegisters().getrMaximumDemand()[row][i];
                }
            }

        }

        if (maximum == null) {
            throw new NoSuchRegisterException(getObisCode().toString());
        }

        return maximum;
    }

    public Quantity getQuantity() throws IOException {
        return new Quantity(getMaximum().asDouble(getQuantityFieldIndex()), getUnit());
    }

    public Date getEventTime() throws IOException {

        int idx = getDateFieldIndex();

        if ("7001010000".equals(getMaximum().asString(idx))) {
            throw new NoSuchRegisterException(getObisCode().toString());
        }

        return getMaximum().asShortDate(idx);

    }

    public Date getToTime() throws IOException {
        if (getBillingPointFromObisCode() == 255) {
            return null;
        }
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        return getProMeter().getRegisters().getrTimestamp()[row].asDate();
    }

    public int getRank() {
        switch( getObisCode().getD() ) {
            case ObisCodeMapper.D_MD_1: return 0;
            case ObisCodeMapper.D_MD_2: return 1;
            case ObisCodeMapper.D_MD_3: return 2;
        }
        throw new IllegalArgumentException("No such Max demand");
    }

    public int getDateFieldIndex(){
        switch( getObisCode().getD() ) {
            case ObisCodeMapper.D_MD_1: return 1;
            case ObisCodeMapper.D_MD_2: return 3;
            case ObisCodeMapper.D_MD_3: return 5;
        }
        throw new IllegalArgumentException("No such Max demand");
    }

    public int getQuantityFieldIndex(){
        switch( getObisCode().getD() ) {
            case ObisCodeMapper.D_MD_1: return 2;
            case ObisCodeMapper.D_MD_2: return 4;
            case ObisCodeMapper.D_MD_3: return 6;
        }
        throw new IllegalArgumentException("No such Max demand");
    }

    public String getDescription( ){
        String d = getCDescription();
        switch( getObisCode().getD() ){
            case ObisCodeMapper.D_MD_1: d = getCDescription() + ",";           break;
            case ObisCodeMapper.D_MD_2: d = getCDescription() + ", second";    break;
            case ObisCodeMapper.D_MD_3: d = getCDescription() + ", third";     break;
        }
        return d + " highest, " + getFDescription();
    }

    public Unit getUnit( ) {

        Unit u = null;
        switch(getObisCode().getC()) {
            case 1:   u = Unit.get(BaseUnit.WATT);               break;
            case 2:   u = Unit.get(BaseUnit.WATT);               break;
            case 3:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 4:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 5:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 6:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 7:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 8:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 9:   u = Unit.get(BaseUnit.VOLTAMPERE);         break;
            case 10:  u = Unit.get(BaseUnit.VOLTAMPERE);         break;
            case 128: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 129: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
            case 21:  u = Unit.get(BaseUnit.WATT);               break;
            case 41:  u = Unit.get(BaseUnit.WATT);               break;
            case 61:  u = Unit.get(BaseUnit.WATT);               break;
            case 22:  u = Unit.get(BaseUnit.WATT);               break;
            case 42:  u = Unit.get(BaseUnit.WATT);               break;
            case 62:  u = Unit.get(BaseUnit.WATT);               break;
            case 131: u = Unit.getUndefined();                   break;
            case 132: u = Unit.getUndefined();                   break;
            case 133: u = Unit.getUndefined();                   break;
            case 134: u = Unit.getUndefined();                   break;
            case 135: u = Unit.getUndefined();                   break;
            case 136: u = Unit.getUndefined();                   break;
            case 137: u = Unit.getUndefined();                   break;
            case 138: u = Unit.getUndefined();                   break;
        }
        return u;
    }

}
