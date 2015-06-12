package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.DLMSStoredValues;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedData;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author sva
 * @since 7/10/13 - 15:52
 */
public class RegisterFactory implements BulkRegisterProtocol {

    private static final int BULK_REQUEST_LIMIT = 5;  //Each bulk request can request attributes for up to 5 different registers.
    private static final ObisCode BILLING_PROFILE_OBIS = ObisCode.fromString("0.0.98.1.0.255");

    private IskraMT880 protocol;
    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    private Map<Register, ComposedData> composedDataMap = new HashMap<Register, ComposedData>();
    private DLMSStoredValues storedValues;

    public RegisterFactory(final IskraMT880 protocol) {
        this.protocol = protocol;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo(register.getObisCode().getDescription());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<Register> toRead;
        List<RegisterValue> result = new ArrayList<RegisterValue>();

        // Read out all billing registers
        result.addAll(doReadBillingRegisters(getBillingRegisters(registers)));

        // Read out all instant registers
        List<Register> nonBillingRegisters = getNonBillingRegisters(registers);

        int count = 0;
        while (((count + 1) * BULK_REQUEST_LIMIT) <= nonBillingRegisters.size()) {    //Read out in steps of 5 registers
            toRead = nonBillingRegisters.subList(count * BULK_REQUEST_LIMIT, (count + 1) * BULK_REQUEST_LIMIT);
            result.addAll(doReadRegisters(toRead));
            count++;
        }
        result.addAll(doReadRegisters(nonBillingRegisters.subList(count * BULK_REQUEST_LIMIT, nonBillingRegisters.size()))); //Read out the remaining registers
        return result;
    }

    private List<RegisterValue> doReadBillingRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        for (Register register : registers) {
            DLMSStoredValues storedValues = getStoredValues();
            if (storedValues != null) {
                try {
                    HistoricalValue historicalValue = storedValues.getHistoricalValue(register.getObisCode());
                    registerValues.add(new RegisterValue(register, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate()));
                } catch (NoSuchRegisterException e) {
                    protocol.getLogger().warning(e.getMessage());
                }
            }
        }
        return registerValues;
    }

    private List<RegisterValue> doReadRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, protocol.supportsBulkRequests());
        for (Register register : registers) {
            RegisterValue rv = null;
            try {
                if (composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        Date eventTime = null;   //Optional capture time attribute
                        DLMSAttribute registerCaptureTime = composedRegisterMap.get(register).getRegisterCaptureTime();
                        if (registerCaptureTime != null) {
                            AbstractDataType attribute = registerComposedCosemObject.getAttribute(registerCaptureTime);
                            eventTime = attribute.getOctetString().getDateTime(protocol.getDlmsSession().getTimeZone()).getValue().getTime();
                        }
                        rv = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        su.getEisUnit()), eventTime);
                    } else {
                        throw new NoSuchRegisterException("Register with ObisCode: " + register.getObisCode() + " does not provide a proper Unit.");
                    }

                } else if (composedDataMap.containsKey(register)) {
                    AbstractDataType data = registerComposedCosemObject.getAttribute(composedDataMap.get(register).getDataValueAttribute());
                    if (data.isOctetString()) {
                        OctetString octetString = data.getOctetString();
                        registerValues.add(new RegisterValue(register, octetString.stringValue()));
                    } else if (data.isBooleanObject()) {
                        BooleanObject booleanObject = data.getBooleanObject();
                        registerValues.add(new RegisterValue(register, new Quantity(booleanObject.intValue(), Unit.getUndefined())));
                    } else if (data.isNumerical()) {
                        registerValues.add(new RegisterValue(register, new Quantity(data.intValue(), Unit.getUndefined())));
                    } else {
                        protocol.getLogger().warning("Failed to read register " + register.getObisCode() + " - Value of DATA object could not be read out as a register");
                    }
                }
            } catch (DataAccessResultException e) {
                protocol.getLogger().severe("Failed to read register" + register.getObisCode() + ": " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                protocol.getLogger().log(Level.SEVERE, "Parsing error while fetching register with ObisCode " + register.getObisCode());
            }
            if (rv != null) {
                registerValues.add(rv);
            }
        }
        return registerValues;
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link com.energyict.protocol.Register} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
     * and the ObisCode is listed in the ObjectList(see {@link com.energyict.dlms.DLMSMeterConfig#getInstance(String)}, then we define a ComposedRegister and add
     * it to the {@link #composedRegisterMap}. Otherwise if it is not a DLMS <CODE>Register</CODE> or <CODE>ExtendedRegister</CODE>, but a DLMS <CODE>DATA</CODE> object,
     * then we add it to the {@link #composedDataMap}.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    protected ComposedCosemObject constructComposedObjectFromRegisterList(final List<Register> registers, final boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = register.getObisCode();

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        DLMSAttribute valueAttribute = new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute captureTimeAttribute = null;  //Optional attribute
                        if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                            captureTimeAttribute = new DLMSAttribute(rObisCode, ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());
                        }
                        ComposedRegister composedRegister = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        if (composedRegister.getRegisterCaptureTime() != null) {
                            dlmsAttributes.add(composedRegister.getRegisterCaptureTime());
                        }
                        composedRegisterMap.put(register, composedRegister);
                    } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                        DLMSAttribute valueAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute captureTimeAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());
                        ComposedRegister composedRegister = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterCaptureTime());
                        composedRegisterMap.put(register, composedRegister);
                    } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                        DLMSAttribute dataAttribute = new DLMSAttribute(uo.getObisCode(), DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID());
                        ComposedData composedData = new ComposedData(dataAttribute);
                        dlmsAttributes.add(composedData.getDataValueAttribute());
                        composedDataMap.put(register, composedData);
                    } else {
                        protocol.getLogger().warning("Failed to read register " + register.getObisCode() + " - Objects of class " + uo.getDLMSClassId() + " cannot be read out as a register.");
                    }
                } else {
                    protocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported - Object not found in meter's instantiated object list!");
                }
            }
            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }

        return null;
    }

    private List<Register> getBillingRegisters(List<Register> registers) {
        List<Register> billingRegisters = new ArrayList<Register>(registers.size());
        for (Register register : registers) {
            if (register.getObisCode().getF() != 255) {
                billingRegisters.add(register);
            }
        }

        return billingRegisters;
    }

    private List<Register> getNonBillingRegisters(List<Register> registers) {
        List<Register> nonBillingRegisters = new ArrayList<Register>(registers.size());
        for (Register register : registers) {
            if (register.getObisCode().getF() == 255) {
                nonBillingRegisters.add(register);
            }
        }

        return nonBillingRegisters;
    }

    private DLMSStoredValues getStoredValues() throws IOException {
        if (storedValues == null) {
            storedValues = new DLMSStoredValues(protocol.getDlmsSession().getCosemObjectFactory(), BILLING_PROFILE_OBIS);
        }
        return storedValues;
    }
}
