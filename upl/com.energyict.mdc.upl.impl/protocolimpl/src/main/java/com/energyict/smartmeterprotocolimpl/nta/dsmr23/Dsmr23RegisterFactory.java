package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.cbo.BaseUnit;
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
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributes.ActivityCalendarAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.MBusClientAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.generic.EncryptionStatus;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 13:12:35
 */
public class Dsmr23RegisterFactory implements BulkRegisterProtocol {

    private static final String[] possibleConnectStates = {"Disconnected", "Connected", "Ready for Reconnection"};

    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");
    public static final ObisCode ACTIVITY_CALENDAR_NAME = ObisCode.fromString("0.0.13.0.0.2");
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
    public static final ObisCode MbusClientObisCode = ObisCode.fromString("0.x.24.1.0.255");

    // Mbus Registers
    public static final ObisCode MbusEncryptionStatus = ObisCode.fromString("0.x.24.50.0.255");
    public static final ObisCode MbusDisconnectMode = ObisCode.fromString("0.x.24.4.128.255");
    public static final ObisCode MbusDisconnectControlState = ObisCode.fromString("0.x.24.4.129.255");
    public static final ObisCode MbusDisconnectOutputState = ObisCode.fromString("0.x.24.4.130.255");


    //MBUS Client Object attributes
    public static final ObisCode MBUS_CLIENT_IDENTIFICATION_NUMBER = ObisCode.fromString("0.x.24.1.6.255");
    public static final ObisCode MBUS_CLIENT_MANUFACTURER_ID = ObisCode.fromString("0.x.24.1.7.255");
    public static final ObisCode MBUS_CLIENT_VERSION = ObisCode.fromString("0.x.24.1.8.255");
    public static final ObisCode MBUS_CLIENT_DEVICE_TYPE = ObisCode.fromString("0.x.24.1.9.255");

    protected final AbstractSmartNtaProtocol protocol;
    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    protected Map<Register, DLMSAttribute> registerMap = new HashMap<Register, DLMSAttribute>();

