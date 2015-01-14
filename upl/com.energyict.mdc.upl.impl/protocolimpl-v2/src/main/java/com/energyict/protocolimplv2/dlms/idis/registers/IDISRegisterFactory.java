package com.energyict.protocolimplv2.dlms.idis.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierById;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.idis.registers.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.AM500;
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

    private final AM500 AM500;
    private static final String ALARM_REGISTER = "0.0.97.98.0.255";
    private final DLMSAttributeMapper[] attributeMappers;
    public static final ObisCode SFSK_PHY_MAC_SETUP = ObisCode.fromString("0.0.26.0.0.255");
    public static final ObisCode SFSK_ACTIVE_INITIATOR = ObisCode.fromString("0.0.26.1.0.255");
    public static final ObisCode SFSK_SYNC_TIMEOUTS = ObisCode.fromString("0.0.26.2.0.255");
    public static final ObisCode SFSK_MAC_COUNTERS = ObisCode.fromString("0.0.26.3.0.255");
    public static final ObisCode SFSK_IEC_LLC_SETIP = ObisCode.fromString("0.0.26.5.0.255");
    public static final ObisCode SFSK_REPORTING_SYSTEM_LIST = ObisCode.fromString("0.0.26.6.0.255");

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
            ObisCode obisCode = offlineRegister.getObisCode();

            try {
                //Read out plc object attributes, where the obiscode F-field is the attribute number
                boolean isAttributeRegisterMapped = false;
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
                        result.add(createCollectedRegister(registerValue, offlineRegister));
                        isAttributeRegisterMapped = true;
                        break;   //Exit this attribute mapper loop
                    }
                }

                if (isAttributeRegisterMapped) {
                    continue;   //Move on, read out the next register
                }

                //Read billing registers
                if (obisCode.getF() != 255) {
                    try {
                        HistoricalValue historicalValue = AM500.getStoredValues().getHistoricalValue(obisCode);
                        RegisterValue registerValue = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime());
                        result.add(createCollectedRegister(registerValue, offlineRegister));
                    } catch (NoSuchRegisterException e) {
                        result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    }
                    continue;
                }

                final UniversalObject uo;
                try {
                    uo = AM500.getDlmsSession().getMeterConfig().findObject(obisCode);
                } catch (ProtocolException e) {
                    result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible));
                    continue;   //Move on, read out the next register
                }

                RegisterValue registerValue = null;
                if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                    final Register register = AM500.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                    registerValue = new RegisterValue(obisCode, register.getQuantityValue());
                } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                    final DemandRegister register = AM500.getDlmsSession().getCosemObjectFactory().getDemandRegister(obisCode);
                    registerValue = new RegisterValue(obisCode, register.getQuantityValue());
                } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                    final ExtendedRegister register = AM500.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                    registerValue = new RegisterValue(obisCode, register.getQuantityValue());
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
                }
                if (registerValue != null) {
                    result.add(createCollectedRegister(registerValue, offlineRegister));
                } else {
                    result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, AM500.getDlmsSession())) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    } else {
                        result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                    }
                }
            }
        }
        return result;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage));
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