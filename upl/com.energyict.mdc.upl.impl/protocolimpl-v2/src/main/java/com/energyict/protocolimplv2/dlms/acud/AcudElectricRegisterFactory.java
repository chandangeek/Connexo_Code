package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.util.Iterator;

public class AcudElectricRegisterFactory extends AcudRegisterFactory {

    public final static ObisCode LOAD_LIMIT = ObisCode.fromString("0.0.94.20.66.255");
    public static final String LIMIT_SEPARATOR = ";";
    public static final String VALUE_SEPARATOR = ",";

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
        } else if (obisCode.equals(ACTIVE_TAX) || obisCode.equals(PASIVE_TAX)) {
            description = readTax(structure);
        } else if (obisCode.equals(ACTIVE_STEP_TARIFF) || obisCode.equals(PASIVE_STEP_TARIFF)) {
            description = readStepTariff(structure);
        } else return super.readStructure(obisCode, structure);
        return new RegisterValue(obisCode, description);
    }

    private String readTax(Structure structure) {
        String monthlyTax = Long.toString(structure.getDataType(0).getUnsigned32().getValue());
        String zeroConsumptionTax = Long.toString(structure.getDataType(1).getUnsigned32().getValue());
        String consumptionTax = Long.toString(structure.getDataType(2).getUnsigned32().getValue());
        String consumptionAmount = Integer.toString(structure.getDataType(3).getUnsigned16().getValue());
        String consumptionLimit = Integer.toString(structure.getDataType(4).getUnsigned16().getValue());
        StringBuffer buff = new StringBuffer();
        buff.append("Monthy Tax = " + monthlyTax + ", ");
        buff.append("Zero Consumption Tax = " + zeroConsumptionTax + ", ");
        buff.append("Consumption Tax = " + consumptionTax + ", ");
        buff.append("Consumption Amount = " + consumptionAmount + " KWH, ");
        buff.append("Consumption Limit = " + consumptionLimit + " KWH.");
        return buff.toString();
    }

    private String readStepTariff(Structure structure) {
        StringBuffer buff = new StringBuffer();

        String tarrifCode = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
        int additionalTaxesId = structure.getDataType(1).getTypeEnum().getValue();
        int graceRecalculationId = structure.getDataType(2).getTypeEnum().getValue();
        String graceRecalculationValue = Integer.toString(structure.getDataType(3).getUnsigned16().getValue());
        Array stepTariffArray = structure.getDataType(4).getArray();

        buff.append("Tarif Code = " + tarrifCode + ", \n");
        buff.append("Aditional Taxes = " + ChargeDeviceMessage.AdditionalTaxesType.getDescriptionValue(additionalTaxesId) + ", \n");
        buff.append("Grace Recalculation = " + ChargeDeviceMessage.GraceRecalculationType.getDescriptionValue(graceRecalculationId) + ", \n");
        buff.append("Grace Recalculation Value = " + graceRecalculationValue + ", \n");

        for (int i = 0; i <= 9; i++) {
            Structure stepTariff = stepTariffArray.getDataType(i).getStructure();

            String tariffCharge = Integer.toString(stepTariff.getDataType(0).getUnsigned16().getValue());
            String price = Long.toString(stepTariff.getDataType(1).getUnsigned32().getValue());
            int recalculationId = stepTariff.getDataType(2).getTypeEnum().getValue();
            String graceWarning = Integer.toString(stepTariff.getDataType(3).getUnsigned16().getValue());
            String additionalTax = Long.toString(stepTariff.getDataType(4).getUnsigned32().getValue());

            buff.append("Tariff Charge = " + tariffCharge + ", ");
            buff.append("Price = " + price + ", ");
            buff.append("Recalculation = " + ChargeDeviceMessage.RecalculationType.getDescriptionValue(recalculationId) + ", ");
            buff.append("Grace Warning = " + graceWarning + ", ");
            buff.append("Aditional Taxe = " + additionalTax + ", \n");
        }
        return buff.toString();
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