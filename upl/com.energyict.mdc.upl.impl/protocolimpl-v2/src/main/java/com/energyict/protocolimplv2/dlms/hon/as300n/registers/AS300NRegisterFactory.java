package com.energyict.protocolimplv2.dlms.hon.as300n.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
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
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.idis.registers.*;
import com.energyict.protocolimplv2.dlms.hon.as300n.AS300N;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AS300NRegisterFactory implements DeviceRegisterSupport{
    private static final String ALARM_REGISTER = "0.0.97.98.0.255";


    private final AS300N as300n;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final DLMSAttributeMapper[] attributeMappers;

    public AS300NRegisterFactory(AS300N as300n, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.as300n= as300n;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        this.attributeMappers = new DLMSAttributeMapper[]{};
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

        try {
            //Read out plc object attributes, where the obiscode F-field is the attribute number
            for (DLMSAttributeMapper attributeMapper : attributeMappers) {
                if (attributeMapper.isObisCodeMapped(obisCode)) {
                    RegisterValue registerValue = attributeMapper.getRegisterValue(obisCode);
                    try {
                        String textValue = registerValue.getText();
                        BigDecimal value = new BigDecimal(textValue);
                        registerValue.setQuantity(new Quantity(value, Unit.get("")));
                    } catch (NumberFormatException e) {
                        //Text register, quantity is not used
                    }
                    return createCollectedRegister(registerValue, offlineRegister);
                }
            }

            //Read billing registers
            if (obisCode.getF() != 255) {
                try {
                    HistoricalValue historicalValue = as300n.getStoredValues().getHistoricalValue(obisCode);
                    RegisterValue registerValue = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime());
                    return createCollectedRegister(registerValue, offlineRegister);
                } catch (NotInObjectListException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                } catch (NoSuchRegisterException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                }
            }

            final UniversalObject uo;
            try {
                uo = as300n.getDlmsSession().getMeterConfig().findObject(obisCode);
            } catch (ProtocolException e) {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }

            RegisterValue registerValue = null;
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = as300n.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity);
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister register = as300n.getDlmsSession().getCosemObjectFactory().getDemandRegister(obisCode);
                Quantity quantity = new Quantity(register.getAttrbAbstractDataType(2).toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = as300n.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                final Disconnector register = as300n.getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode);
                registerValue = new RegisterValue(obisCode, "" + register.getState());
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = as300n.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                OctetString octetString = register.getValueAttr().getOctetString();
                if (octetString != null && octetString.stringValue() != null) {
                    registerValue = new RegisterValue(obisCode, octetString.stringValue());
                } else {
                    Unsigned32 value = register.getValueAttr().getUnsigned32();
                    if (value != null) {
                        if (obisCode.equals(ObisCode.fromString(ALARM_REGISTER))) {
                            AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(obisCode, value.longValue());
                            registerValue = alarmBitsRegister.getRegisterValue();
                        } else {
                            registerValue = new RegisterValue(obisCode, new Quantity(value.getValue(), Unit.get("")));
                        }
                    }
                }
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            }
            return createCollectedRegister(registerValue, offlineRegister);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, as300n.getDlmsSessionProperties().getRetries()+1)) {
                if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                } else {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                }
            } else {
                throw ConnectionCommunicationException.numberOfRetriesReached(e, as300n.getDlmsSession().getProperties().getRetries() + 1);
            }
        }
    }

    private boolean isMBusValueChannel(ObisCode obisCode) {
        return ((obisCode.getA() == 0) && (obisCode.getC() == 24) && (obisCode.getD() == 2) && (obisCode.getE() > 0 && obisCode.getE() < 5) && obisCode.getF() == 255);
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }
}