    public Dsmr23RegisterFactory(final AbstractSmartNtaProtocol protocol) {
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
        return null;
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        registers = filterOutAllInvalidRegisters(registers);
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, this.protocol.supportsBulkRequests());
        for (Register register : registers) {
            RegisterValue rv = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = readUnit(registerComposedCosemObject, register);
                    if (su.getUnitCode() != 0) {
                        Date eventTime = null;   //Optional capture time attribute
                        DLMSAttribute registerCaptureTime = this.composedRegisterMap.get(register).getRegisterCaptureTime();
                        if (registerCaptureTime != null) {
                            eventTime = readEventTime(registerComposedCosemObject, registerCaptureTime);
                        }
                        rv = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        su.getEisUnit()), eventTime);
                    } else {
                        this.protocol.getLogger().log(Level.WARNING, "Register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "] does not provide a proper Unit.");
                    }

                } else if (this.registerMap.containsKey(register)) {
                    rv = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }
            } catch (IOException e) {
                this.protocol.getLogger().log(Level.WARNING, "Failed to fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]" + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                this.protocol.getLogger().log(Level.SEVERE, "Parsing error while fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]");
            }
            if (rv != null) {
                registerValues.add(rv);
            }
        }
        return registerValues;
    }

    protected Date readEventTime(ComposedCosemObject registerComposedCosemObject, DLMSAttribute registerCaptureTime) throws IOException {
        AbstractDataType attribute = registerComposedCosemObject.getAttribute(registerCaptureTime);
        return attribute.getOctetString().getDateTime(protocol.getDlmsSession().getTimeZone()).getValue().getTime();
    }

    protected ScalerUnit readUnit(ComposedCosemObject registerComposedCosemObject, Register register) throws IOException {
        return new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
    }

    /**
     * From the given list of registers, filter out all invalid ones. <br></br>
     * A register is invalid if the physical address of the device, owning the register, could not be fetched.
     * This is the case, when an Mbus device is linked to a master in EIMaster, but in reality not physically connected. <br></br>
     * E.g.: This is the case when the Mbus device in the field has been swapped for a new one, but without changing/correcting the EIMaster configuration.
     *
     * @param registers    the complete list of registers
     * @return  the validated list containing all valid registers
     */
    protected List<Register> filterOutAllInvalidRegisters(List<Register> registers) {
        List<Register> validRegisters = new ArrayList<Register>();

        for (Register register : registers) {
            if (this.protocol.getPhysicalAddressFromSerialNumber(register.getSerialNumber()) != -1) {
                validRegisters.add(register);
            } else {
                this.protocol.getLogger().severe("Register " + register.getObisCode() + " is not supported because MbusDevice '" + register.getSerialNumber() + "' is not installed on the physical device.");
            }
        }
        return validRegisters;
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

                // check if the registers are directly readable from the ObjectList
                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
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
                        this.composedRegisterMap.put(register, composedRegister);
                    } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        if (ACTIVITY_CALENDAR.equals(rObisCode) || ACTIVITY_CALENDAR_NAME.equals(rObisCode)) {
                            this.registerMap.put(register, new DLMSAttribute(ACTIVITY_CALENDAR, ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE.getAttributeNumber(), DLMSClassId.ACTIVITY_CALENDAR.getClassId()));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else if (rObisCode.equals(GSM_SIGNAL_STRENGTH)) {
                            this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else if (rObisCode.equals(ISKRA_MBUS_ENCRYPTION_STATUS)) {
                            this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, DLMSClassId.DATA.getClassId()));
                            dlmsAttributes.add(this.registerMap.get(register));

                            // We can't add this for the SecuritySetup, MBus client setup attributes or the AssociationLN object, the DSMR4.0 uses these
                        } else if (!(uo.getObisCode().equalsIgnoreBChannel(MbusClientObisCode) || uo.getObisCode().equals(SecuritySetup.getDefaultObisCode()) || (uo.getObisCode().equals(AssociationLN.getDefaultObisCode())))) {
                            // We get the default 'Value' attribute (2), mostly Data objects
                            this.registerMap.put(register, new DLMSAttribute(uo.getObisCode(), DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                            dlmsAttributes.add(this.registerMap.get(register));
                        }
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
                    } else if (rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus)) {
                        this.registerMap.put(register, new DLMSAttribute(MbusEncryptionStatus, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectMode)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), DisconnectControlAttribute.CONTROL_MODE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectControlState)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectOutputState)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), DisconnectControlAttribute.OUTPUT_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE)) {
                        this.registerMap.put(register, new DLMSAttribute(CORE_FIRMWARE_SIGNATURE, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_IDENTIFICATION_NUMBER)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.IDENTIFICATION_NUMBER.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_MANUFACTURER_ID)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.MANUFACTURER_ID.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_VERSION)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.VERSION.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_DEVICE_TYPE)) {
                        this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.DEVICE_TYPE.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }  else {
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

    /**
     * The given obisCode is not a valid one. We use it to make a distiction between two arguments of the same object.
     * This function will return the original obisCode from the Disconnector object, without the E-value
     *
     * @param oc -  the manipulated ObisCode of the Disconnector object
     * @return the original ObisCode of the Disconnector object
     */
    private ObisCode adjustToMbusOC(ObisCode oc) {
        return new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), 0, oc.getF());
    }

    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(Register register, AbstractDataType abstractDataType) throws IOException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (rObisCode.equals(ACTIVITY_CALENDAR)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(((OctetString) abstractDataType).getOctetStr()));
        } else if (rObisCode.equals(CORE_FIRMWARE) || rObisCode.equals(MODULE_FIRMWARE)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.getContentByteArray()));
        } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE) || rObisCode.equals(MODULE_FIRMWARE_SIGNATURE)) {
            OctetString os = OctetString.fromByteArray(abstractDataType.getContentByteArray());
            return new RegisterValue(register, null, null, null, null, new Date(), 0, ParseUtils.decimalByteToString(os.getOctetStr()).toUpperCase());
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
        } else if (rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus)) {
            long encryptionValue = abstractDataType.longValue();
            Quantity quantity = new Quantity(BigDecimal.valueOf(encryptionValue), Unit.getUndefined());
            String text = EncryptionStatus.forValue((int) encryptionValue).getLabelKey();
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, text);
        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectMode)) {
            int mode = ((TypeEnum) abstractDataType).getValue();
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()), null, null, null, new Date(), 0, new String("ConnectControl mode: " + mode));
        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectControlState)) {
            int state = ((TypeEnum) abstractDataType).getValue();
            if ((state < 0) || (state > 2)) {
                throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
            }
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()), null, null, null, new Date(), 0, new String("ConnectControl state: " + possibleConnectStates[state]));
        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectOutputState)) {
            boolean state;
            state = ((BooleanObject) abstractDataType).getState();
            Quantity quantity = new Quantity(state ? "1" : "0", Unit.getUndefined());
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, "State: " + state);
        }  else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_IDENTIFICATION_NUMBER)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(),  Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, new String("Client identification number: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_MANUFACTURER_ID)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(),  Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, new String("Manufacturer Id: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_VERSION)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(),  Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, new String("Client version: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_DEVICE_TYPE)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(),  Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, new String("Client device type: " + Long.toString(abstractDataType.longValue())));
        } else if (abstractDataType.isOctetString()) {
            String text;
            if (octetStringPrintableAsString((OctetString) abstractDataType)) {
                text = new String(abstractDataType.getContentByteArray());
            } else {
                text = new String("HEX: " + ProtocolTools.getHexStringFromBytes(abstractDataType.getContentByteArray(), ""));
            }
            return new RegisterValue(register, null, null, null, null, new Date(), 0, text);
        } else if (abstractDataType.isTypeEnum()) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));
        } else if (abstractDataType.isNumerical()) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));
        } else {
            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
        }
    }

    /**
     * Tests if the content ByteArray of this OctetString contains only printable ASCII characters
     *
     * @param octetString the OctetString to test
     * @return true if the OctetString contains only printable ASCII characters
     *         false if the OctetString contains non-printable characters
     */
    private boolean octetStringPrintableAsString(OctetString octetString) {
        for (byte b : octetString.getContentByteArray()) {
            if(b < 31 || b > 127) {
                return false;
            }
        }
        return true;
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

    public AbstractSmartNtaProtocol getProtocol() {
        return protocol;
    }
}
