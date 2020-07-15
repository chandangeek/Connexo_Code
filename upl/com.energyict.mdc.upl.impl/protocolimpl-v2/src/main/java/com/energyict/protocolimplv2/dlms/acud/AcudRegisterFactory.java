package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.common.DisconnectControlState;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AcudRegisterFactory implements DeviceRegisterSupport {

    public final static ObisCode MONEY_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.67.255");
    public final static ObisCode CONSUMPTION_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.68.255");
    public final static ObisCode TIME_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.69.255");
    public final static ObisCode CREDIT_DAY_LIMIT = ObisCode.fromString("0.0.94.20.70.255");
    public final static ObisCode ACTIVE_FIRMWARE = ObisCode.fromString("0.0.0.2.0.255");


    private final Acud protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public AcudRegisterFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.protocol = protocol;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : registers) {
            CollectedRegister collectedRegister = readRegister(offlineRegister);
            result.add(collectedRegister);
        }
        return result;
    }

    private CollectedRegister readRegister(OfflineRegister offlineRegister) {
        try {
            ObisCode obisCode = offlineRegister.getObisCode();
            final UniversalObject uo;
            try {
                uo = protocol.getDlmsSession().getMeterConfig().findObject(obisCode);
            } catch (ProtocolException e) {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }

            RegisterValue registerValue = null;
            if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = protocol.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                AbstractDataType attribute = register.getValueAttr();
                if (attribute.isStructure()) {
                    registerValue = readStructure(obisCode, (Structure) attribute);
                } else if (attribute.isArray()) {
                    registerValue = readArray(obisCode, (Array) attribute);
                } else if (attribute.isVisibleString()) {
                    registerValue = new RegisterValue(obisCode, attribute.getVisibleString().getStr());
                } else if (attribute.isOctetString() && attribute.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, attribute.getOctetString().stringValue());
                } else if (attribute.isBitString() && attribute.getBitString() != null) {
                    registerValue = new RegisterValue(obisCode, new Quantity(attribute.getBitString().toBigDecimal().longValue(), Unit.get("")));
                } else if (attribute.isDateTime()) {
                    registerValue = new RegisterValue(obisCode, attribute.getDateTime().getCalendar(protocol.getTimeZone()).getTime());
                } else {
                    Number value = register.getValueAttr().toBigDecimal();
                    if (value != null) {
                        registerValue = new RegisterValue(obisCode, new Quantity(value, Unit.get("")));
                    } else {
                        return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                    }
                }
            } else if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = protocol.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity);
            } else if (uo.getClassID() == DLMSClassId.REGISTER_MONITOR.getClassId()) {
                RegisterMonitor registerMonitor = protocol.getDlmsSession().getCosemObjectFactory().getRegisterMonitor(obisCode);
                int value = registerMonitor.readThresholds().getDataType(0).intValue();
                registerValue = new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                int value = protocol.getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode).getControlState().getValue();
                registerValue = new RegisterValue(obisCode, DisconnectControlState.fromValue(value).getDescription());
            } else if (uo.getClassID() == DLMSClassId.CREDIT_SETUP.getClassId()) {
                CreditSetup creditSetup = protocol.getDlmsSession().getCosemObjectFactory().getCreditSetup(obisCode);
                int amount = creditSetup.readCurrentCreditAmount().getInteger32().intValue();
                registerValue = new RegisterValue(obisCode, new Quantity(amount, Unit.get("")));
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            }
            return createCollectedRegister(registerValue, offlineRegister);

        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        }
    }

    protected RegisterValue readStructure(ObisCode obisCode, Structure structure) throws IOException {
        String highThreshold;
        String lowThreshold;
        String description;
        if (obisCode.equals(MONEY_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getInteger32().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getInteger32().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingCreditHighDefaultTranslation, DeviceMessageConstants.remainingCreditLowDefaultTranslation);
        } else if (obisCode.equals(CONSUMPTION_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.consumedCreditHighDefaultTranslation, DeviceMessageConstants.consumedCreditLowDefaultTranslation);
        } else if (obisCode.equals(TIME_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned8().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned8().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingTimeHighDefaultTranslation, DeviceMessageConstants.remainingTimeLowDefaultTranslation);
        } else if (obisCode.equals(CREDIT_DAY_LIMIT)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, "Days Limit1", "Days Limit2");
        } else if (obisCode.equals(ACTIVE_FIRMWARE)) {
            String model = structure.getDataType(0).getVisibleString().getStr();
            String version = structure.getDataType(1).getVisibleString().getStr();
            String crc = structure.getDataType(2).getVisibleString().getStr();
            description = "Model=" +model + ", Firmware Version=" + version + ", Firmware CRC=" + crc;
        } else
            throw new ProtocolException("Cannot decode the structure data for the obis code: " + obisCode);
        return new RegisterValue(obisCode, description);
    }

    protected RegisterValue readArray(ObisCode obisCode, Array array) throws IOException {
        throw new ProtocolException("Cannot decode the array data for the obis code: " + obisCode);
    }

    protected String formatDescr(String highThreshold, String lowThreshold, String highDefaultTranslation, String lowDefaultTranslation) {
        return highDefaultTranslation + "=" + highThreshold + ",\n" + lowDefaultTranslation + "=" + lowThreshold + ".";
    }

    @SuppressWarnings("unchecked")
    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister
                    .setFailureInformation(ResultType.InCompatible,
                            issueFactory.createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": " + errorMessage[0].toString(),
                                    register.getObisCode(),
                                    errorMessage[0]));
        } else {
            collectedRegister
                    .setFailureInformation(ResultType.NotSupported,
                            issueFactory.createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": Not Supported",
                                    register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRegister) {
        return new RegisterIdentifierById(offlineRegister.getRegisterId(), offlineRegister.getObisCode(), offlineRegister.getDeviceIdentifier());
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }
}