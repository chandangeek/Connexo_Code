package com.energyict.protocolimplv2.abnt.common;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.field.FloatField;
import com.energyict.protocolimplv2.abnt.common.structure.ChannelGroup;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.HolidayRecords;
import com.energyict.protocolimplv2.abnt.common.structure.InstrumentationPageFields;
import com.energyict.protocolimplv2.abnt.common.structure.InstrumentationPageResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadInstallationCodeResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadFields;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetCondition;
import com.energyict.protocolimplv2.abnt.common.structure.field.BatteryStatusField;
import com.energyict.protocolimplv2.abnt.common.structure.field.ConnectionTypeField;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.QuantityConversionIndicatorField;
import com.energyict.protocolimplv2.abnt.common.structure.field.ReactivePowerCharacteristicField;
import com.energyict.protocolimplv2.abnt.common.structure.field.SoftwareVersionField;
import com.energyict.protocolimplv2.abnt.common.structure.field.TariffConfigurationField;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitField;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 2/09/2014 - 14:09
 */
public class RegisterFactory implements DeviceRegisterSupport {

    private static final ObisCode PARAMETER_READ_BASE_OBIS = ObisCode.fromString("0.0.96.0.0.255");
    private static final ObisCode REGISTER_READ_GROUP_1_BASE_OBIS = ObisCode.fromString("1.0.0.8.0.255");
    private static final ObisCode REGISTER_READ_GROUP_2_BASE_OBIS = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode INSTRUMENTATION_PAGE_BASE_OBIS = ObisCode.fromString("1.0.4.8.0.255");
    private static final ObisCode SERIAL_NUMBER_OBIS = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode INSTALLATION_CODE_OBIS = ObisCode.fromString("0.0.96.2.0.255");
    private static final ObisCode INSTALLATION_CODE_HEX_OBIS = ObisCode.fromString("0.1.96.2.0.255");
    private static final ObisCode HISTORY_LOG_OBIS = ObisCode.fromString("0.0.99.98.128.255");
    private static final ObisCode POWER_FAILURE_LOG_OBIS = ObisCode.fromString("1.0.99.97.128.255");
    private final AbstractAbntProtocol meterProtocol;
    private Map<Integer, ReadParametersResponse> actualParametersMap;
    private Map<Integer, ReadParametersResponse> previousParametersMap;
    private Map<Integer, RegisterReadResponse> actualRegistersMap;
    private Map<Integer, RegisterReadResponse> billingRegistersMap;
    private HistoryLogResponse historyLog;
    private PowerFailLogResponse powerFailLog;
    private ReadInstallationCodeResponse installationCodeResponse;
    private InstrumentationPageResponse instrumentationPage;

    public RegisterFactory(AbstractAbntProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(registers.size());
        for (OfflineRegister register : registers) {
            CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
            readRegister(register, collectedRegister);
            collectedRegisters.add(collectedRegister);
        }

        return collectedRegisters;
    }

