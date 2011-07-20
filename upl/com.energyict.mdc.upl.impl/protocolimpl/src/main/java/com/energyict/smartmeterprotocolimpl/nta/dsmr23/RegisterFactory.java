package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.composedobjects.ComposedRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 13:12:35
 */
public class RegisterFactory implements BulkRegisterProtocol {

    private static final String[] possibleConnectStates = {"Disconnected", "Connected", "Ready for Reconnection"};

    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");
    public static final ObisCode CORE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode MODULE_FIRMWARE = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode CORE_FIRMWARE_SIGNATURE = ObisCode.fromString("1.0.0.2.8.255");
    public static final ObisCode MODULE_FIRMWARE_SIGNATURE = ObisCode.fromString("1.1.0.2.8.255");
    public static final ObisCode DISCONNECT_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    public static final ObisCode CONNECT_CONTROL_MODE = ObisCode.fromString("0.0.96.3.128.255");
    public static final ObisCode CONNECT_CONTROL_STATE = ObisCode.fromString("0.0.96.3.129.255");
    public static final ObisCode CONNECT_CONTROL_BREAKER_STATE = ObisCode.fromString("0.0.96.3.130.255");
    public static final ObisCode ISKRA_MBUS_ENCRYPTION_STATUS = ObisCode.fromString("0.0.97.98.1.255");
    public static final ObisCode GSM_SIGNAL_STRENGTH = ObisCode.fromString("0.0.96.12.5.255");


    private final AbstractSmartNtaProtocol protocol;
    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    private Map<Register, DLMSAttribute> registerMap = new HashMap<Register, DLMSAttribute>();

    public RegisterFactory(final AbstractSmartNtaProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(final Register register) throws IOException {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(final List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, this.protocol.supportsBulkRequests());
        for (Register register : registers) {
            RegisterValue rv = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        rv = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        su.getUnit()));
                    } else {
                        //TODO check if this is the right way of doing
                        throw new NoSuchRegisterException("Register with ObisCode: " + register.getObisCode() + " does not provide a proper Unit.");
                    }

                } else if (this.registerMap.containsKey(register)) {
                    rv = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }
            } catch (IOException e) {
                this.protocol.getLogger().log(Level.WARNING, "Failed to fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]");
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
     * it to the {@link #composedRegisterMap}. Otherwise if it is not a DLMS <CODE>Register</CODE> or <CODE>ExtendedRegister</CODE>, but the ObisCode exists in the
     * ObjectList, then we just add it to the {@link #registerMap}. The handling of the <CODE>registerMap</CODE> should be done by the {@link #readRegisters(java.util.List)}
     * method for each <CODE>ObisCode</CODE> in specific.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    protected ComposedCosemObject constructComposedObjectFromRegisterList(List<Register> registers, boolean supportsBulkRequest) {

        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);

                // check if the registers are directly readable with from the ObjectList
                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, RegisterAttributes.Register_Value.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, RegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, DemandRegisterAttributes.Register_Value.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, DemandRegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        if (rObisCode.equals(ACTIVITY_CALENDAR)) {
                            this.registerMap.put(register, new DLMSAttribute(ACTIVITY_CALENDAR, ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE.getAttributeNumber(), DLMSClassId.ACTIVITY_CALENDAR.getClassId()));
                        } else if (rObisCode.equals(GSM_SIGNAL_STRENGTH)) {
                            this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.Register_Value.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                        } else if (rObisCode.equals(ISKRA_MBUS_ENCRYPTION_STATUS)) {
                            this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, DLMSClassId.DATA.getClassId()));
                        } else {
                            // We get the default 'Value' attribute (2), mostly Data objects
                            this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        }
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } else {
                    if (rObisCode.equals(CONNECT_CONTROL_MODE)) {
                        this.registerMap.put(register, new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.CONTROL_MODE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(CONNECT_CONTROL_STATE)) {
                        this.registerMap.put(register, new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(CONNECT_CONTROL_BREAKER_STATE)) {
                        this.registerMap.put(register, new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.OUTPUT_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else {
                        this.protocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
                    }
                }
            }
            return new ComposedCosemObject(this.protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }

        return null;
    }

    public ObisCode getCorrectedRegisterObisCode(Register register) {
        return this.protocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    //TODO Mbus registers should be added as well

    private RegisterValue convertCustomAbstractObjectsToRegisterValues(Register register, AbstractDataType abstractDataType) throws IOException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (rObisCode.equals(ACTIVITY_CALENDAR)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(((OctetString) abstractDataType).getOctetStr()));
        } else if (rObisCode.equals(CORE_FIRMWARE) || rObisCode.equals(MODULE_FIRMWARE)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.toByteArray()));
        } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE) || rObisCode.equals(MODULE_FIRMWARE_SIGNATURE)) {
            OctetString os = new OctetString(abstractDataType.toByteArray(), 0);
            return new RegisterValue(register, null, null, null, null, new Date(), 0, com.energyict.genericprotocolimpl.common.ParseUtils.decimalByteToString(os.getOctetStr()));
        } else if (rObisCode.equals(CONNECT_CONTROL_MODE)) {
            int mode = ((TypeEnum) abstractDataType).getValue();
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()), null, null, null, new Date(), 0, new String("ConnectControl mode: " + mode));
        } else if (rObisCode.equals(CONNECT_CONTROL_STATE)) {
            int state = ((TypeEnum) abstractDataType).getValue();
            if ((state < 0) || (state > 2)) {
                throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
            }
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()), null, null, null, new Date(), 0, new String("ConnectControl state: " + possibleConnectStates[state]));
        } else if (rObisCode.equals(CONNECT_CONTROL_BREAKER_STATE)) {
            boolean state;
            state = ((BooleanObject) abstractDataType).getState();
            Quantity quantity = new Quantity(state ? "1" : "0", Unit.getUndefined());
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, "State: " + state);
        } else if (rObisCode.equals(ISKRA_MBUS_ENCRYPTION_STATUS)) {
            String text = getEncryptionText(abstractDataType.longValue());
            return new RegisterValue(register, new Quantity(new BigDecimal(abstractDataType.longValue()), Unit.getUndefined()), null, null, null, new Date(), 0, text);
        } else if (rObisCode.equals(GSM_SIGNAL_STRENGTH)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()), null, null, null, new Date(), 0, abstractDataType.longValue() + " dBm");
        } else {
            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
        }
    }

    private static boolean isOctetStringtype(ObisCode obisCode) {
        return CORE_FIRMWARE.equals(obisCode) || MODULE_FIRMWARE.equals(obisCode) || CORE_FIRMWARE_SIGNATURE.equals(obisCode)
                || MODULE_FIRMWARE_SIGNATURE.equals(obisCode) || ACTIVITY_CALENDAR.equals(obisCode) || GSM_SIGNAL_STRENGTH.equals(obisCode);
    }

    /**
     * Convert the received value to a readeable text
     *
     * @param value - the value from the alarm register
     * @return UserFriendly text saying which Mbus decryption failed
     */
    protected String getEncryptionText(long value) {
        StringBuilder strBuilder = new StringBuilder();
        long mask = 134217728;
        for (int i = 0; i < 4; i++) {
            if ((value & mask) == mask) {
                strBuilder.append("Decryption error on Mbus " + (i + 1) + "\r\n");
            }
            mask = mask << 1;
        }
        if ("".equalsIgnoreCase(strBuilder.toString())) {
            return "No encryption errors on Mbus channels";
        } else {
            return strBuilder.toString();
        }
    }
}
