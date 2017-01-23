package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;

import java.io.IOException;
import java.util.Date;

/** the java version of a closure ( aka a nice function pointer ) */
public abstract class AbstractValueFactory {

    private ObisCode obisCode;
    private CewePrometer proMeter;

    public abstract String getDescription();

    public AbstractValueFactory(ObisCode obisCode, CewePrometer proMeter) {
        this.obisCode = obisCode;
        this.proMeter = proMeter;
    }

    /**
     * Getter for the cewe prometer object
     *
     * @return the CewePrometer object
     */
    public CewePrometer getProMeter() {
        return proMeter;
    }

    /**
     * Getter for the obiscode object
     *
     * @return the obisCode
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Get the quantity for this register
     *
     * @return Always null, but subclasses should override this method
     * @throws IOException If there was an error reading the quantity
     */
    public Quantity getQuantity() throws IOException {
        return null;
    }

    /**
     * Since the eventTime is always the same as the toTime ... shortcut
     *
     * @return Returns always null
     * @throws IOException This method does not throw an IOException, but subclasses may
     */
    public Date getEventTime() throws IOException {
        return null;
    }

    public Date getFromTime() throws IOException {
        return null;
    }

    public Date getToTime() throws IOException {
        return null;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    /**
     * Get the actual register value for a given obisCode
     *
     * @param obisCode the obiscode of the register
     * @return The register value matching the obiscode
     * @throws IOException If there is no quantity, or an other reading error
     */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        Quantity q = getQuantity();
        if (q == null) {
            throwException(obisCode);
        }
        return new RegisterValue(obisCode, q, getEventTime(), getFromTime(), getToTime());
    }

    public String getCDescription( ) {
        String d = "";
        switch(obisCode.getC()) {
            case 0:     d = "time";                     break;
            case 1:     d = "active energy imp.";       break;
            case 2:     d = "active energy exp.";       break;
            case 3:     d = "reactive energy imp.";     break;
            case 4:     d = "reactive energy exp.";     break;
            case 5:     d = "reactive energy QI";       break;
            case 6:     d = "reactive energy QII";      break;
            case 7:     d = "reactive energy QIII";     break;
            case 8:     d = "reactive energy QIV";      break;
            case 9:     d = "apparent energy imp.";     break;
            case 10:    d = "apparent energy exp";      break;
            case 128:   d = "reactive energy ind.";     break;
            case 129:   d = "reactive energy cap.";     break;
            case 21:    d = "active energy imp. L1";    break;
            case 41:    d = "active energy imp. L2";    break;
            case 61:    d = "active energy imp. L3";    break;
            case 22:    d = "active energy exp. L1";    break;
            case 42:    d = "active energy exp. L2";    break;
            case 62:    d = "active energy exp. L3";    break;
            case 131:   d = "External register 1";      break;
            case 132:   d = "External register 2";      break;
            case 133:   d = "External register 3";      break;
            case 134:   d = "External register 4";      break;
            case 135:   d = "External register 5";      break;
            case 136:   d = "External register 6";      break;
            case 137:   d = "External register 7";      break;
            case 138:   d = "External register 8";      break;
        }
        return d;
    }

    public String getFDescription() {
        if (obisCode.getF() < 0) {
            return "VZ" + obisCode.getF();
        } else if (obisCode.getF() == 0) {
            return "VZ";
        } else {
            return "";
        }
    }

    public Unit getUnit() {
        Unit u = null;
        switch(obisCode.getC()) {
            case 1:   u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 2:   u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 3:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 4:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 5:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 6:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 7:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 8:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 9:   u = Unit.get(BaseUnit.VOLTAMPEREHOUR);         break;
            case 10:  u = Unit.get(BaseUnit.VOLTAMPEREHOUR);         break;
            case 128: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 129: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
            case 21:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 41:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 61:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 22:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 42:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 62:  u = Unit.get(BaseUnit.WATTHOUR);               break;
            case 131: u = Unit.getUndefined();                       break;
            case 132: u = Unit.getUndefined();                       break;
            case 133: u = Unit.getUndefined();                       break;
            case 134: u = Unit.getUndefined();                       break;
            case 135: u = Unit.getUndefined();                       break;
            case 136: u = Unit.getUndefined();                       break;
            case 137: u = Unit.getUndefined();                       break;
            case 138: u = Unit.getUndefined();                       break;
        }
        return u;
    }

    public int getBillingPointFromObisCode() {
        return obisCode.getF();
    }

    public String toString() {
        return obisCode.toString();
    }

    /**
     * Shorthand notation for throwing NoSuchRegisterException
     *
     * @param obisCode The obiscode of the register
     * @throws NoSuchRegisterException The exception generated by this method;
     */
    public void throwException(ObisCode obisCode) throws NoSuchRegisterException {
        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob + " is not supported!";
        throw new NoSuchRegisterException(msg);
    }

}
