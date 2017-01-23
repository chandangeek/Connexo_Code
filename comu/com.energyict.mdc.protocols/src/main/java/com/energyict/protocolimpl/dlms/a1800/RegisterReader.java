package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * reads a given register
 * <p/>
 * Created by heuckeg on 03.07.2014.
 */
public class RegisterReader {

    private static final int TRANSFORMER_RATIO_DENOMINATOR = 10000;
    private final DlmsSession session;
    private Map<String, BigDecimal> multiplierMap;
    private Map<String, BigDecimal> transformerMap;

    public RegisterReader(DlmsSession session) {
        this.session = session;
        this.multiplierMap = new HashMap();
        this.transformerMap = new HashMap();
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
            quantity = applyMultiplier(register, quantity);
            quantity = applyTransformerRatios(register, quantity);
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
            quantity = applyMultiplier(register, quantity);
            quantity = applyTransformerRatios(register, quantity);
            Date d = r.getCaptureTime();
            return new RegisterValue(register.getObisCode(), quantity, d, null, null, new Date());
        } catch (Exception ex) {
            throw new NoSuchRegisterException("Unsupported value attribute type - obis code" + register.getObisCode().toString());
        }
    }

    /**
     * Check whether or not a multiplier should be applied,
     * and if so, apply the correct one (either instrumentation one or non-instrumentation one).
     */
    private Quantity applyMultiplier(A1800Register register, Quantity quantity) throws NoSuchRegisterException {
        BigDecimal value = quantity.getAmount();
        BigDecimal multiplier = getMultiplier(register);
        if (multiplier != null) {
            value = new BigDecimal(value.floatValue() * multiplier.floatValue());
        }
        return new Quantity(value, quantity.getUnit());
    }

    /**
     * Check whether or not a transformer ratio should be applied,
     * and if so, apply the correct one (either Current Transformer and/or Voltage Transformer).
     */
    private Quantity applyTransformerRatios(A1800Register register, Quantity quantity) throws NoSuchRegisterException {
        if (((A1800Properties)session.getProperties()).needToApplyTransformerRatios()) {
            for (String transformerObisCode : register.getTransformerObisCodes()) {
                BigDecimal value = quantity.getAmount();
                BigDecimal transformerRatio = getTransformerRatio(transformerObisCode);
                if (transformerRatio != null) {
                    value = new BigDecimal(value.floatValue() * transformerRatio.floatValue());
                }
                quantity = new Quantity(value, quantity.getUnit());

            }
        }
        return quantity;
    }

    private BigDecimal getMultiplier(A1800Register register) throws NoSuchRegisterException {
        String multiplierObisCode = register.getMultiplierObisCode();
        if (multiplierObisCode == null) {
            return null;
        }

        if (!getMultiplierMap().containsKey(multiplierObisCode)) {
            A1800Register regDef = A1800Register.find(ObisCode.fromString(multiplierObisCode));
            getMultiplierMap().put(multiplierObisCode, readClass1(regDef).getQuantity().getAmount());
        }
        return  getMultiplierMap().get(multiplierObisCode);
    }

    private BigDecimal getTransformerRatio(String transformerObisCode) throws NoSuchRegisterException {
        if (!getTransformerMap().containsKey(transformerObisCode)) {
            A1800Register regDef = A1800Register.find(ObisCode.fromString(transformerObisCode));
            BigDecimal transformerRatioNominator = readClass1(regDef).getQuantity().getAmount();
            getTransformerMap().put(transformerObisCode, new BigDecimal(transformerRatioNominator.floatValue() / TRANSFORMER_RATIO_DENOMINATOR)); //TODO: not sure if this should be fixed, should be investigated further
        }
        return  getTransformerMap().get(transformerObisCode);
    }

    private Map<String, BigDecimal> getMultiplierMap() {
        return multiplierMap;
    }

    private Map<String, BigDecimal> getTransformerMap() {
        return transformerMap;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }
}