/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;

import java.io.IOException;
import java.util.Date;

/**
 * @author Koen
 */
public class ObisCodeMapper {
    //TimeZone timeZone;
    private final DataReadingCommandFactory drcf;
    private final RegisterConfig regs;

    public ObisCodeMapper(DataReadingCommandFactory drcf, RegisterConfig regs) {
        this.drcf = drcf;
        this.regs = regs;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null, null);
        return (RegisterInfo) ocm.doGetRegister(obisCode, false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue;
        int billingPoint;

        // obis F code
        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
            billingPoint = obisCode.getF();
        } else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
            billingPoint = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        ObisCode oc = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255);

        // Overal transformer ratio = 1.1.0.4.4.255
        // Configuration program number = 1.1.0.2.0.255
        if (oc.equals(ObisCode.fromString("1.1.0.4.4.255"))) {
            if (read) {
                return new RegisterValue(oc, drcf.getCTVTRatio());
            } else {
                return new RegisterInfo("CTVT ratio");
            }

        } else if (oc.equals(ObisCode.fromString("1.1.0.2.0.255"))) {
            if (read) {
                return new RegisterValue(oc, drcf.getConfigInfo());
            } else {
                return new RegisterInfo("Program ID & programming date");
            }
        } else {
            if (read) {
                int regId = regs.getMeterRegisterId(oc);
                if (regId == -1) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                }
                try {
                    Date billingDate = drcf.getRegisterSet(billingPoint + 1).getRegister(regId).getBillingTimestamp();
                    Quantity quantity = drcf.getRegisterSet(billingPoint + 1).getRegister(regId).getQuantity();
                    Date date = drcf.getRegisterSet(billingPoint + 1).getRegister(regId).getMdTimestamp();
                    if (quantity != null) {
                        registerValue = new RegisterValue(obisCode, quantity, date, billingDate);
                        return registerValue;
                    } else {
                        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                    }
                } catch (IOException e) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported! " + e.toString());
                }
            } else {
                return new RegisterInfo(obisCode.toString());
            }
        }

    }

}