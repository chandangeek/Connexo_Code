package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * reads a given register
 * <p/>
 * Created by heuckeg on 03.07.2014.
 */
public class RegisterReader {

    private final DlmsSession session;

    public RegisterReader(DlmsSession session) {
        this.session = session;
    }

    public final RegisterValue readRegister(ObisCode obisCode) throws IOException {
        A1800Register regDef = A1800Register.find(obisCode);

        if (regDef == null) {
            throw new NoSuchRegisterException("No such register with obis code " + obisCode.toString());
        }

        switch (regDef.getClassId()) {
            case 1:
                return readClass1(regDef);
            case 3:
                return readClass3(regDef);
            case 4:
                return readClass4(regDef);
            default:
                throw new NoSuchRegisterException("Unsupported class - obis code" + obisCode.toString() + " class:" + regDef.getClassId());
        }
    }

    private RegisterValue readClass1(A1800Register register) {
        try {
            Data d = getCosemObjectFactory().getData(register.getObisCode());
            if (d.getValueAttr().isOctetString()) {
                String s = ((OctetString) d.getValueAttr()).stringValue().trim();
                return new RegisterValue(register.getObisCode(), s);
            }
            if (d.getValueAttr().isVisibleString()) {
                String s = ((VisibleString) d.getValueAttr()).getStr().trim();
                return new RegisterValue(register.getObisCode(), s);
            }

            try {
                BigDecimal value = d.getValueAttr().toBigDecimal();
                Quantity quantity = new Quantity(value, Unit.get(BaseUnit.UNITLESS, 0));
                return new RegisterValue(register.getObisCode(), quantity, new Date());
            } catch (Exception ex) {
                throw new NoSuchRegisterException("Unsupported value attribute type - obis code" + register.getObisCode().toString());
            }
        } catch (IOException e) {
            session.getLogger().severe("Unable to read data (" + register.getObisCode().toString() + ")! [" + e.getMessage() + "]");
        }
        return null;
    }

    private RegisterValue readClass3(A1800Register register) throws NoSuchRegisterException {
        Register r;
        try {
            r = getCosemObjectFactory().getRegister(register.getObisCode());
        } catch (IOException e) {
            throw new NoSuchRegisterException("Unable to read data (" + register.getObisCode().toString() + ")! [" + e.getMessage() + "]");
        }
        try {
            Quantity quantity = r.getQuantityValue();
            return new RegisterValue(register.getObisCode(), quantity, null, null, null, new Date());
        } catch (Exception ex) {
            throw new NoSuchRegisterException("Unsupported value attribute type - obis code" + register.getObisCode().toString());
        }
    }

    private RegisterValue readClass4(A1800Register register) throws NoSuchRegisterException {
        ExtendedRegister r;
        try {
            r = getCosemObjectFactory().getExtendedRegister(register.getObisCode());
        } catch (IOException e) {
            throw new NoSuchRegisterException("Unable to read data (" + register.getObisCode().toString() + ")! [" + e.getMessage() + "]");
        }
        try {
            Quantity quantity = r.getQuantityValue();
            Date d = r.getCaptureTime();
            return new RegisterValue(register.getObisCode(), quantity, d, null, null, new Date());
        } catch (Exception ex) {
            throw new NoSuchRegisterException("Unsupported value attribute type - obis code" + register.getObisCode().toString());
        }
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }
}
