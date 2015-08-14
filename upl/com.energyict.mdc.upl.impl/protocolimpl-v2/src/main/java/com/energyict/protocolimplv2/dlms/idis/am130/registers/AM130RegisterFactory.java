package com.energyict.protocolimplv2.dlms.idis.am130.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
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
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/01/2015 - 11:13
 */
public class AM130RegisterFactory implements DeviceRegisterSupport {

    private static final String ALARM_REGISTER1 = "0.0.97.98.0.255";
    private static final String ALARM_REGISTER2 = "0.0.97.98.1.255";
    private static final String ERROR_REGISTER = "0.0.97.97.0.255";
    private final AM130 am130;

    public AM130RegisterFactory(AM130 am130) {
        this.am130 = am130;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : offlineRegisters) {
            CollectedRegister collectedRegister = readRegister(offlineRegister);
            result.add(collectedRegister);
        }
        return result;
    }

    protected CollectedRegister readRegister(OfflineRegister offlineRegister) {
        ObisCode obisCode = offlineRegister.getObisCode();

        try {
            //Read billing registers
            if (obisCode.getF() != 255) {
                try {
                    HistoricalValue historicalValue = am130.getStoredValues().getHistoricalValue(obisCode);
                    RegisterValue registerValue = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime());
                    return createCollectedRegister(registerValue, offlineRegister);
                } catch (NoSuchRegisterException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                } catch (NotInObjectListException e) {
                    return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                }
            }

            if (isMBusValueChannel(obisCode)) {
                obisCode = am130.getPhysicalAddressCorrectedObisCode(obisCode, offlineRegister.getSerialNumber());
            }

            final UniversalObject uo;
            try {
                uo = am130.getDlmsSession().getMeterConfig().findObject(obisCode);
            } catch (ProtocolException e) {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }

            RegisterValue registerValue;
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = am130.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity);
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister register = am130.getDlmsSession().getCosemObjectFactory().getDemandRegister(obisCode);
                Quantity quantity = new Quantity(register.getAttrbAbstractDataType(2).toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = am130.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                final TypeEnum controlState = am130.getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode).getControlState();
                registerValue = new RegisterValue(obisCode, DisconnectControlState.fromState(controlState.intValue()).name());
                registerValue.setQuantity(new Quantity(controlState.intValue(), Unit.get(BaseUnit.UNITLESS)));
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = am130.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                OctetString octetString = register.getValueAttr().getOctetString();
                BooleanObject booleanObject = register.getValueAttr().getBooleanObject();
                if (octetString != null) {
                    registerValue = new RegisterValue(obisCode, octetString.stringValue());
                } else if (booleanObject != null) {
                    registerValue = new RegisterValue(obisCode, String.valueOf(booleanObject.getState()));
                } else {
                    if (obisCode.equals(ObisCode.fromString(ALARM_REGISTER1)) || obisCode.equals(ObisCode.fromString(ERROR_REGISTER))) {
                        AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(obisCode, register.getValueAttr().longValue());
                        registerValue = alarmBitsRegister.getRegisterValue();
                    } else if (obisCode.equals(ObisCode.fromString(ALARM_REGISTER2))) {
                        AlarmBitsRegister2 alarmBitsRegister2 = new AlarmBitsRegister2(obisCode, register.getValueAttr().longValue());
                        registerValue = alarmBitsRegister2.getRegisterValue();
                    } else {
                        registerValue = new RegisterValue(obisCode, new Quantity(register.getValueAttr().toBigDecimal(), Unit.get("")));
                    }
                }
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            }
            return createCollectedRegister(registerValue, offlineRegister);
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