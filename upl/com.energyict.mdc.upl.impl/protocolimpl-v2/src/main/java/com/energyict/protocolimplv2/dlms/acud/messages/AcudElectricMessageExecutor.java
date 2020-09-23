package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;

import java.io.IOException;

public class AcudElectricMessageExecutor extends AcudMessageExecutor {

    private static final ObisCode CURRENT_OVER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.11.35.0.255");
    private static final ObisCode CURRENT_OVER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.11.44.0.255");
    private static final ObisCode VOLTAGE_UNDER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.12.31.0.255");
    private static final ObisCode VOLTAGE_UNDER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.12.43.0.255");
    private static final ObisCode LIPF_UNDER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.13.31.0.255");
    private static final ObisCode LIPF_UNDER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.13.45.0.255");

    public final static ObisCode LOAD_LIMIT = ObisCode.fromString("0.0.94.20.66.255");
    public static final String LIMIT_SEPARATOR = ";";
    public static final String VALUE_SEPARATOR = ",";

    public AcudElectricMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.UPDATE_LOAD_LIMITS)) {
            updateLoadLimits(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_CURRENT_OVER_LIMIT_THRESHOLD)) {
            setCurrentOverLimitThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_CURRENT_OVER_LIMIT_TIME_THRESHOLD)) {
            setCurrentOverLimitTimeThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_VOLTAGE_UNDER_LIMIT_THRESHOLD)) {
            setVoltageUnderLimitThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_VOLTAGE_UNDER_LIMIT_TIME_THRESHOLD)) {
            setVoltageUnderLimitTimeThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_LIPF_UNDER_LIMIT_THRESHOLD)) {
            setLiPfUnderLimitThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_LIPF_UNDER_LIMIT_TIME_THRESHOLD)) {
            setLiPfUnderLimitTimeThreshold(pendingMessage);
        } else
            return super.executeMessage(pendingMessage, collectedMessage);
        return collectedMessage;
    }

    protected void updateMoneyCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        String currency = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.currency);
        Integer remainingCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditHigh));
        Integer remainingCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new VisibleString(currency));
        thresholdStructure.addDataType(new Unsigned16(remainingCreditHigh));
        thresholdStructure.addDataType(new Unsigned16(remainingCreditLow));
        getCosemObjectFactory().writeObject(MONEY_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    protected void updateConsumptionCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer consumedCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditHigh));
        Integer consumedCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned32(consumedCreditHigh));
        thresholdStructure.addDataType(new Unsigned32(consumedCreditLow));
        getCosemObjectFactory().writeObject(CONSUMPTION_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    protected void updateTimeCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer remainingTimeHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeHigh));
        Integer remainingTimeLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned16(remainingTimeHigh));
        thresholdStructure.addDataType(new Unsigned16(remainingTimeLow));
        getCosemObjectFactory().writeObject(TIME_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    @Override
    protected void addStepTarifCharge(OfflineDeviceMessage pendingMessage, Structure changeStep, Integer step) throws IOException {
        Integer charge = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, CHARGE_STEP + step));
        changeStep.addDataType(new Unsigned16(charge));
    }

    private void updateLoadLimits(OfflineDeviceMessage pendingMessage) throws IOException {
        Array limits = new Array();
        String limitArray = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.loadLimitArray);
        for (String limitItem : limitArray.trim().split(LIMIT_SEPARATOR)) {
            Structure limit = new Structure();
            String[] limitItemArray = limitItem.trim().split(VALUE_SEPARATOR);
            Integer startValue = Integer.parseInt(limitItemArray[0].trim());
            Integer stopValue = Integer.parseInt(limitItemArray[1].trim());
            Integer limitValue = Integer.parseInt(limitItemArray[2].trim());
            limit.addDataType(new Unsigned16( startValue));
            limit.addDataType(new Unsigned16( stopValue));
            limit.addDataType(new Unsigned16( limitValue));
            limits.addDataType(limit);
        }
        getCosemObjectFactory().writeObject(LOAD_LIMIT, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), limits.getBEREncodedByteArray());
    }

    private void setCurrentOverLimitThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.CurrentOverLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(CURRENT_OVER_LIMIT_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }

    private void setCurrentOverLimitTimeThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.CurrentOverLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(CURRENT_OVER_LIMIT_TIME_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned8(threshold).getBEREncodedByteArray());
    }

    private void setVoltageUnderLimitThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.VoltageUnderLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(VOLTAGE_UNDER_LIMIT_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(threshold).getBEREncodedByteArray());
    }

    private void setVoltageUnderLimitTimeThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.VoltageUnderLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(VOLTAGE_UNDER_LIMIT_TIME_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned8(threshold).getBEREncodedByteArray());
    }

    private void setLiPfUnderLimitThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.LiPfUnderLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(LIPF_UNDER_LIMIT_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Integer16(threshold).getBEREncodedByteArray());
    }

    private void setLiPfUnderLimitTimeThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer threshold = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.LiPfUnderLimitThresholdAttributeName));
        getCosemObjectFactory().writeObject(LIPF_UNDER_LIMIT_TIME_THRESHOLD, DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned8(threshold).getBEREncodedByteArray());
    }
}