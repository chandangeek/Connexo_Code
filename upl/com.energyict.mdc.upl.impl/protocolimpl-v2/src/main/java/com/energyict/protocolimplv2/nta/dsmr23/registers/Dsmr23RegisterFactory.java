package com.energyict.protocolimplv2.nta.dsmr23.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributes.*;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.common.EncryptionStatus;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 13:12:35
 */
public class Dsmr23RegisterFactory implements DeviceRegisterSupport {

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
    private static final String[] possibleConnectStates = {"Disconnected", "Connected", "Ready for Reconnection"};
    protected final AbstractDlmsProtocol protocol;
    protected Map<OfflineRegister, DLMSAttribute> registerMap = new HashMap<>();
    private Map<OfflineRegister, ComposedRegister> composedRegisterMap = new HashMap<>();

    public Dsmr23RegisterFactory(final AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper {@link com.energyict.mdc.meterdata.ResultType resultType}
     * <b>and</b> {@link com.energyict.mdc.issues.Issue issue} should be returned so proper logging of this action can be performed.
     *
     * @param allRegisters The OfflineRtuRegisters for which to request a value
     * @return a <code>List</code> of collected register values
     */
    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<OfflineRegister> validRegisters = filterOutAllInvalidRegisters(allRegisters);
        List<CollectedRegister> collectedRegisters = new ArrayList<CollectedRegister>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(validRegisters, protocol.getDlmsSessionProperties().isBulkRequest());
        for (OfflineRegister register : allRegisters) {
            if (!validRegisters.contains(register)) {
                collectedRegisters.add(createUnsupportedRegister(register));
                continue;
            }
            RegisterValue rv = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        Date eventTime = null;   //Optional capture time attribute
                        DLMSAttribute registerCaptureTime = this.composedRegisterMap.get(register).getRegisterCaptureTime();
                        if (registerCaptureTime != null) {
                            AbstractDataType attribute = registerComposedCosemObject.getAttribute(registerCaptureTime);
                            eventTime = attribute.getOctetString().getDateTime(protocol.getDlmsSession().getTimeZone()).getValue().getTime();
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

                if (rv != null) {
                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(rv.getQuantity(), rv.getText());
                    deviceRegister.setCollectedTimeStamps(rv.getReadTime(), rv.getFromTime(), rv.getToTime(), rv.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } else {
                    collectedRegisters.add(createUnsupportedRegister(register));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        collectedRegisters.add(createUnsupportedRegister(register));
                    } else {
                        collectedRegisters.add(createIncompatibleRegister(register, e.getMessage()));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                collectedRegisters.add(createIncompatibleRegister(register, e.getMessage()));
            }
        }
        return collectedRegisters;
    }

    /**
     * From the given list of registers, filter out all invalid ones. <br></br>
     * A register is invalid if the physical address of the device, owning the register, could not be fetched.
     * This is the case, when an Mbus device is linked to a master in EIMaster, but in reality not physically connected. <br></br>
     * E.g.: This is the case when the Mbus device in the field has been swapped for a new one, but without changing/correcting the EIMaster configuration.
     *
     * @param registers the complete list of registers
     * @return the validated list containing all valid registers
     */
    protected List<OfflineRegister> filterOutAllInvalidRegisters(List<OfflineRegister> registers) {
        List<OfflineRegister> validRegisters = new ArrayList<>();

        for (OfflineRegister register : registers) {
            if (protocol.getPhysicalAddressFromSerialNumber(register.getSerialNumber()) != -1) {
                validRegisters.add(register);
            } else {
                protocol.getLogger().severe("Register " + register + " is not supported because MbusDevice " + register.getSerialNumber() + " is not installed on the physical device.");
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
    protected ComposedCosemObject constructComposedObjectFromRegisterList(List<OfflineRegister> registers, boolean supportsBulkRequest) {

        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (OfflineRegister register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);

                if (rObisCode != null) {
                    // check if the registers are directly readable from the ObjectList
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
                            this.registerMap.put(register, new DLMSAttribute(adjustToMbusDisconnectOC(rObisCode), DisconnectControlAttribute.CONTROL_MODE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectControlState)) {
                            this.registerMap.put(register, new DLMSAttribute(adjustToMbusDisconnectOC(rObisCode), DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectOutputState)) {
                            this.registerMap.put(register, new DLMSAttribute(adjustToMbusDisconnectOC(rObisCode), DisconnectControlAttribute.OUTPUT_STATE.getAttributeNumber(), DLMSClassId.DISCONNECT_CONTROL));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE)) {
                            this.registerMap.put(register, new DLMSAttribute(CORE_FIRMWARE_SIGNATURE, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                            dlmsAttributes.add(this.registerMap.get(register));
                        } else {
                            protocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
                        }
                    }
                }
            }
            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }

        return null;
    }

    public ObisCode getCorrectedRegisterObisCode(OfflineRegister register) {
        return this.protocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    /**
     * The given obisCode is not a valid one. We use it to make a distinction between two arguments of the same object.
     * This function will return the original obisCode from the Disconnector object, without the E-value
     *
     * @param oc -  the manipulated ObisCode of the Disconnector object
     * @return the original ObisCode of the Disconnector object
     */
    private ObisCode adjustToMbusDisconnectOC(ObisCode oc) {
        return new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), 0, oc.getF());
    }

    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
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
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()), null, null, null, new Date(), 0, "ConnectControl mode: " + mode);
        } else if (rObisCode.equals(CONNECT_CONTROL_STATE)) {
            int state = ((TypeEnum) abstractDataType).getValue();
            if ((state < 0) || (state > 2)) {
                throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
            }
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()), null, null, null, new Date(), 0, "ConnectControl state: " + possibleConnectStates[state]);
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
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()), null, null, null, new Date(), 0, "ConnectControl mode: " + mode);
        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectControlState)) {
            int state = ((TypeEnum) abstractDataType).getValue();
            if ((state < 0) || (state > 2)) {
                throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
            }
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()), null, null, null, new Date(), 0, "ConnectControl state: " + possibleConnectStates[state]);
        } else if (rObisCode.equalsIgnoreBChannel(MbusDisconnectOutputState)) {
            boolean state;
            state = ((BooleanObject) abstractDataType).getState();
            Quantity quantity = new Quantity(state ? "1" : "0", Unit.getUndefined());
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, "State: " + state);
        } else if (abstractDataType.isOctetString()) {
            String text;
            if (octetStringPrintableAsString((OctetString) abstractDataType)) {
                text = new String(abstractDataType.getContentByteArray());
            } else {
                text = new String("HEX: " + ProtocolTools.getHexStringFromBytes(abstractDataType.getContentByteArray(), ""));
            }
            return new RegisterValue(register, null, null, null, null, new Date(), 0, text);
        } else if (abstractDataType.isNumerical()) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));
        } else if (abstractDataType.isTypeEnum()) {
            return new RegisterValue(register, new Quantity(abstractDataType.getTypeEnum().getValue(), Unit.getUndefined()));
        } else {
            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
        }
    }

    /**
     * Tests if the content ByteArray of this OctetString contains only printable ASCII characters
     *
     * @param octetString the OctetString to test
     * @return true if the OctetString contains only printable ASCII characters
     * false if the OctetString contains non-printable characters
     */
    private boolean octetStringPrintableAsString(OctetString octetString) {
        for (byte b : octetString.getContentByteArray()) {
            if (b < 31 || b > 127) {
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

    public AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    protected RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    protected CollectedRegister createUnsupportedRegister(OfflineRegister register) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        return collectedRegister;
    }

    protected CollectedRegister createIncompatibleRegister(OfflineRegister register, String errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage));
        return collectedRegister;
    }
}
