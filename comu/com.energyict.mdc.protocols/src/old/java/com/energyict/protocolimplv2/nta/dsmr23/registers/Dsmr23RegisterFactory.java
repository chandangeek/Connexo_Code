package com.energyict.protocolimplv2.nta.dsmr23.registers;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
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
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.common.EncryptionStatus;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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
public class Dsmr23RegisterFactory implements DeviceRegisterSupport {

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

    private Map<OfflineRegister, ComposedRegister> composedRegisterMap = new HashMap<>();
    protected Map<OfflineRegister, DLMSAttribute> registerMap = new HashMap<>();
    protected final AbstractDlmsProtocol protocol;
    private final boolean supportsBulkRequests;
    protected final IssueService issueService;
    protected final MdcReadingTypeUtilService readingTypeUtilService;
    private final CollectedDataFactory collectedDataFactory;

    public Dsmr23RegisterFactory(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory, boolean supportsBulkRequests) {
        this.protocol = protocol;
        this.issueService = issueService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.collectedDataFactory = collectedDataFactory;
        this.supportsBulkRequests = supportsBulkRequests;
    }

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper ResultType
     * <b>and</b> {@link com.energyict.mdc.issues.Issue issue} should be returned so proper logging of this action can be performed.
     *
     * @param allRegisters The OfflineRtuRegisters for which to request a value
     * @return a <code>List</code> of collected register values
     */
    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<OfflineRegister> validRegisters = filterOutAllInvalidRegisters(allRegisters);
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(validRegisters, supportsBulkRequests);
        for (OfflineRegister register : allRegisters) {
            if (!validRegisters.contains(register)) {
                collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
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
                        this.protocol.getLogger().log(Level.WARNING, () -> "Register with ObisCode " + register.getObisCode() + "[" + register.getDeviceSerialNumber() + "] does not provide a proper Unit.");
                    }
                } else if (this.registerMap.containsKey(register)) {
                    rv = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }

                if (rv != null) {
                        CollectedRegister deviceRegister = this.collectedDataFactory
                            .createMaximumDemandCollectedRegister(getRegisterIdentifier(register),
                                    register.getReadingType());
                    deviceRegister.setCollectedData(rv.getQuantity(), rv.getText());
                    deviceRegister.setCollectedTimeStamps(
                            getNullSafeInstants(rv.getReadTime()),
                            getNullSafeInstants(rv.getFromTime()),
                            getNullSafeInstants(rv.getToTime()),
                            getNullSafeInstants(rv.getEventTime()));
                    collectedRegisters.add(deviceRegister);
                } else {
                    collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
                    } else {
                        collectedRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage()));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                collectedRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage()));
            }
        }
        return collectedRegisters;
    }

    private Instant getNullSafeInstants(Date date) {
        if(date != null){
            return date.toInstant();
        }
        return null;
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
            if (protocol.getPhysicalAddressFromSerialNumber(register.getDeviceSerialNumber()) != -1) {
                validRegisters.add(register);
            } else {
                protocol.getLogger().severe(() -> "Register " + register + " is not supported because MbusDevice " + register.getDeviceSerialNumber() + " is not installed on the physical device.");
            }
        }
        return validRegisters;
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the Register is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
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
            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
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
                            protocol.getLogger().log(Level.SEVERE, () -> "Register with ObisCode " + rObisCode + " is not supported.");
                        }
                    }
                }
            }
            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }

        return null;
    }

    public ObisCode getCorrectedRegisterObisCode(OfflineRegister register) {
        return this.protocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getDeviceSerialNumber());
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
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.getContentByteArray()));
        } else if (abstractDataType.isNumerical()) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));
        } else {
//            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
            this.issueService.newIssueCollector().addWarning(register, "registerNotSupported", register.getObisCode());
            return null;
        }
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
                strBuilder.append("Decryption error on Mbus ").append(i + 1).append("\r\n");
            }
            mask = mask << 1;
        }
        if ("".equalsIgnoreCase(strBuilder.toString())) {
            return "No encryption errors on Mbus channels";
        } else {
            return strBuilder.toString();
        }
    }

    public RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    public CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register),
                register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_ISSUE,
                            register.getObisCode(),
                            arguments));
        } else {
            collectedRegister.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_NOT_SUPPORTED,
                            register.getObisCode(),
                            arguments));
        }
        return collectedRegister;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }
}
