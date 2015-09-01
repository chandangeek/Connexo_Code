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
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.attributes.ActivityCalendarAttributes;
import com.energyict.dlms.cosem.attributes.ClockAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.common.composedobjects.ComposedActivityCalendar;
import com.energyict.protocolimplv2.common.composedobjects.ComposedClock;
import com.energyict.protocolimplv2.common.composedobjects.ComposedData;
import com.energyict.protocolimplv2.common.composedobjects.ComposedDisconnectControl;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/01/2015 - 11:13
 */
public class AM130RegisterFactory implements DeviceRegisterSupport {

    private static final int BULK_RESQUEST_REGISTER_LIMIT = 5;  //The number of attributes in a bulk request should be smaller than 16. Note that 1, 2 or 3 attributes are read out for every register!

    private static final String ALARM_REGISTER1 = "0.0.97.98.0.255";
    private static final String ALARM_REGISTER2 = "0.0.97.98.1.255";
    private static final String ERROR_REGISTER = "0.0.97.97.0.255";
    private final AM130 am130;

    public AM130RegisterFactory(AM130 am130) {
        this.am130 = am130;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<OfflineRegister> subSet;
        List<CollectedRegister> result = new ArrayList<>();

        result.addAll(readBillingRegisters(offlineRegisters));      // Cause these cannot be read out in bulk
        filterOutAllAllBillingRegistersFromList(offlineRegisters);  // Cause they are already read out (see previous line)
        result.addAll(filterOutAllInvalidMBusRegistersFromList(offlineRegisters)); // For each invalid one, an 'Incompatible' collectedRegister will be added

        int count = 0;
        while ((count * BULK_RESQUEST_REGISTER_LIMIT) <= offlineRegisters.size()) {    //Read out in steps of x registers
            int toIndex = (count + 1) * BULK_RESQUEST_REGISTER_LIMIT;
            subSet = offlineRegisters.subList(count * BULK_RESQUEST_REGISTER_LIMIT, (toIndex < offlineRegisters.size()) ? toIndex : offlineRegisters.size());
            result.addAll(readSubSetOfRegisters(subSet));
            count++;
        }
        return result;
    }

    protected List<CollectedRegister> readSubSetOfRegisters(List<OfflineRegister> registers) {
        //Map of attributes (value, unit, captureTime) per register
        Map<ObisCode, ComposedObject> composedObjectMap = new HashMap<>();

        //List of all attributes that need to be read out
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

        createComposedObjectMap(registers, composedObjectMap, dlmsAttributes);
        ComposedCosemObject composedCosemObject = new ComposedCosemObject(getMeterProtocol().getDlmsSession(), true, dlmsAttributes);
        return createCollectedRegisterListFromComposedCosemObject(registers, composedObjectMap, composedCosemObject);
    }

    protected void createComposedObjectMap(List<OfflineRegister> registers, Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes) {
        for (OfflineRegister register : registers) {
            addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
        }
    }

    protected void addComposedObjectToComposedRegisterMap(Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes, OfflineRegister register) {
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
                composedObject = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterValueAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterUnitAttribute());
                if (((ComposedRegister) composedObject).getRegisterCaptureTime() != null) {
                    dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID());
                DLMSAttribute unitAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                DLMSAttribute captureTimeAttribute = new DLMSAttribute(obisCode, DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());

