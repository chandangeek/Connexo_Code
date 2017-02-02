package com.energyict.protocolimplv2.dlms.idis.am130.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.attributes.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.common.composedobjects.*;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/01/2015 - 11:13
 */
public class AM130RegisterFactory implements DeviceRegisterSupport {

    protected static final int BULK_REQUEST_ATTRIBUTE_LIMIT = 16;  //The number of attributes in a bulk request should be smaller than 16.

    private static final String ALARM_REGISTER1 = "0.0.97.98.0.255";
    private static final String ALARM_REGISTER2 = "0.0.97.98.1.255";
    private static final String ERROR_REGISTER = "0.0.97.97.0.255";
    private static final String ACTIVE_FIRMWARE_SIGNATURE = "1.0.0.2.8.255";
    private static final String ACTIVE_FIRMWARE_SIGNATURE_1 = "1.1.0.2.8.255";
    private static final String ACTIVE_FIRMWARE_SIGNATURE_2 = "1.2.0.2.8.255";

    private final AM130 am130;

    public AM130RegisterFactory(AM130 am130) {
        this.am130 = am130;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<OfflineRegister> subSet;
        List<CollectedRegister> result = new ArrayList<>();

        result.addAll(readBillingRegisters(offlineRegisters));      // Cause these cannot be read out in bulk
        filterOutAllAllBillingRegistersFromList(offlineRegisters);  // Cause they are already read out (see previous line)
        result.addAll(filterOutAllInvalidRegistersFromList(offlineRegisters)); // For each invalid one, an 'Incompatible' collectedRegister will be added

        int from = 0;
        while (from < offlineRegisters.size()) {    //Read out in steps of x registers
            subSet = offlineRegisters.subList(from, offlineRegisters.size());
            List<CollectedRegister> collectedRegisters = readSubSetOfRegisters(subSet);
            from += collectedRegisters.size();
            result.addAll(collectedRegisters);
        }
        return result;
    }

    /**
     * Read out a subset of registers.
     * The limit of the meter is 16 attributes per bulk request.
     * Note that the number of requested registers in the bulk request is dynamic.
     */
    protected List<CollectedRegister> readSubSetOfRegisters(List<OfflineRegister> registers) {
        //Map of attributes (value, unit, captureTime) per register
        Map<ObisCode, ComposedObject> composedObjectMap = new HashMap<>();

        //List of all attributes that need to be read out
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

        int count = createComposedObjectMap(registers, composedObjectMap, dlmsAttributes);
        ComposedCosemObject composedCosemObject = new ComposedCosemObject(getMeterProtocol().getDlmsSession(), getMeterProtocol().getDlmsSessionProperties().isBulkRequest(), dlmsAttributes);
        return createCollectedRegisterListFromComposedCosemObject(registers.subList(0, count), composedObjectMap, composedCosemObject);
    }

