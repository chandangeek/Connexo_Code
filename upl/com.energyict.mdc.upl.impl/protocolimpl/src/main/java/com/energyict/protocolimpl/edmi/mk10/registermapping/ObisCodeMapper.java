/*
 * ObisCodeMapper.java
 *
 * Created on 23 maart 2006, 15:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author koen
 */
public class ObisCodeMapper {

    CommandLineProtocol protocol;
    private ObisCodeFactory obisCodeFactory;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(CommandLineProtocol protocol) {
        this.protocol = protocol;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        int billingPoint;

        // obis F code
        if ((obisCode.getF() >= -99) && (obisCode.getF() <= 99)) {
            billingPoint = Math.abs(obisCode.getF());
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
        if (obisCode.toString().contains("1.0.96.1.0.255")) {
            return new RegisterValue(obisCode, getProtocol().getCommandFactory().getReadCommand(MK10Register.SYSTEM_SERIALNUMBER).getRegister().getString());
        } else if (obisCode.toString().contains("1.0.96.2.0.255")) {
            return new RegisterValue(obisCode, getProtocol().getCommandFactory().getReadCommand(MK10Register.SYSTEM_MODEL_ID).getRegister().getString());
        } else if (obisCode.toString().contains("1.0.0.2.0.255")) {
            return new RegisterValue(obisCode, getProtocol().getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_VERSION).getRegister().getString());
        } else if (obisCode.toString().contains("1.0.0.2.8.255")) {
            return new RegisterValue(obisCode, getProtocol().getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_REVISION).getRegister().getString());
        } else if (obisCode.toString().contains("1.1.0.2.8.255")) {
            return new RegisterValue(obisCode, getProtocol().getCommandFactory().getReadCommand(MK10Register.SYSTEM_BOOTLOADER_REVISION).getRegister().getString());
        } else if ((obisCode.toString().contains("1.0.0.1.0.255")) || (obisCode.toString().contains("1.1.0.1.0.255"))) { // billing counter
            return new RegisterValue(obisCode, new Quantity(new BigDecimal("" + getObisCodeFactory().getBillingInfo().getNrOfBillingResets()), Unit.get("")));
        } // billing counter
        else if ((obisCode.toString().contains("1.0.0.1.2.")) || (obisCode.toString().contains("1.1.0.1.2."))) { // billing point timestamp
            if (billingPoint == 0) {
                return new RegisterValue(obisCode, getObisCodeFactory().getBillingInfo().getToDate());
            } else if (billingPoint == 1) {
                return new RegisterValue(obisCode, getObisCodeFactory().getBillingInfo().getFromDate());
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } else if ((obisCode.toString().contains("1.0.0.4.2.255")) || (obisCode.toString().contains("1.1.0.4.2.255"))) { // CT numerator
            return new RegisterValue(obisCode, new Quantity(getProtocol().getCommandFactory().getReadCommand(MK10Register.CT_MULTIPLIER).getRegister().getBigDecimal(), Unit.get("")));
        } else if ((obisCode.toString().contains("1.0.0.4.3.255")) || (obisCode.toString().contains("1.1.0.4.3.255"))) { // VT numerator
            return new RegisterValue(obisCode, new Quantity(getProtocol().getCommandFactory().getReadCommand(MK10Register.VT_MULTIPLIER).getRegister().getBigDecimal(), Unit.get("")));
        } else if ((obisCode.toString().contains("1.0.0.4.5.255")) || (obisCode.toString().contains("1.1.0.4.5.255"))) { // CT denominator
            return new RegisterValue(obisCode, new Quantity(getProtocol().getCommandFactory().getReadCommand(MK10Register.CT_DIVISOR).getRegister().getBigDecimal(), Unit.get("")));
        } else if ((obisCode.toString().contains("1.0.0.4.6.255")) || (obisCode.toString().contains("1.1.0.4.6.255"))) { // VT denominator
            return new RegisterValue(obisCode, new Quantity(getProtocol().getCommandFactory().getReadCommand(MK10Register.VT_DIVISOR).getRegister().getBigDecimal(), Unit.get("")));
        } else {
            // *********************************************************************************
            // electricity related registers
            return getObisCodeFactory().getRegisterValue(obisCode);
        }
    }

    public ObisCodeFactory getObisCodeFactory() {
        if (obisCodeFactory == null) {
            obisCodeFactory = new ObisCodeFactory(getProtocol());
        }
        return obisCodeFactory;
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }
}