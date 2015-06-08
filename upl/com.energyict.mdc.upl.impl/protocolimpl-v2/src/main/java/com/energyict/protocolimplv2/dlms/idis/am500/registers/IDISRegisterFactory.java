package com.energyict.protocolimplv2.dlms.idis.am500.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.idis.registers.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

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
    private final AM500 AM500;
    private final DLMSAttributeMapper[] attributeMappers;

    public IDISRegisterFactory(AM500 AM500) {
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
                obisCode = AM500.getPhysicalAddressCorrectedObisCode(obisCode, offlineRegister.getSerialNumber());
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
            if (IOExceptionHandler.isUnexpectedResponse(e, AM500.getDlmsSession())) {
                if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                } else {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                }
            } else {
                throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, AM500.getDlmsSession().getProperties().getRetries() + 1);
            }
        }
    }

    private boolean isMBusValueChannel(ObisCode obisCode) {
        return ((obisCode.getA() == 0) && (obisCode.getC() == 24) && (obisCode.getD() == 2) && (obisCode.getE() > 0 && obisCode.getE() < 5) && obisCode.getF() == 255);
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }
}