    /**
     * Create a map of ComposedObjects for as much registers as possible.
     * Return the number of registers from the given list that will be read out.
     */
    protected int createComposedObjectMap(List<OfflineRegister> registers, Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes) {
        int count = 0;

        for (OfflineRegister register : registers) {
            if (dlmsAttributes.size() < BULK_REQUEST_ATTRIBUTE_LIMIT) {
                Boolean result = addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
                if (result != null) {
                    count++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Return true if attribute(s) were added to the list, to read out the given register.
     * Return false if the register is not supported by the protocol implementation or the meter.
     * Return null if no attribute(s) were added to the list, because the attribute(s) for the given register could no longer be added to the bulk request (meter limit of 16)
     */
    protected Boolean addComposedObjectToComposedRegisterMap(Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes, OfflineRegister register) {
        ComposedObject composedObject = null;

        ObisCode obisCode = register.getObisCode();
        if (isMBusValueChannel(register.getObisCode())) {
            obisCode = getMeterProtocol().getPhysicalAddressCorrectedObisCode(obisCode, register.getSerialNumber());
        }

        final UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(getMeterProtocol().getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
        if (uo != null) {
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                DLMSAttribute unitAttribute = new DLMSAttribute(obisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                DLMSAttribute captureTimeAttribute = null;  //Optional attribute
                if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                    captureTimeAttribute = new DLMSAttribute(obisCode, ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());
                }
                if ((dlmsAttributes.size() + (captureTimeAttribute == null ? 2 : 3)) > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                composedObject = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterValueAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterUnitAttribute());
                if (((ComposedRegister) composedObject).getRegisterCaptureTime() != null) {
                    dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                if (dlmsAttributes.size() + 3 > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID());
                DLMSAttribute unitAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                DLMSAttribute captureTimeAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());

                composedObject = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterValueAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterUnitAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterCaptureTime());
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                if (dlmsAttributes.size() + 1 > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, DataAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedData(valueAttribute);
                dlmsAttributes.add(((ComposedData) composedObject).getDataValueAttribute());
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                if (dlmsAttributes.size() + 1 > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                DLMSAttribute controlStateAttribute = new DLMSAttribute(obisCode, DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedDisconnectControl(null, controlStateAttribute, null);
                dlmsAttributes.add(((ComposedDisconnectControl) composedObject).getControlStateAttribute());
            } else if (uo.getClassID() == DLMSClassId.CLOCK.getClassId()) {
                if (dlmsAttributes.size() + 1 > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                DLMSAttribute timeAttribute = new DLMSAttribute(obisCode, ClockAttributes.TIME.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedClock(timeAttribute);
                dlmsAttributes.add(((ComposedClock) composedObject).getTimeAttribute());
            } else if (uo.getClassID() == DLMSClassId.ACTIVITY_CALENDAR.getClassId()) {
                if (dlmsAttributes.size() + 1 > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                    return null; //Don't add the new attributes, no more room
                }

                DLMSAttribute timeAttribute = new DLMSAttribute(obisCode, ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedActivityCalendar(timeAttribute);
                dlmsAttributes.add(((ComposedActivityCalendar) composedObject).getCalendarNameActiveAttribute());
            }
            if (composedObject != null) {
                composedObjectMap.put(obisCode, composedObject);
                return true;
            }
        }
        return false;
    }

    protected List<CollectedRegister> createCollectedRegisterListFromComposedCosemObject(List<OfflineRegister> registers, Map<ObisCode, ComposedObject> composedObjectMap, ComposedCosemObject composedCosemObject) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        for (OfflineRegister offlineRegister : registers) {
            collectedRegisters.add(createCollectedRegisterFor(offlineRegister, composedObjectMap, composedCosemObject));
        }
        return collectedRegisters;
    }

    protected CollectedRegister createCollectedRegisterFor(OfflineRegister offlineRegister, Map<ObisCode, ComposedObject> composedObjectMap, ComposedCosemObject composedCosemObject) {
        ObisCode obisCode = offlineRegister.getObisCode();
        if (isMBusValueChannel(offlineRegister.getObisCode())) {
            obisCode = getMeterProtocol().getPhysicalAddressCorrectedObisCode(obisCode, offlineRegister.getSerialNumber());
        }
        ComposedObject composedObject = composedObjectMap.get(obisCode);
        if (composedObject == null) {
            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
        } else {
            try {
                if (composedObject instanceof ComposedRegister) {
                    return getCollectedRegisterForComposedObject(composedObject, offlineRegister, composedCosemObject);
                } else if (composedObject instanceof ComposedData) {
                    ComposedData composedData = (ComposedData) composedObject;
                    AbstractDataType dataValue = composedCosemObject.getAttribute(composedData.getDataValueAttribute());

                    RegisterValue registerValue;
                    if (dataValue.getOctetString() != null) {
                        registerValue = containsActiveFirmwareSignature(composedData)
                                ? new RegisterValue(offlineRegister, "0x" + ProtocolTools.getHexStringFromBytes(dataValue.getOctetString().getOctetStr(), ""))
                                : new RegisterValue(offlineRegister, dataValue.getOctetString().stringValue().trim());
                    } else if (dataValue.getBooleanObject() != null) {
                        registerValue = new RegisterValue(offlineRegister, String.valueOf(dataValue.getBooleanObject().getState()));
                    } else if (dataValue.getTypeEnum() != null) {
                        registerValue = new RegisterValue(offlineRegister, new Quantity(dataValue.getTypeEnum().getValue(), Unit.getUndefined()));
                    } else {
                        registerValue = getRegisterValueForAlarms(offlineRegister, dataValue);
                    }
                    return createCollectedRegister(registerValue, offlineRegister);
                } else if (composedObject instanceof ComposedDisconnectControl) {
                    ComposedDisconnectControl composedDisconnectControl = (ComposedDisconnectControl) composedObject;
                    AbstractDataType controlState = composedCosemObject.getAttribute(composedDisconnectControl.getControlStateAttribute());

                    RegisterValue registerValue = new RegisterValue(offlineRegister, DisconnectControlState.fromState(controlState.intValue()).name());
                    registerValue.setQuantity(new Quantity(controlState.intValue(), Unit.get(BaseUnit.UNITLESS)));
                    return createCollectedRegister(registerValue, offlineRegister);
                } else if (composedObject instanceof ComposedClock) {
                    ComposedClock composedClock = (ComposedClock) composedObject;
                    AbstractDataType timeAttribute = composedCosemObject.getAttribute(composedClock.getTimeAttribute());
                    Calendar calendar = new AXDRDateTime(timeAttribute.getOctetString(), AXDRDateTimeDeviationType.Negative).getValue();

                    RegisterValue registerValue = new RegisterValue(offlineRegister, calendar.getTime());
                    registerValue.setQuantity(new Quantity(calendar.getTimeInMillis() / 1000, Unit.get(BaseUnit.SECOND)));
                    return createCollectedRegister(registerValue, offlineRegister);
                } else if (composedObject instanceof ComposedActivityCalendar) {
                    ComposedActivityCalendar composedActivityCalendar = (ComposedActivityCalendar) composedObject;
                    AbstractDataType calendarNameValue = composedCosemObject.getAttribute(composedActivityCalendar.getCalendarNameActiveAttribute());
                    RegisterValue registerValue = new RegisterValue(offlineRegister, calendarNameValue.getOctetString().stringValue().trim());
                    return createCollectedRegister(registerValue, offlineRegister);
                } else {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Encountered unexpected ComposedObject - data cannot be parsed."); // Should never occur
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession().getProperties().getRetries())) {
                    if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                    } else {
                        return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                    }
                } // else a proper connectionCommunicationException is thrown
                return null;
            }
        }
    }

    protected CollectedRegister getCollectedRegisterForComposedObject(ComposedObject composedObject, OfflineRegister offlineRegister, ComposedCosemObject composedCosemObject) throws IOException {
        ComposedRegister composedRegister = ((ComposedRegister) composedObject);
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        Issue scalerUnitIssue = null;
        if (composedRegister.getRegisterUnitAttribute() != null &&
                composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute()).getStructure().getDataType(1) != null) {
            ScalerUnit scalerUnit = null;
            try {
                scalerUnit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute()));
                unit = scalerUnit.getEisUnit();
            } catch (Exception e) {
                scalerUnitIssue =  MdcManager.getIssueFactory().createProblem(offlineRegister.getObisCode(), "registerXissue", offlineRegister.getObisCode(),
                        "Unable to resolve the unit code value: " + scalerUnit.getUnitCode());
            }
        }
        Date captureTime = null;
        Issue timeZoneIssue = null;
        if (composedRegister.getRegisterCaptureTime() != null) {
            AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
            TimeZone configuredTimeZone = getMeterProtocol().getDlmsSession().getTimeZone();
            DateTime dlmsDateTime = captureTimeOctetString.getOctetString().getDateTime(configuredTimeZone);
            int configuredTimeZoneOffset = configuredTimeZone.getRawOffset()/(-1*60*1000);
            if (dlmsDateTime.getDeviation() != configuredTimeZoneOffset){
                timeZoneIssue = MdcManager.getIssueFactory().createWarning(offlineRegister.getObisCode(), "registerXissue", offlineRegister.getObisCode(),
                        "Capture time zone offset ["+dlmsDateTime.getDeviation()+"] differs from the configured time zone ["+configuredTimeZone.getDisplayName()+"] = ["+configuredTimeZoneOffset+"]");
            }
            captureTime = dlmsDateTime.getValue().getTime();
        }

        AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
        RegisterValue registerValue;
        if (attributeValue.getOctetString() != null) {
            registerValue = new RegisterValue(offlineRegister, attributeValue.getOctetString().stringValue());
        } else {
            // let each sub-protocol to create it's own flavour of time-stamp combination
            // AM540 when reading mirror will change this
            registerValue = getRegisterValueForComposedRegister(offlineRegister, captureTime, attributeValue, unit );
        }

        CollectedRegister collectedRegister = createCollectedRegister(registerValue, offlineRegister);
        if (timeZoneIssue!=null) {
            collectedRegister.setFailureInformation(ResultType.ConfigurationError, timeZoneIssue);
        }
        if(scalerUnitIssue != null) {
            collectedRegister.setFailureInformation(ResultType.DataIncomplete, scalerUnitIssue);
        }

        return collectedRegister;
    }

    /**
     * Will create a register value for a composed register which provides the capture time
     * By default will set readTime = collection time eventTime=captureTime
     *
     * This is overridden in AM540 to handle mirror devices!
     *
     */
    protected RegisterValue getRegisterValueForComposedRegister(OfflineRegister offlineRegister, Date captureTime, AbstractDataType attributeValue, Unit unit) {
        return  new RegisterValue(offlineRegister,
                new Quantity(attributeValue.toBigDecimal(), unit),
                captureTime // eventTime
        );
    }

    protected RegisterValue getRegisterValueForAlarms(OfflineRegister offlineRegister, AbstractDataType dataValue) {
        RegisterValue registerValue;
        if (offlineRegister.getObisCode().equals(ObisCode.fromString(ALARM_REGISTER1)) || offlineRegister.getObisCode().equals(ObisCode.fromString(ERROR_REGISTER))) {
            AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(offlineRegister.getObisCode(), dataValue.longValue());
            registerValue = alarmBitsRegister.getRegisterValue();
        } else if (offlineRegister.getObisCode().equals(ObisCode.fromString(ALARM_REGISTER2))) {
            AlarmBitsRegister2 alarmBitsRegister2 = new AlarmBitsRegister2(offlineRegister.getObisCode(), dataValue.longValue());
            registerValue = alarmBitsRegister2.getRegisterValue();
        } else {
            registerValue = new RegisterValue(offlineRegister, new Quantity(dataValue.toBigDecimal(), Unit.getUndefined()));
        }
        return registerValue;
    }

    private boolean containsActiveFirmwareSignature(ComposedData composedData) {
        return composedData.getDataValueAttribute().getObisCode().equals(ObisCode.fromString(ACTIVE_FIRMWARE_SIGNATURE)) ||
                composedData.getDataValueAttribute().getObisCode().equals(ObisCode.fromString(ACTIVE_FIRMWARE_SIGNATURE_1)) ||
                composedData.getDataValueAttribute().getObisCode().equals(ObisCode.fromString(ACTIVE_FIRMWARE_SIGNATURE_2));
    }

    private void filterOutAllAllBillingRegistersFromList(List<OfflineRegister> offlineRegisters) {
        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (register.getObisCode().getF() != 255) {
                it.remove();
            }
        }
    }

    /**
     * Filter out the following registers:
     * - MBus devices (by serial number) that are not installed on the e-meter
     */
    protected List<CollectedRegister> filterOutAllInvalidRegistersFromList(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> invalidRegisters = new ArrayList<>();
        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (getMeterProtocol().getPhysicalAddressFromSerialNumber(register.getSerialNumber()) == -1) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register " + register + " is not supported because MbusDevice " + register.getSerialNumber() + " is not installed on the physical device."));
                it.remove();
            }
        }
        return invalidRegisters;
    }

