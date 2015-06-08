package com.energyict.protocolimplv2.eict.webrtuz3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.webrtuz3.WebRTUZ3;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/04/2015 - 9:14
 */
public class WebRTUZ3RegisterFactory implements DeviceRegisterSupport {

    public static final ObisCode CORE_FW_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode ACTIVE_TARIFF_REGISTER = ObisCode.fromString("0.0.96.14.0.255");
    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");

    public static final ObisCode MBUS_CLIENT = ObisCode.fromString("0.0.24.1.0.255");
    public static final ObisCode MBUS_CLIENT_STATUS = ObisCode.fromString("0.0.24.1.11.255");
    public static final ObisCode MBUS_CLIENT_ALARM = ObisCode.fromString("0.0.24.1.12.255");

    private final WebRTUZ3 meterProtocol;

    private Map<OfflineRegister, ComposedRegister> composedRegisterMap = new HashMap<>();
    private Map<OfflineRegister, DLMSAttribute> registerMap = new HashMap<>();

    public WebRTUZ3RegisterFactory(WebRTUZ3 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param allRegisters The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     */
    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<OfflineRegister> validRegisters = filterOutAllInvalidRegisters(allRegisters);
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(allRegisters, this.meterProtocol.getDlmsSessionProperties().isBulkRequest());

        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        for (OfflineRegister register : allRegisters) {

            if (!validRegisters.contains(register)) {
                collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
                continue;
            }

            RegisterValue registerValue = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        registerValue = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        su.getEisUnit()));
                    } else { // TODO don't do this, we should throw an exception because we will report incorrect data. (keeping it here for testing with dump meter)
                        registerValue = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        Unit.getUndefined()));
                    }

                } else if (this.registerMap.containsKey(register)) {
                    registerValue = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }

                if (registerValue != null) {
                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } else {
                    collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, meterProtocol.getDlmsSession())) {
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

    protected RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    protected CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
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
            try {
                meterProtocol.getPhysicalAddressFromSerialNumber(register.getSerialNumber());
                validRegisters.add(register);
            } catch (ComServerExecutionException e) {
                meterProtocol.getLogger().severe("Register " + register + " is not supported because MbusDevice '" + register.getSerialNumber() + "' is not installed on the physical device.");
            }
        }
        return validRegisters;
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link OfflineRegister} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
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

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        // We get the default 'Value' attribute (2)
                        this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } else {
                    if (rObisCode.equals(getCorrectedRegisterObisCode(MBUS_CLIENT_STATUS, register.getSerialNumber()))) {
                        this.registerMap.put(register,
                                new DLMSAttribute(
                                        getCorrectedRegisterObisCode(MBUS_CLIENT, register.getSerialNumber()),
                                        MbusClientAttributes.STATUS.getAttributeNumber(),
                                        DLMSClassId.MBUS_CLIENT.getClassId()
                                )
                        );
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(getCorrectedRegisterObisCode(MBUS_CLIENT_ALARM, register.getSerialNumber()))) {
                        this.registerMap.put(register,
                                new DLMSAttribute(
                                        getCorrectedRegisterObisCode(MBUS_CLIENT, register.getSerialNumber()),
                                        MbusClientAttributes.ALARM.getAttributeNumber(),
                                        DLMSClassId.MBUS_CLIENT.getClassId()
                                )
                        );
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else {
                        this.meterProtocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
                    }
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    public ObisCode getCorrectedRegisterObisCode(OfflineRegister register) {
        return this.meterProtocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    public ObisCode getCorrectedRegisterObisCode(ObisCode obisCode, String serialNumber) {
        return this.meterProtocol.getPhysicalAddressCorrectedObisCode(obisCode, serialNumber);
    }

    private RegisterValue convertCustomAbstractObjectsToRegisterValues(OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (isSupportedByProtocol(rObisCode)) {
            if (rObisCode.equals(ACTIVITY_CALENDAR)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()), null, null, null, new Date(), 0, String.valueOf(abstractDataType.longValue()));
            }
        } else if (rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_STATUS) || rObisCode.equalsIgnoreBChannel(MBUS_CLIENT_ALARM)) {
            long value = abstractDataType.longValue();
            return new RegisterValue(register, new Quantity(value, Unit.getUndefined()), null, null, null, new Date(), 0, String.format("0x%02X", value));
        } else {
            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
        }
    }

    private boolean isSupportedByProtocol(ObisCode obisCode) {
        return ERROR_REGISTER.equals(obisCode) || ALARM_REGISTER.equals(obisCode) || ACTIVE_TARIFF_REGISTER.equals(obisCode)
                || CORE_FW_VERSION.equals(obisCode) || ACTIVITY_CALENDAR.equals(obisCode);
    }
}