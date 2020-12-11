package com.energyict.protocolimplv2.dlms.a2.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.GPRSStandardStatusAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.a2.A2;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.*;

public class A2RegisterFactory implements DeviceRegisterSupport {

    private final static ObisCode INSTALLER_MAITAINER_SETUP = ObisCode.fromString("0.0.94.39.30.255");
    private final static ObisCode GLOBAL_FRAMECOUNTER_THRESHOLDS = ObisCode.fromString("0.0.94.39.33.255");
    private final static ObisCode USER_MESSAGE = ObisCode.fromString("0.0.94.39.46.255");
    private final static ObisCode ON_DEMAND_SNAPSHOT_TIME = ObisCode.fromString("0.0.94.39.8.255");
    private final static ObisCode LEAKAGE_TEST_PARAMETERS = ObisCode.fromString("0.0.94.39.26.255");
    private final static ObisCode SYNCHRONIZATION_REGISTERS = ObisCode.fromString("0.0.94.39.20.255");
    private final static ObisCode BATERY_CHANGE_AUTHORIZATION = ObisCode.fromString("0.0.94.39.14.255");
    private final static ObisCode VALVE_CONFIGURATION_PGV = ObisCode.fromString("0.0.94.39.3.255");
    private final static ObisCode VALVE_CLOSURE_CAUSE = ObisCode.fromString("0.0.94.39.7.255");
    private final static ObisCode UNITS_DEVICE_STATUS = ObisCode.fromString("7.0.96.5.0.255");
    private final static ObisCode METROLOGICAL_FIRMWARE_VERSION = ObisCode.fromString("7.0.0.2.1.255");
    private final static ObisCode NON_METROLOGICAL_FIRMWARE_VERSION = ObisCode.fromString("7.1.0.2.1.255");

