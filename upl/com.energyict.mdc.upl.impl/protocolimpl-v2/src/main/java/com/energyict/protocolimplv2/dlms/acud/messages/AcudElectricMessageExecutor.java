package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.Calendar;

public class AcudElectricMessageExecutor extends AcudMessageExecutor {

    private static final ObisCode CURRENT_OVER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.11.35.0.255");
    private static final ObisCode CURRENT_OVER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.11.44.0.255");
    private static final ObisCode VOLTAGE_UNDER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.12.31.0.255");
    private static final ObisCode VOLTAGE_UNDER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.12.43.0.255");
    private static final ObisCode LIPF_UNDER_LIMIT_THRESHOLD = ObisCode.fromString("1.0.13.31.0.255");
    private static final ObisCode LIPF_UNDER_LIMIT_TIME_THRESHOLD = ObisCode.fromString("1.0.13.45.0.255");

    public static final ObisCode PASSIVE_STEP_TARIFF_OBIS = ObisCode.fromString("0.0.94.20.75.255");
    public static final ObisCode PASSIVE_TAX_RATES_OBIS = ObisCode.fromString("0.0.94.20.77.255");
    public static final ObisCode STEP_TARIFF_SCHEDULER_OBIS = ObisCode.fromString("0.0.15.0.9.255");
    public static final ObisCode TAX_SCHEDULER_OBIS = ObisCode.fromString("0.0.15.0.10.255");

    public final static ObisCode LOAD_LIMIT = ObisCode.fromString("0.0.94.20.66.255");

    private static final int STEP_TARIFF_SWITCH = 3;
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
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.SWITCH_TAX_AND_STEP_TARIFF)) {
            switchTaxAndStepTariff(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_STEP_TARIFF)) {
            changeStepTariffConfig(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_TAX_RATES)) {
            changeTaxRates(pendingMessage);
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

    private void switchTaxAndStepTariff(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activationDate).getValue();
        Calendar activationTime = Calendar.getInstance(getProtocol().getTimeZone());
        activationTime.setTimeInMillis(Long.valueOf(attributeTime));
        SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(STEP_TARIFF_SCHEDULER_OBIS);
        Structure tariffStruct = new Structure();
        tariffStruct.addDataType(new OctetString(SCRIPTS_OBIS.getLN()));
        int tariffType = getTariffType(pendingMessage);
        tariffStruct.addDataType(new Unsigned16(tariffType));
        singleActionSchedule.writeExecutedScript(tariffStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(activationTime));
        if (tariffType == STEP_TARIFF_SWITCH) {
            singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(TAX_SCHEDULER_OBIS);
            singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(activationTime));
        }
    }

    private int getTariffType(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.tariffType);
        return ChargeDeviceMessage.TariffType.entryForDescription(description).getId();
    }

    private void changeStepTariffConfig(OfflineDeviceMessage pendingMessage) throws IOException {
        Structure changeStepTariff = new Structure();
        Integer tarrifCode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.tariffCode));
        String additionalTaxes = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.additionalTaxesType);
        Integer additionalTaxesId = ChargeDeviceMessage.AdditionalTaxesType.entryForDescription(additionalTaxes).getId();
        String graceRecalculation = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.graceRecalculationType);
        Integer graceRecalculationId = ChargeDeviceMessage.GraceRecalculationType.entryForDescription(graceRecalculation).getId();
        Integer graceRecalculationValue = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.graceRecalculationValue));
        changeStepTariff.addDataType(new Unsigned16(tarrifCode));
        changeStepTariff.addDataType(new TypeEnum(additionalTaxesId));
        changeStepTariff.addDataType(new TypeEnum(graceRecalculationId));
        changeStepTariff.addDataType(new Unsigned16(graceRecalculationValue));
        Array changeStepTariffArray = new Array();
        for (int i = 1; i <= 10; i++) {
            Integer price = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, PRICE_STEP + i));
            String recalculation = getDeviceMessageAttributeValue(pendingMessage, RECALCULATION_TYPE_STEP + i);
            Integer recalculationId = ChargeDeviceMessage.RecalculationType.entryForDescription(recalculation).getId();
            Integer graceWarning = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, GRACE_WARNING_STEP + i));
            Integer additionalTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, ADDITIONAL_TAX_STEP + i));
            Structure changeStep = new Structure();
            addStepTarifCharge(pendingMessage, changeStep, i);
            changeStep.addDataType(new Unsigned32(price));
            changeStep.addDataType(new TypeEnum(recalculationId));
            changeStep.addDataType(new Unsigned16(graceWarning));
            changeStep.addDataType(new Unsigned32(additionalTax));
            changeStepTariffArray.addDataType(changeStep);
        }
        changeStepTariff.addDataType(changeStepTariffArray);
        getCosemObjectFactory().writeObject(PASSIVE_STEP_TARIFF_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), changeStepTariff.getBEREncodedByteArray());
    }

    private void changeTaxRates(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer monthlyTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.monthlyTax));
        Integer zeroConsumptionTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.zeroConsumptionTax));
        Integer consumptionTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionTax));
        Integer consumptionAmount = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionAmount));
        Integer consumptionLimit = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionLimit));
        Structure taxRatesStructure = new Structure();
        taxRatesStructure.addDataType(new Unsigned32(monthlyTax));
        taxRatesStructure.addDataType(new Unsigned32(zeroConsumptionTax));
        taxRatesStructure.addDataType(new Unsigned32(consumptionTax));
        taxRatesStructure.addDataType(new Unsigned16(consumptionAmount));
        taxRatesStructure.addDataType(new Unsigned16(consumptionLimit));
        getCosemObjectFactory().writeObject(PASSIVE_TAX_RATES_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), taxRatesStructure.getBEREncodedByteArray());
    }

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
            limit.addDataType(new Unsigned16(startValue));
            limit.addDataType(new Unsigned16(stopValue));
            limit.addDataType(new Unsigned16(limitValue));
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