                composedObject = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterValueAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterUnitAttribute());
                dlmsAttributes.add(((ComposedRegister) composedObject).getRegisterCaptureTime());
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, DataAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedData(valueAttribute);
                dlmsAttributes.add(((ComposedData) composedObject).getDataValueAttribute());
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                DLMSAttribute controlStateAttribute = new DLMSAttribute(obisCode, DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedDisconnectControl(null, controlStateAttribute, null);
                dlmsAttributes.add(((ComposedDisconnectControl) composedObject).getControlStateAttribute());
            } else if (uo.getClassID() == DLMSClassId.CLOCK.getClassId()) {
                DLMSAttribute timeAttribute = new DLMSAttribute(obisCode, ClockAttributes.TIME.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedClock(timeAttribute);
                dlmsAttributes.add(((ComposedClock) composedObject).getTimeAttribute());
            } else if (uo.getClassID() == DLMSClassId.ACTIVITY_CALENDAR.getClassId()) {
                DLMSAttribute timeAttribute = new DLMSAttribute(obisCode, ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE.getAttributeNumber(), uo.getClassID());
                composedObject = new ComposedActivityCalendar(timeAttribute);
                dlmsAttributes.add(((ComposedActivityCalendar) composedObject).getCalendarNameActiveAttribute());
            }
            if (composedObject != null) {
                composedObjectMap.put(obisCode, composedObject);
            }
        }
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
                    ComposedRegister composedRegister = ((ComposedRegister) composedObject);

                    Unit unit = null;
                    if (composedRegister.getRegisterUnitAttribute() != null) {
                        unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                    }
                    Date captureTime = null;
                    if (composedRegister.getRegisterCaptureTime() != null) {
                        AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                        captureTime = captureTimeOctetString.getOctetString().getDateTime(getMeterProtocol().getDlmsSession().getTimeZone()).getValue().getTime();
                    }

                    AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                    RegisterValue registerValue;
                    if (attributeValue.getOctetString() != null) {
                        registerValue = new RegisterValue(offlineRegister, attributeValue.getOctetString().stringValue());
                    } else {
                        registerValue = new RegisterValue(offlineRegister,
                                new Quantity(attributeValue.toBigDecimal(), unit),
                                captureTime
                        );
                    }
                    return createCollectedRegister(registerValue, offlineRegister);
                } else if (composedObject instanceof ComposedData) {
                    ComposedData composedData = (ComposedData) composedObject;
                    AbstractDataType dataValue = composedCosemObject.getAttribute(composedData.getDataValueAttribute());

                    RegisterValue registerValue;
                    if (dataValue.getOctetString() != null) {
                        registerValue = new RegisterValue(offlineRegister, dataValue.getOctetString().stringValue().trim());
                    } else if (dataValue.getBooleanObject() != null) {
                        registerValue = new RegisterValue(offlineRegister, String.valueOf(dataValue.getBooleanObject().getState()));
                    } else {
                        if (offlineRegister.getObisCode().equals(ObisCode.fromString(ALARM_REGISTER1)) || offlineRegister.getObisCode().equals(ObisCode.fromString(ERROR_REGISTER))) {
                            AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(offlineRegister.getObisCode(), dataValue.longValue());
                            registerValue = alarmBitsRegister.getRegisterValue();
                        } else if (offlineRegister.getObisCode().equals(ObisCode.fromString(ALARM_REGISTER2))) {
                            AlarmBitsRegister2 alarmBitsRegister2 = new AlarmBitsRegister2(offlineRegister.getObisCode(), dataValue.longValue());
                            registerValue = alarmBitsRegister2.getRegisterValue();
                        } else {
                            registerValue = new RegisterValue(offlineRegister, new Quantity(dataValue.toBigDecimal(), Unit.get("")));
                        }
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
                if (IOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession())) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                    } else {
                        return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                    }
                } // else a proper connectionCommunicationException is thrown
                return null;
            }
        }
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

    private List<CollectedRegister> filterOutAllInvalidMBusRegistersFromList(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> invalidMBusRegisters = new ArrayList<>();
        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (getMeterProtocol().getPhysicalAddressFromSerialNumber(register.getSerialNumber()) == -1) {
                invalidMBusRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register " + register + " is not supported because MbusDevice " + register.getSerialNumber() + " is not installed on the physical device."));
                it.remove();
            }
        }
        return invalidMBusRegisters;
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

    private CollectedRegister readBillingRegister(OfflineRegister offlineRegister) {
        try {
            HistoricalValue historicalValue = ((AM130) getMeterProtocol()).getStoredValues().getHistoricalValue(offlineRegister.getObisCode());
            RegisterValue registerValue = new RegisterValue(offlineRegister.getObisCode(), historicalValue.getQuantityValue(), historicalValue.getEventTime());
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
        if (IOExceptionHandler.isUnexpectedResponse(e, am130.getDlmsSession())) {
            if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        } else {
            throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, am130.getDlmsSession().getProperties().getRetries() + 1);
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
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            if (errorMessage.length == 0) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupportedBecause", register.getObisCode(), errorMessage[0]));
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