    private void readRegister(OfflineRegister register, CollectedRegister collectedRegister) {
        collectedRegister.setReadTime(new Date());

        try {
            if (registerIsOfType(register, PARAMETER_READ_BASE_OBIS)) { //A. Registers of Parameter read
                readParameterReadRegisters(register, collectedRegister);
            } else if (registerIsOfType(register, REGISTER_READ_GROUP_1_BASE_OBIS)) { // B. Registers of Register read (channel group 1)
                readRegisterReadRegisters(register, collectedRegister);
            } else if (registerIsOfType(register, REGISTER_READ_GROUP_2_BASE_OBIS)) { // C. Registers of Register read (channel group 2)
                readRegisterReadRegisters(register, collectedRegister);
            } else if (registerIsOfType(register, INSTRUMENTATION_PAGE_BASE_OBIS)) {    // D. Registers of Instrumentation page
                readInstrumentationPageRegister(register, collectedRegister);
            } else {    // E. Other special registers
                readSpecialRegister(register, collectedRegister);
            }

            if (registerNotYetCollected(collectedRegister)) { // If still not collected, then set unsupported
                registerNotSupported(register, collectedRegister);
            }
        } catch (ParsingException e) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createProblem(collectedRegister, "CouldNotParseRegisterData", e.getMessage()));
        }
    }

    private boolean registerIsOfType(OfflineRegister register, ObisCode baseObisCode) {
        ObisCode obisCode = register.getObisCode();
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0);
        obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 0);
        return obisCode.equalsIgnoreBillingField(baseObisCode);
    }

    private void readParameterReadRegisters(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        int channelGroup = register.getObisCode().getB();
        boolean billingRegisters = register.getObisCode().getF() != 255;
        if (register.getObisCode().getE() == 0x80) {
            setCollectedData(collectedRegister, getParameters(channelGroup, billingRegisters).getBytes());
            return;
        }

        ReadParameterFields field = ReadParameterFields.fromCode(register.getObisCode().getE());
        if (field != null) {
            setCollectedData(register, collectedRegister, getParameters(channelGroup, billingRegisters).getField(field));
        } else {
            registerNotSupported(register, collectedRegister);
        }
    }

    private void readRegisterReadRegisters(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        int channelGroup = register.getObisCode().getC();
        boolean billingRegisters = register.getObisCode().getF() != 255;
        if (register.getObisCode().getE() == 0x80) {
            getParameters(channelGroup, billingRegisters);
            setCollectedData(collectedRegister, getRegisters(channelGroup, billingRegisters).getBytes());
            return;
        }

        ReadParametersResponse parameters = getParameters(channelGroup, billingRegisters);
        RegisterReadResponse registers = getRegisters(channelGroup, billingRegisters);

        RegisterReadFields field = RegisterReadFields.fromCode(register.getObisCode().getE());
        if (field != null) {
            BcdEncodedField value = (BcdEncodedField) registers.getField(field);
            double val = (double) value.getValue() * getRegisterNumerator(parameters, field.getChannelGroup()) / getRegisterDenominator(parameters, field.getChannelGroup());
            Quantity quantity = new Quantity(
                    new BigDecimal(val),
                    getRegisterUnit(parameters, field.getChannelGroup())
            );
            setCollectedData(collectedRegister, quantity);
        } else {
            registerNotSupported(register, collectedRegister);
        }
    }

    private int getRegisterNumerator(ReadParametersResponse parameters, ChannelGroup channelGroup) throws ParsingException {
        switch (channelGroup) {
            case GROUP_1_ENERGY:
            case GROUP_1_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn1)).getValue();
            case GROUP_2_ENERGY:
            case GROUP_2_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn2)).getValue();
            case GROUP_3_ENERGY:
            case GROUP_3_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.numeratorChn3)).getValue();
            default:
                throw MdcManager.getComServerExceptionFactory().createUnrecognizedEnumValueError(channelGroup);
        }
    }

    private int getRegisterDenominator(ReadParametersResponse parameters, ChannelGroup channelGroup) throws ParsingException {
        switch (channelGroup) {
            case GROUP_1_ENERGY:
            case GROUP_1_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn1)).getValue();
            case GROUP_2_ENERGY:
            case GROUP_2_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn2)).getValue();
            case GROUP_3_ENERGY:
            case GROUP_3_DEMAND:
                return (int) ((BcdEncodedField) parameters.getField(ReadParameterFields.denominatorChn3)).getValue();
            default:
                throw MdcManager.getComServerExceptionFactory().createUnrecognizedEnumValueError(channelGroup);
        }
    }

    private Unit getRegisterUnit(ReadParametersResponse parameters, ChannelGroup channelGroup) {
        switch (channelGroup) {
            case GROUP_1_ENERGY:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn1)).getEisUnit();
            case GROUP_1_DEMAND:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn1)).getEisUnit().getFlowUnit();
            case GROUP_2_ENERGY:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn2)).getEisUnit();
            case GROUP_2_DEMAND:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn2)).getEisUnit().getFlowUnit();
            case GROUP_3_ENERGY:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn3)).getEisUnit();
            case GROUP_3_DEMAND:
                return ((UnitField) parameters.getField(ReadParameterFields.unitChn2)).getEisUnit().getFlowUnit();
            default:
                throw MdcManager.getComServerExceptionFactory().createUnrecognizedEnumValueError(channelGroup);
        }
    }

    private void readInstrumentationPageRegister(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        if (register.getObisCode().getE() == 0x80) {
            setCollectedData(collectedRegister, getInstrumentationPage().getBytes());
            return;
        }

        InstrumentationPageFields field = InstrumentationPageFields.fromCode(register.getObisCode().getE());
        if (field != null) {
            setCollectedData(register, collectedRegister, getInstrumentationPage().getField(field));
        } else {
            registerNotSupported(register, collectedRegister);
        }
    }

    private void readPowerFailureLog(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        setCollectedData(collectedRegister, getPowerFailLog().getBytes());
    }

    private void readHistoryLog(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        setCollectedData(collectedRegister, getHistoryLog().getBytes());
    }

    private void readSpecialRegister(OfflineRegister register, CollectedRegister collectedRegister) throws ParsingException {
        if (register.getObisCode().equals(SERIAL_NUMBER_OBIS)) {
            setCollectedText(collectedRegister, getMeterProtocol().getSerialNumber());
        } else if (register.getObisCode().equals(INSTALLATION_CODE_OBIS)) {
            setCollectedText(collectedRegister, getInstallationCode().getInstallationCode());
        } else if (register.getObisCode().equals(INSTALLATION_CODE_HEX_OBIS)) {
            setCollectedText(collectedRegister, getInstallationCode().getInstallationCodeAsHex());
        } else if (register.getObisCode().equals(POWER_FAILURE_LOG_OBIS)) {
            readPowerFailureLog(register, collectedRegister);
        } else if (register.getObisCode().equals(HISTORY_LOG_OBIS)) {
            readHistoryLog(register, collectedRegister);
        }
    }

    private void setCollectedData(OfflineRegister register, CollectedRegister collectedRegister, AbstractField abstractField) throws ParsingException {
        if (abstractField instanceof FloatField) {
            setCollectedData(collectedRegister, (FloatField) abstractField);
        } else if (abstractField instanceof DateTimeField) {
            setCollectedData(collectedRegister, ((DateTimeField) abstractField).getDate(getMeterProtocol().getTimeZone()));
        } else if (abstractField instanceof ReactivePowerCharacteristicField) {
            setCollectedData(
                    collectedRegister,
                    ((ReactivePowerCharacteristicField) abstractField).getReactiveCharacteristicsCode(),
                    ((ReactivePowerCharacteristicField) abstractField).getReactivePowerCharacteristicsInfo()
            );
        } else if (abstractField instanceof ConnectionTypeField) {
            setCollectedData(
                    collectedRegister,
                    ((ConnectionTypeField) abstractField).getConnectionTypeCode(),
                    ((ConnectionTypeField) abstractField).getConnectionTypeInfo()
            );
        } else if (abstractField instanceof QuantityConversionIndicatorField) {
            setCollectedData(
                    collectedRegister,
                    ((QuantityConversionIndicatorField) abstractField).getQuantityConversionIndicatorCode(),
                    ((QuantityConversionIndicatorField) abstractField).getQuantityConversionIndicatorInfo()
            );
        } else if (abstractField instanceof SoftwareVersionField) {
            setCollectedText(collectedRegister, ((SoftwareVersionField) abstractField).getSoftwareVersion());
        } else if (abstractField instanceof BatteryStatusField) {
            setCollectedData(
                    collectedRegister,
                    ((BatteryStatusField) abstractField).getStatusCode(),
                    ((BatteryStatusField) abstractField).getBatteryStatusMessage()
            );
        } else if (abstractField instanceof TariffConfigurationField) {
            setCollectedText(collectedRegister, ((TariffConfigurationField) abstractField).getTariffInfo());
        } else if (abstractField instanceof HolidayRecords) {
            setCollectedText(collectedRegister, ((HolidayRecords) abstractField).getAllHolidaysAsText(getMeterProtocol().getTimeZone()));
        } else if (abstractField instanceof AutomaticDemandResetCondition) {
            setCollectedText(collectedRegister, ((AutomaticDemandResetCondition) abstractField).getDemandResetConditionMessage());
        } else if (abstractField instanceof DstConfigurationRecord) {
            setCollectedText(collectedRegister, ((DstConfigurationRecord) abstractField).getDstConfigurationInfo());
        } else if (abstractField instanceof BcdEncodedField) {
            setCollectedData(collectedRegister, (BcdEncodedField) abstractField);
        } else {
            registerNotSupported(register, collectedRegister);
        }
    }

    private void setCollectedData(CollectedRegister collectedRegister, FloatField floatField) {
        collectedRegister.setCollectedData(
                new Quantity(
                        new BigDecimal(floatField.getValue()),
                        floatField.getUnit()
                )
        );
    }

    private void setCollectedData(CollectedRegister collectedRegister, BcdEncodedField bcdEncodedField) throws ParsingException {
        collectedRegister.setCollectedData(
                new Quantity(
                        new BigDecimal(bcdEncodedField.getValue()),
                        Unit.getUndefined()
                )
        );
    }

    private void setCollectedData(CollectedRegister collectedRegister, Quantity quantity) {
        collectedRegister.setCollectedData(quantity);
    }

    private void setCollectedData(CollectedRegister collectedRegister, Date date) {
        collectedRegister.setCollectedData(
                new Quantity(new BigDecimal(date.getTime()), Unit.getUndefined()),
                date.toString()
        );
    }

    private void setCollectedData(CollectedRegister collectedRegister, int value, String message) {
        collectedRegister.setCollectedData(
                new Quantity(new BigDecimal(value), Unit.getUndefined()),
                message
        );
    }

    private void setCollectedData(CollectedRegister collectedRegister, byte[] bytes) {
        collectedRegister.setCollectedData(
                ProtocolTools.getHexStringFromBytes(bytes, "")
        );
    }

    private void setCollectedText(CollectedRegister collectedRegister, String text) {
        collectedRegister.setCollectedData(text);
    }

    private void registerNotSupported(OfflineRegister register, CollectedRegister collectedRegister) {
        collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register, "registerXnotsupported", register.getObisCode()));
    }

    private boolean registerNotYetCollected(CollectedRegister collectedRegister) {
        return collectedRegister.getCollectedQuantity() == null && collectedRegister.getText() == null;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }


    public ReadParametersResponse getParameters(int channelGroup, boolean billingRegisters) throws ParsingException {
        if (billingRegisters) {
            return getBillingParameters(channelGroup);
        } else {
            return getActualParameters(channelGroup);
        }
    }

    private ReadParametersResponse getActualParameters(int channelGroup) throws ParsingException {
        if (!getActualParametersMap().containsKey(channelGroup)) {
            getActualParametersMap().put(channelGroup, getRequestFactory().readParameters(channelGroup));
        }
        return getActualParametersMap().get(channelGroup);
    }

    private ReadParametersResponse getBillingParameters(int channelGroup) throws ParsingException {
        if (!getPreviousParametersMap().containsKey(channelGroup)) {
            getPreviousParametersMap().put(channelGroup, getRequestFactory().readPreviousParameters(channelGroup));
        }
        return getPreviousParametersMap().get(channelGroup);
    }

    public Map<Integer, ReadParametersResponse> getActualParametersMap() {
        if (this.actualParametersMap == null) {
            this.actualParametersMap = new HashMap<>();
        }
        return this.actualParametersMap;
    }

    public Map<Integer, ReadParametersResponse> getPreviousParametersMap() {
        if (this.previousParametersMap == null) {
            this.previousParametersMap = new HashMap<>();
        }
        return this.previousParametersMap;
    }

    public RegisterReadResponse getRegisters(int channelGroup, boolean billingRegisters) throws ParsingException {
        if (billingRegisters) {
            return getBillingRegisters(channelGroup);
        } else {
            return getActualRegisters(channelGroup);
        }
    }

    private RegisterReadResponse getActualRegisters(int channelGroup) throws ParsingException {
        if (!getActualRegistersMap().containsKey(channelGroup)) {
            getActualRegistersMap().put(channelGroup, getRequestFactory().readActualRegisters(channelGroup));
        }
        return getActualRegistersMap().get(channelGroup);
    }

    private RegisterReadResponse getBillingRegisters(int channelGroup) throws ParsingException {
        if (!getBillingRegistersMap().containsKey(channelGroup)) {
            getBillingRegistersMap().put(channelGroup, getRequestFactory().readBillingRegisters(channelGroup));
        }
        return getBillingRegistersMap().get(channelGroup);
    }

    private Map<Integer, RegisterReadResponse> getActualRegistersMap() {
        if (this.actualRegistersMap == null) {
            this.actualRegistersMap = new HashMap<>();
        }
        return this.actualRegistersMap;
    }

    private Map<Integer, RegisterReadResponse> getBillingRegistersMap() {
        if (this.billingRegistersMap == null) {
            this.billingRegistersMap = new HashMap<>();
        }
        return this.billingRegistersMap;
    }

    public InstrumentationPageResponse getInstrumentationPage() throws ParsingException {
        if (this.instrumentationPage == null) {
            this.instrumentationPage = getRequestFactory().readInstrumentationPage();
        }
        return this.instrumentationPage;
    }

    public PowerFailLogResponse getPowerFailLog() throws ParsingException {
        if (this.powerFailLog == null) {
            this.powerFailLog = getRequestFactory().readPowerFailLog();
        }
        return this.powerFailLog;
    }

    public HistoryLogResponse getHistoryLog() throws ParsingException {
        if (this.historyLog == null) {
            this.historyLog = getRequestFactory().readHistoryLog();
        }
        return this.historyLog;
    }

    public ReadInstallationCodeResponse getInstallationCode() throws ParsingException {
        if (this.installationCodeResponse == null) {
            this.installationCodeResponse = getRequestFactory().readInstallationCode();
        }
        return this.installationCodeResponse;
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }
}