package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;

public class AcudElectricMessageExecutor extends AcudMessageExecutor {

    private static final ObisCode MONEY_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.67.255");
    private static final ObisCode CONSUMPTION_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.68.255");
    private static final ObisCode TIME_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.69.255");

    public static final int CREDIT_THRESHOLD_VALUE_ATTR = 2;

    public AcudElectricMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
       if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_MONEY_CREDIT_THRESHOLD)) {
            updateMoneyCreditThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_CONSUMPTION_CREDIT_THRESHOLD)) {
            updateConsumptionCreditThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_TIME_CREDIT_THRESHOLD)) {
            updateTimeCreditThreshold(pendingMessage);
        } else
            return super.executeMessage(pendingMessage, collectedMessage );
       return collectedMessage;
    }

    private void updateMoneyCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer remainingCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditHigh));
        Integer remainingCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned8(remainingCreditHigh));
        thresholdStructure.addDataType(new Unsigned8(remainingCreditLow));
        getCosemObjectFactory().writeObject(MONEY_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), CREDIT_THRESHOLD_VALUE_ATTR, thresholdStructure.getBEREncodedByteArray());
    }

    private void updateConsumptionCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer consumedCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditHigh));
        Integer consumedCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned16(consumedCreditHigh));
        thresholdStructure.addDataType(new Unsigned16(consumedCreditLow));
        getCosemObjectFactory().writeObject(CONSUMPTION_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), CREDIT_THRESHOLD_VALUE_ATTR, thresholdStructure.getBEREncodedByteArray());
    }

    private void updateTimeCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Long remainingTimeHigh = Long.parseLong(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeHigh));
        Long remainingTimeLow = Long.parseLong(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned32(remainingTimeHigh));
        thresholdStructure.addDataType(new Unsigned32(remainingTimeLow));
        getCosemObjectFactory().writeObject(TIME_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), CREDIT_THRESHOLD_VALUE_ATTR, thresholdStructure.getBEREncodedByteArray());
    }
}