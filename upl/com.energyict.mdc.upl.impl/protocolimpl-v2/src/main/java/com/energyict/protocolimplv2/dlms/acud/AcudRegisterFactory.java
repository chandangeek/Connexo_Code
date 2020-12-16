package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
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
import com.energyict.protocolimplv2.common.DisconnectControlState;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AcudRegisterFactory implements DeviceRegisterSupport {

    public final static ObisCode MONEY_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.67.255");
    public final static ObisCode CONSUMPTION_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.68.255");
    public final static ObisCode TIME_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.69.255");
    public final static ObisCode ACTIVE_TAX = ObisCode.fromString("0.0.94.20.76.255");
    public final static ObisCode PASIVE_TAX = ObisCode.fromString("0.0.94.20.77.255");
    public final static ObisCode ACTIVE_STEP_TARIFF = ObisCode.fromString("0.0.94.20.74.255");
    public final static ObisCode PASIVE_STEP_TARIFF = ObisCode.fromString("0.0.94.20.75.255");
    public final static ObisCode CREDIT_DAY_LIMIT = ObisCode.fromString("0.0.94.20.70.255");
    public final static ObisCode FRIENDLY_DAY_PERIOD = ObisCode.fromString("0.0.94.20.72.255");
    public final static ObisCode FRIENDLY_WEEKDAYS = ObisCode.fromString("0.0.94.20.73.255");
    public final static ObisCode ACTIVE_FIRMWARE = ObisCode.fromString("0.0.0.2.0.255");
    public static final Integer NEW_ACCOUNT = 1;
    public static final Integer ACTIVE_ACCOUNT = 2;
    public static final Integer CLOSED_ACCOUNT = 3;


    private final Acud protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public AcudRegisterFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.protocol = protocol;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : registers) {
            CollectedRegister collectedRegister = readRegister(offlineRegister);
            result.add(collectedRegister);
        }
        return result;
    }

    private CollectedRegister readRegister(OfflineRegister offlineRegister) {
        try {
            ObisCode obisCode = offlineRegister.getObisCode();
            final UniversalObject uo;
            try {
                uo = protocol.getDlmsSession().getMeterConfig().findObject(obisCode);
            } catch (ProtocolException e) {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }

            RegisterValue registerValue = null;
            if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data register = protocol.getDlmsSession().getCosemObjectFactory().getData(obisCode);
                AbstractDataType attribute = register.getValueAttr();
                if (attribute.isStructure()) {
                    registerValue = readStructure(obisCode, (Structure) attribute);
                } else if (attribute.isArray()) {
                    registerValue = readArray(obisCode, (Array) attribute);
                } else if (attribute.isVisibleString()) {
                    registerValue = new RegisterValue(obisCode, attribute.getVisibleString().getStr());
                } else if (attribute.isOctetString() && attribute.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, attribute.getOctetString().stringValue());
                } else if (attribute.isBitString() && attribute.getBitString() != null) {
                    registerValue = readBitString(obisCode, attribute);
                } else if (attribute.isDateTime()) {
                    registerValue = new RegisterValue(obisCode, attribute.getDateTime().getCalendar(protocol.getTimeZone()).getTime());
                } else {
                    Number value = register.getValueAttr().toBigDecimal();
                    if (value != null) {
                        registerValue = new RegisterValue(obisCode, new Quantity(value, Unit.get("")));
                    } else {
                        return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                    }
                }
            } else if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = protocol.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = new Quantity(register.getValueAttr().toBigDecimal(), register.getScalerUnit().getEisUnit());
                registerValue = new RegisterValue(obisCode, quantity);
            } else if (uo.getClassID() == DLMSClassId.REGISTER_MONITOR.getClassId()) {
                RegisterMonitor registerMonitor = protocol.getDlmsSession().getCosemObjectFactory().getRegisterMonitor(obisCode);
                int value = registerMonitor.readThresholds().getDataType(0).intValue();
                registerValue = new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
                AbstractDataType valueAttr = register.getValueAttr();
                if (valueAttr.getOctetString() != null) {
                    registerValue = new RegisterValue(obisCode, (valueAttr.getOctetString()).stringValue());
                } else {
                    Quantity quantity = new Quantity(valueAttr.toBigDecimal(), register.getScalerUnit().getEisUnit());
                    registerValue = new RegisterValue(obisCode, quantity, register.getCaptureTime());
                }
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                int value = protocol.getDlmsSession().getCosemObjectFactory().getDisconnector(obisCode).getControlState().getValue();
                registerValue = new RegisterValue(obisCode, DisconnectControlState.fromValue(value).getDescription());
            } else if (uo.getClassID() == DLMSClassId.ACCOUNT_SETUP.getClassId()) {
                registerValue = readAccountSetup(obisCode);
            } else if (uo.getClassID() == DLMSClassId.CREDIT_SETUP.getClassId()) {
                CreditSetup creditSetup = protocol.getDlmsSession().getCosemObjectFactory().getCreditSetup(obisCode);
                int amount = creditSetup.readCurrentCreditAmount().getInteger32().intValue();
                registerValue = new RegisterValue(obisCode, new Quantity(amount, Unit.get("")));
            } else if (uo.getClassID() == DLMSClassId.SPECIAL_DAYS_TABLE.getClassId()) {
                SpecialDaysTable specialDaysTable = protocol.getDlmsSession().getCosemObjectFactory().getSpecialDaysTable(obisCode);
                Array specialDays = specialDaysTable.readSpecialDays();
                StringBuffer buff = new StringBuffer("");
                for (int i=0; i < specialDays.nrOfDataTypes(); i++) {
                    Structure special = specialDays.getDataType(i).getStructure();
                    appendSpecialDayString(buff, special);
                }
                registerValue = new RegisterValue(obisCode, buff.toString());
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            }
            return createCollectedRegister(registerValue, offlineRegister);

        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        }
    }

    private void appendSpecialDayString(StringBuffer buff, Structure special)  {
        buff.append(String.valueOf(special.getDataType(0).getUnsigned16().getValue()));
        buff.append(",");
        buff.append(AXDRDate.toDescription(special.getDataType(1).getOctetString()));
        buff.append(",");
        buff.append(String.valueOf(special.getDataType(2).getUnsigned8().getValue()));
        buff.append(";\n");
    }

    protected RegisterValue readStructure(ObisCode obisCode, Structure structure) throws IOException {
        String highThreshold;
        String lowThreshold;
        String description;
        if (obisCode.equals(MONEY_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getInteger32().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getInteger32().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingCreditHighDefaultTranslation, DeviceMessageConstants.remainingCreditLowDefaultTranslation);
        } else if (obisCode.equals(CONSUMPTION_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.consumedCreditHighDefaultTranslation, DeviceMessageConstants.consumedCreditLowDefaultTranslation);
        } else if (obisCode.equals(TIME_CREDIT_THRESHOLD)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned8().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned8().getValue());
            description = formatDescr(highThreshold, lowThreshold, DeviceMessageConstants.remainingTimeHighDefaultTranslation, DeviceMessageConstants.remainingTimeLowDefaultTranslation);
        } else if (obisCode.equals(CREDIT_DAY_LIMIT)) {
            highThreshold = Integer.toString(structure.getDataType(0).getUnsigned16().getValue());
            lowThreshold = Integer.toString(structure.getDataType(1).getUnsigned16().getValue());
            description = formatDescr(highThreshold, lowThreshold, "Days Limit1", "Days Limit2");
        } else if (obisCode.equals(ACTIVE_FIRMWARE)) {
            String model = structure.getDataType(0).getVisibleString().getStr();
            String version = structure.getDataType(1).getVisibleString().getStr();
            String crc = structure.getDataType(2).getVisibleString().getStr();
            description = "Model=" + model + ", Firmware Version=" + version + ", Firmware CRC=" + crc;
        } else if (obisCode.equals(ACTIVE_TAX) || obisCode.equals(PASIVE_TAX)) {
            description = readTax(structure);
        } else if (obisCode.equals(ACTIVE_STEP_TARIFF) || obisCode.equals(PASIVE_STEP_TARIFF)) {
            description = readStepTariff(structure);
        } else if (obisCode.equals(FRIENDLY_DAY_PERIOD)) {
            description = readFriendlyDayPeriod(structure);
        } else
            throw new ProtocolException("Cannot decode the structure data for the obis code: " + obisCode);
        return new RegisterValue(obisCode, description);
    }

    protected RegisterValue readBitString(ObisCode obisCode, AbstractDataType attribute) {
        if (obisCode.equals(FRIENDLY_WEEKDAYS)) {
            return new RegisterValue(obisCode, Integer.toString(attribute.getBitString().intValue(), 2));
        }
        return new RegisterValue(obisCode, new Quantity(attribute.getBitString().toBigDecimal().longValue(), Unit.get("")));
    }

    protected RegisterValue readArray(ObisCode obisCode, Array array) throws IOException {
        throw new ProtocolException("Cannot decode the array data for the obis code: " + obisCode);
    }

    protected String formatDescr(String highThreshold, String lowThreshold, String highDefaultTranslation, String lowDefaultTranslation) {
        return highDefaultTranslation + "=" + highThreshold + ",\n" + lowDefaultTranslation + "=" + lowThreshold + ".";
    }

    protected RegisterValue readAccountSetup(ObisCode obisCode) throws IOException {
        StringBuffer buff = new StringBuffer();
        AccountSetup accountSetup = protocol.getDlmsSession().getCosemObjectFactory().getAccountSetup(obisCode);
        int paymentMode = accountSetup.readPaymentMode().getValue();
        buff.append("PaymentMode = " + ChargeDeviceMessage.ChargeMode.getDescriptionValue(paymentMode) + ", ");

        int accountStatus = accountSetup.readAccountStatus().getValue();
        if (accountStatus == NEW_ACCOUNT)
            buff.append("AccountStatus = New.");
        else if (accountStatus == ACTIVE_ACCOUNT)
            buff.append("AccountStatus = Active.");
        else if (accountStatus == CLOSED_ACCOUNT)
            buff.append("AccountStatus = Closed.");
        else
            buff.append("AccountStatus = Unknown.");

        return new RegisterValue(obisCode, buff.toString());
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

    private String readFriendlyDayPeriod(Structure structure) {
        StringBuffer buff = new StringBuffer();
        Structure start = structure.getDataType(0).getStructure();
        buff.append("Start period: ");
        appendTimeString(buff, start);
        Structure stop = structure.getDataType(1).getStructure();
        buff.append(" Stop period: ");
        appendTimeString(buff, stop);
        buff.append(".");
        return buff.toString();
    }

    private void appendTimeString(StringBuffer buff, Structure structure) {
        buff.append(Integer.toString(structure.getDataType(0).getUnsigned8().getValue()));
        buff.append(":");
        buff.append(Integer.toString(structure.getDataType(1).getUnsigned8().getValue()));
        buff.append(":");
        buff.append(Integer.toString(structure.getDataType(2).getUnsigned8().getValue()));
        buff.append(".");
        buff.append(Integer.toString(structure.getDataType(3).getUnsigned8().getValue()));
    }

    @SuppressWarnings("unchecked")
    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister
                    .setFailureInformation(ResultType.InCompatible,
                            issueFactory.createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": " + errorMessage[0].toString(),
                                    register.getObisCode(),
                                    errorMessage[0]));
        } else {
            collectedRegister
                    .setFailureInformation(ResultType.NotSupported,
                            issueFactory.createWarning(register.getObisCode(),
                                    register.getObisCode().toString() + ": Not Supported",
                                    register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRegister) {
        return new RegisterIdentifierById(offlineRegister.getRegisterId(), offlineRegister.getObisCode(), offlineRegister.getDeviceIdentifier());
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }
}