package com.energyict.protocolimplv2.dlms.idis.am500.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKActiveInitiatorMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKIec61334LLCSetupMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKMacCountersMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKPhyMacSetupMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKReportingSystemListMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKSyncTimeoutsMapper;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/01/2015 - 11:13
 */
public class IDISRegisterFactory implements DeviceRegisterSupport {

    public static final ObisCode SFSK_PHY_MAC_SETUP = ObisCode.fromString("0.0.26.0.0.255");
    public static final ObisCode SFSK_ACTIVE_INITIATOR = ObisCode.fromString("0.0.26.1.0.255");
    public static final ObisCode SFSK_SYNC_TIMEOUTS = ObisCode.fromString("0.0.26.2.0.255");
    public static final ObisCode SFSK_MAC_COUNTERS = ObisCode.fromString("0.0.26.3.0.255");
    public static final ObisCode SFSK_IEC_LLC_SETIP = ObisCode.fromString("0.0.26.5.0.255");
    public static final ObisCode SFSK_REPORTING_SYSTEM_LIST = ObisCode.fromString("0.0.26.6.0.255");
    private static final String ALARM_REGISTER = "0.0.97.98.0.255";
    private final com.energyict.protocolimplv2.dlms.idis.am500.AM500 AM500;
    private final DLMSAttributeMapper[] attributeMappers;

    public IDISRegisterFactory(com.energyict.protocolimplv2.dlms.idis.am500.AM500 AM500) {
        this.AM500 = AM500;
        this.attributeMappers = new DLMSAttributeMapper[]{
                new SFSKPhyMacSetupMapper(SFSK_PHY_MAC_SETUP, AM500.getDlmsSession().getCosemObjectFactory()),
                new SFSKActiveInitiatorMapper(SFSK_ACTIVE_INITIATOR, AM500.getDlmsSession().getCosemObjectFactory()),
                new SFSKSyncTimeoutsMapper(SFSK_SYNC_TIMEOUTS, AM500.getDlmsSession().getCosemObjectFactory()),
                new SFSKMacCountersMapper(SFSK_MAC_COUNTERS, AM500.getDlmsSession().getCosemObjectFactory()),
                new SFSKIec61334LLCSetupMapper(SFSK_IEC_LLC_SETIP, AM500.getDlmsSession().getCosemObjectFactory()),
                new SFSKReportingSystemListMapper(SFSK_REPORTING_SYSTEM_LIST, AM500.getDlmsSession().getCosemObjectFactory()),
        };
    }

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
                    HistoricalValue historicalValue = AM500.getStoredValues().getHistoricalValue(obisCode);
                    RegisterValue registerValue = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime());
                    return createCollectedRegister(registerValue, offlineRegister);
                } catch (NotInObjectListException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                } catch (NoSuchRegisterException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                }
            }

            if (isMBusValueChannel(obisCode)) {
                obisCode = AM500.getPhysicalAddressCorrectedObisCode(obisCode, offlineRegister.getDeviceSerialNumber());
            }

            final UniversalObject uo;
            try {
                uo = AM500.getDlmsSession().getMeterConfig().findObject(obisCode);
            } catch (ProtocolException e) {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }

            RegisterValue registerValue = null;
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = AM500.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity);
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister register = AM500.getDlmsSession().getCosemObjectFactory().getDemandRegister(obisCode);
                Quantity quantity = new Quantity(register.getAttrbAbstractDataType(2).toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = AM500.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                final Disconnector register = AM500.getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode);
                registerValue = new RegisterValue(obisCode, "" + register.getState());
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = AM500.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                OctetString octetString = register.getValueAttr().getOctetString();
                if (octetString != null && octetString.stringValue() != null) {
                    registerValue = new RegisterValue(obisCode, octetString.stringValue());
                } else {
                    Unsigned32 value = register.getValueAttr().getUnsigned32();
                    if (value != null) {
                        if (obisCode.equals(ObisCode.fromString(ALARM_REGISTER))) {
                            AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(obisCode, value);
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
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, AM500.getDlmsProperties().getRetries() + 1)) {
                if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                } else {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                }
            } else {
                throw new ConnectionCommunicationException(AM500.getDlmsSession().getProperties().getRetries() + 1);
            }
        }
    }

    private boolean isMBusValueChannel(ObisCode obisCode) {
        return ((obisCode.getA() == 0) && (obisCode.getC() == 24) && (obisCode.getD() == 2) && (obisCode.getE() > 0 && obisCode.getE() < 5) && obisCode.getF() == 255);
    }

    public RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = this.AM500.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.AM500.getIssueService()
                    .newWarning(register.getObisCode(), MessageSeeds.REGISTER_INCOMPATIBLE, register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.AM500.getIssueService()
                    .newWarning(register.getObisCode(), MessageSeeds.REGISTER_NOT_SUPPORTED, register.getObisCode(), errorMessage[0]));
        }
        return collectedRegister;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = this.AM500.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister), offlineRegister.getReadingType());
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime().toInstant(), registerValue.getFromTime().toInstant(), registerValue.getToTime().toInstant(), registerValue.getEventTime()
                .toInstant());
        return deviceRegister;
    }
}