    private final A2 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private List<ObisCode> binaryMaps16BitObisCodes = Arrays.asList(VALVE_CONFIGURATION_PGV);
    private List<ObisCode> binaryMaps8BitObisCodes = Arrays.asList(VALVE_CLOSURE_CAUSE, UNITS_DEVICE_STATUS);
    private List<ObisCode> firmwareVersionObisCodes = Arrays.asList(METROLOGICAL_FIRMWARE_VERSION, NON_METROLOGICAL_FIRMWARE_VERSION);

    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    public A2RegisterFactory(A2 a2, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = a2;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : offlineRegisters) {
            CollectedRegister collectedRegister = readRegister(offlineRegister);
            result.add(collectedRegister);
        }
        return result;
    }

    private CollectedRegister readRegister(OfflineRegister offlineRegister) {
        ObisCode obisCode = offlineRegister.getObisCode();
        final UniversalObject uo;
        RegisterValue registerValue = null;
        try {
            uo = protocol.getDlmsSession().getMeterConfig().findObject(obisCode);
        } catch (NotInObjectListException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
        try {
            //class 1
            if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                Data data = protocol.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                AbstractDataType valueAttr = data.getValueAttr();
                OctetString octetString = valueAttr.getOctetString();
                // octet-string
                if (octetString != null && octetString.stringValue() != null) {
                    TimeZone timeZone = protocol.getTimeZone();
                    DateTimeOctetString dateTime = octetString.getDateTime(timeZone);
                    // date and time
                    if (dateTime != null) {
                        registerValue = new RegisterValue(obisCode, dateTimeFormatter.print((dateTime.getValue().getTimeInMillis())));
                    } else if (isFirmwareVersion(obisCode)) {
                        String description = decodeFirmwareVersion(octetString);
                        registerValue = new RegisterValue(obisCode, description);
                    } else {
                        // only the signatures remain
                        registerValue = new RegisterValue(obisCode, getHexString(octetString));
                    }
                }
                // unsigned & double-long-unsigned & long-unsigned
                Unsigned32 value32 = valueAttr.getUnsigned32();
                Quantity quantity = null;
                if (value32 != null) {
                    quantity = new Quantity(value32.getValue(), data.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity);
                }
                Unsigned16 value16 = valueAttr.getUnsigned16();
                if (value16 != null) {
                    quantity = new Quantity(value16.getValue(), data.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity);
                }
                Unsigned8 value8 = valueAttr.getUnsigned8();
                if (value8 != null) {
                    quantity = new Quantity(value8.getValue(), data.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity);
                }
                if (quantity != null) {
                    registerValue = checkBinaryMap(obisCode, registerValue, quantity);
                }
                // structure
                Structure structure = valueAttr.getStructure();
                if (structure != null) {
                    registerValue = readStructure(data);
                }
                // visible-string
                VisibleString visibleString = valueAttr.getVisibleString();
                if (visibleString != null) {
                    registerValue = new RegisterValue(obisCode, visibleString.getStr());
                }
                // enum
                TypeEnum typeEnum = valueAttr.getTypeEnum();
                if (typeEnum != null) {
                    registerValue = new RegisterValue(obisCode, new Quantity(typeEnum.getValue(), Unit.get("")));
                }
                // date
                CosemDate date = valueAttr.getDate();
                if( date != null){
                    registerValue = new RegisterValue(obisCode, date.toString());
                }
                // time
                CosemTime time = valueAttr.getTime();
                    if(time != null){
                        registerValue = new RegisterValue(obisCode, time.toString());
                    }

                // class 3
            } else if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = protocol.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
//                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit()); // The scalar and unit are not used in class 3
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), 255, 0);

                // process Battery change authorization structure
                if (obisCode.equals(BATERY_CHANGE_AUTHORIZATION)) {
                    Structure structure = register.getValueAttr().getStructure();
                    String authorization = Integer.toBinaryString(structure.getDataType(0).getUnsigned8().getValue());
                    int presetDuration = structure.getDataType(1).getUnsigned8().getValue();
                    String description = String.join(" ", "authorization = ",
                            StringUtils.leftPad(authorization, 8, "0"), "\npreset duration (min) = ", Integer.toString(presetDuration));
                    registerValue = new RegisterValue(obisCode, description);
                } else {
                    registerValue = new RegisterValue(obisCode, quantity);
                }

                // class 4
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.GSM_STANDARD_STATUS.getClassId()) {
                GPRSStandardStatus gprsStatus = protocol.getDlmsSession().getCosemObjectFactory().getGPRSStandardStatus(obisCode);
                int strength = gprsStatus.readSignalStrength().getInteger16().intValue();
                registerValue = new RegisterValue(obisCode, new Quantity(strength, Unit.get("")));
            } else if (uo.getClassID() == DLMSClassId.GSM_DIAGNOSTICS.getClassId()) {
                GSMDiagnosticsIC gsmDiagnosticsIC = protocol.getDlmsSession().getCosemObjectFactory().getGSMDiagnosticsIC(obisCode);
                Structure structure = gsmDiagnosticsIC.readPP3NetworkStatus();
                int rsrpValue = structure.getDataType(0).intValue();
                int rsrqValue = structure.getDataType(1).intValue();
                String description = "RSRP = "+ rsrpValue + "\nRSRQ = "+rsrqValue;
                registerValue = new RegisterValue(obisCode, description);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            }
            return createCollectedRegister(registerValue, offlineRegister);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                } else {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                }
            } else {
                throw ConnectionCommunicationException.numberOfRetriesReached(e, protocol.getDlmsSession().getProperties().getRetries() + 1);
            }
        }
    }

    private boolean isFirmwareVersion(ObisCode obisCode) {
        return firmwareVersionObisCodes.contains(obisCode);
    }

    protected String decodeFirmwareVersion(OctetString octetString) {
        byte[] value = octetString.getOctetStr();
        int version = (((((int) value[0]) & 0xFF) << 8) | (((int) value[1]) & 0xFF)) & 0xFFFF;
        int commit = (((((int) value[2]) & 0xFF) << 8) | (((int) value[3]) & 0xFF)) & 0xFFFF;
        int buildDate = (((((int) value[4]) & 0xFF) << 8) | (((int) value[5]) & 0xFF)) & 0xFFFF;
        int major = (version & 0xF800) >> 11;
        int minor = (version & 0x07C0) >> 6;
        int fix = (version & 0x003F);
        String versionString = String.join(".", Integer.toString(major), Integer.toString(minor), Integer.toString(fix));

        int year = 2015 + ((buildDate & 0xFE00) >> 9);
        int month = (buildDate & 0x01E0) >> 5;
        int day = (buildDate & 0x001F);
        String date = String.join("-", Integer.toString(year), Integer.toString(month), Integer.toString(day));

        return String.join(" ", "version =", versionString,
                "\ncommit number =", Integer.toString(commit), "\ndate =", date);
    }

    private String getHexString(OctetString octetString) {
        return Hex.encodeHexString(octetString.getOctetStr());
    }

    private RegisterValue checkBinaryMap(ObisCode obisCode, RegisterValue registerValue, Quantity quantity) {
        String binary = quantity.getAmount().toBigInteger().toString(2);
        int binaryLenth = 0;
        if (isBinaryMap16Bit(obisCode)) {
            binaryLenth = 16;
        }
        if (isBinaryMap8Bit(obisCode)) {
            binaryLenth = 8;
        }
        if (binaryLenth == 0) {
            return registerValue;
        }
        return new RegisterValue(obisCode, StringUtils.leftPad(binary, binaryLenth, "0"));
    }

    private boolean isBinaryMap8Bit(ObisCode obisCode) {
        return binaryMaps8BitObisCodes.contains(obisCode);
    }

    private boolean isBinaryMap16Bit(ObisCode obisCode) {
        return binaryMaps16BitObisCodes.contains(obisCode);
    }

    private RegisterValue readStructure(Data data) throws IOException {
        AbstractDataType dataType = data.getValueAttr();
        Structure structure = dataType.getStructure();
        ObisCode obisCode = data.getObisCode();
        String description = structure.toString();
        if (obisCode.equals(INSTALLER_MAITAINER_SETUP)) {
            String permission = Integer.toBinaryString(structure.getDataType(0).getUnsigned8().getValue());
            Date scheduledTime = new Date(structure.getDataType(1).getUnsigned32().getValue());
            int presetDuration = structure.getDataType(2).getUnsigned16().getValue();
            description = String.join(" ", "permission =", StringUtils.leftPad(permission, 8, "0"),
                    "\nscheduled time =", dateTimeFormatter.print(scheduledTime.getTime()), "\npreset duration (hours) =", String.valueOf(presetDuration));
        } else if (obisCode.equals(GLOBAL_FRAMECOUNTER_THRESHOLDS)) {
            String highThreshold = Long.toString(structure.getDataType(0).getUnsigned32().getValue());
            String lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned8().getValue());
            description = String.join(" ", "high threshold =", highThreshold,
                    "\nlow threshold =", lowThreshold);
        } else if (obisCode.equals(USER_MESSAGE)) {
            Date dateTime = new Date(structure.getDataType(0).getUnsigned32().getValue());
            int userId = structure.getDataType(1).getUnsigned16().getValue();
            VisibleString userName = structure.getDataType(2).getVisibleString();
            description = String.join(" ","date time=",dateTimeFormatter.print(dateTime.getTime()), "\nuser id =", Integer.toString(userId),
                    "\nuser name =", userName.getStr());
        } else if (obisCode.equals(ON_DEMAND_SNAPSHOT_TIME)) {
            Date dateTime = new Date(structure.getDataType(0).getUnsigned32().getValue());
            String reason = Integer.toBinaryString(structure.getDataType(1).getUnsigned8().getValue());
            description = String.join(" ", "date time =", dateTimeFormatter.print(dateTime.getTime()),
                    "\nreason =", StringUtils.leftPad(reason, 8, "0"));
        } else if (obisCode.equals(LEAKAGE_TEST_PARAMETERS)) {
            int testDuration = structure.getDataType(0).getUnsigned16().getValue();
            int gasFlow = structure.getDataType(1).getUnsigned16().getValue();
            description = String.join(" ", "test duration (sec) =", Integer.toString(testDuration),
                    "\ngas flow (l/h) =", Integer.toString(gasFlow));

        } else if (obisCode.equals(SYNCHRONIZATION_REGISTERS)) {
            long numberOfSynchronizations = structure.getDataType(0).getUnsigned32().getValue();
            long increasedSeconds = structure.getDataType(1).getUnsigned32().getValue();
            long decreasedSeconds = structure.getDataType(2).getUnsigned32().getValue();
            description = String.join(" ", "number of synchronizations =", Long.toString(numberOfSynchronizations),
                    "\nincreased seconds =", Long.toString(increasedSeconds), "\ndecreased seconds =", Long.toString(decreasedSeconds));
        }
        return new RegisterValue(obisCode, description);
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), new DeviceIdentifierById(offlineRtuRegister.getDeviceId()));
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }
}