    private List<CollectedRegister> readBillingRegisters(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> collectedBillingRegisters = new ArrayList<>();
        for (OfflineRegister offlineRegister : offlineRegisters) {
            if (offlineRegister.getObisCode().getF() != 255) {
                collectedBillingRegisters.add(readBillingRegister(offlineRegister));
            }
        }
        return collectedBillingRegisters;
    }

    protected CollectedRegister readBillingRegister(OfflineRegister offlineRegister) {
        try {
            HistoricalValue historicalValue = ((AM130) getMeterProtocol()).getStoredValues().getHistoricalValue(offlineRegister.getObisCode());
            RegisterValue registerValue = new RegisterValue(
                                                    offlineRegister.getObisCode(),
                                                    historicalValue.getQuantityValue(),
                                                    null, // event time
                                                    null, // from time
                                                    historicalValue.getBillingDate(), // to time
                                                    historicalValue.getCaptureTime(),  // read time
                                                    0,
                                                    null);

            return createCollectedRegister(registerValue, offlineRegister);
        } catch (NoSuchRegisterException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported, e.getMessage());
        } catch (NotInObjectListException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        } catch (IOException e) {
            return handleIOException(offlineRegister, e);
        }
    }

    protected CollectedRegister handleIOException(OfflineRegister offlineRegister, IOException e) {
        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, am130.getDlmsSession().getProperties().getRetries())) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        } else {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, am130.getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    private boolean isMBusValueChannel(ObisCode obisCode) {
        return ((obisCode.getA() == 0) && (obisCode.getC() == 24) && (obisCode.getD() == 2) && (obisCode.getE() > 0 && obisCode.getE() < 5) && obisCode.getF() == 255);
    }

    protected RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    protected CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            if (errorMessage.length == 0) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupportedBecause", register.getObisCode(), errorMessage[0]));
            }
        }
        return collectedRegister;
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    public AbstractDlmsProtocol getMeterProtocol() {
        return am130;
    }

    private enum DisconnectControlState {
        Unknown(-1),
        Disconnected(0),
        Connected(1),
        Ready_for_reconnection(2);

        private final int state;

        DisconnectControlState(int state) {
            this.state = state;
        }

        public static DisconnectControlState fromState(int state) {
            for (DisconnectControlState disconnectControlState : values()) {
                if (state == disconnectControlState.state) {
                    return disconnectControlState;
                }
            }
            return Unknown;
        }
    }
}