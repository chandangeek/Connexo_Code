package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class AcudElectricRegisterFactory extends AcudRegisterFactory {

    public final static ObisCode LOAD_LIMIT = ObisCode.fromString("0.0.94.20.66.255");
    public static final String LIMIT_SEPARATOR = ";";
    public static final String VALUE_SEPARATOR = ",";

    @SuppressWarnings("unchecked")
    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = getMeterProtocol().getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister
                    .setFailureInformation(ResultType.InCompatible,
                            getMeterProtocol().getIssueFactory().createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": " + errorMessage[0].toString(),
                                    register.getObisCode(),
                                    errorMessage[0]));
        } else {
            collectedRegister
                    .setFailureInformation(ResultType.NotSupported,
                            getMeterProtocol().getIssueFactory().createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": Not Supported",
                                    register.getObisCode()));
        }
        return collectedRegister;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> result = super.readRegisters(offlineRegisters);
        result.addAll(readBillingRegisters(offlineRegisters)); // Cause these cannot be read out in bulk

        return result;
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
            HistoricalValue historicalValue = (getMeterProtocol()).getStoredValues().getHistoricalValue(offlineRegister.getObisCode());
            RegisterValue registerValue = new RegisterValue(
                    offlineRegister.getObisCode(),
                    historicalValue.getQuantityValue(),
                    historicalValue.getEventTime(), // event time
                    null, // from time
                    historicalValue.getBillingDate(), // to time
                    historicalValue.getBillingDate(), // Set measurement time as billing date. TODO: test other protocols
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

    @SuppressWarnings("Duplicates")
    private CollectedRegister handleIOException(OfflineRegister offlineRegister, IOException e) {
        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession().getProperties().getRetries())) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        } else {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMeterProtocol().getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = this.getMeterProtocol().getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    public AcudElectricRegisterFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected RegisterValue readStructure(ObisCode obisCode, Structure structure) throws IOException {
        String highThreshold;
        String lowThreshold;
        String description;
        if (obisCode.equals(MONEY_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(2).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingCreditHighDefaultTranslation, DeviceMessageConstants.remainingCreditLowDefaultTranslation);
        } else if (obisCode.equals(CONSUMPTION_CREDIT_THRESHOLD)) {
            highThreshold = Long.toString(structure.getDataType(0).getUnsigned32().getValue());
            lowThreshold = Long.toString(structure.getDataType(1).getUnsigned32().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.consumedCreditHighDefaultTranslation, DeviceMessageConstants.consumedCreditLowDefaultTranslation);
        } else if (obisCode.equals(TIME_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingTimeHighDefaultTranslation, DeviceMessageConstants.remainingTimeLowDefaultTranslation);
        } else return super.readStructure(obisCode, structure);
        return new RegisterValue(obisCode, description);
    }

    protected RegisterValue readArray(ObisCode obisCode, Array array) throws IOException {
        String description;
        if (obisCode.equals(LOAD_LIMIT)) {
            description = readLoadLimits(array);
        } else return super.readArray(obisCode, array);
        return new RegisterValue(obisCode, description);
    }

    protected String readLoadLimits(Array array) {
        StringBuffer buff = new StringBuffer("{");
        for (Iterator<AbstractDataType> it = array.iterator(); it.hasNext(); ) {
            Structure limit = (Structure) it.next();
            buff.append(limit.getDataType(0).getUnsigned16().getValue());
            buff.append(VALUE_SEPARATOR);
            buff.append(limit.getDataType(1).getUnsigned16().getValue());
            buff.append(VALUE_SEPARATOR);
            buff.append(limit.getDataType(2).getUnsigned16().getValue());
            buff.append(LIMIT_SEPARATOR);
        }
        buff.append("}");
        return buff.toString();
    }
}