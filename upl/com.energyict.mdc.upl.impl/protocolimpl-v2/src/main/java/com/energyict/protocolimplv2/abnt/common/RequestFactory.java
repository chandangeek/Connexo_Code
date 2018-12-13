package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.exception.UnknownFunctionCodeParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.ReaderSerialNumber;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureAutomaticDemandResetRequest;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureAutomaticDemandResetResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureDstRequest;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureDstResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureHolidayListRequest;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureHolidayListResponse;
import com.energyict.protocolimplv2.abnt.common.structure.DateModificationRequest;
import com.energyict.protocolimplv2.abnt.common.structure.DateModificationResponse;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogRequest;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.HolidayRecords;
import com.energyict.protocolimplv2.abnt.common.structure.InstrumentationPageResponse;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutRequest;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogRequest;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadInstallationCodeRequest;
import com.energyict.protocolimplv2.abnt.common.structure.ReadInstallationCodeResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersRequest;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadRequest;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadResponse;
import com.energyict.protocolimplv2.abnt.common.structure.TimeModificationRequest;
import com.energyict.protocolimplv2.abnt.common.structure.TimeModificationResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetCondition;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileDataSelector;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileReadSizeArgument;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author sva
 * @since 13/08/2014 - 13:21
 */
public class RequestFactory {

    private static final int NUMBER_OF_MILLIS_IN_5_MIN = 300000;
    private final PropertySpecService propertySpecService;
    private Connection connection;
    private ComChannel comChannel;
    private AbntProperties properties;
    private String meterSerialNumber;
    private ReadParametersResponse defaultParameters;

    public RequestFactory(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public RequestFactory(PropertySpecService propertySpecService, Connection connection, ComChannel comChannel, AbntProperties properties) {
        this(propertySpecService);
        this.connection = connection;
        this.comChannel = comChannel;
        this.properties = properties;
    }

    public void logOn() {
        try {
            ResponseFrame response = sendFrameGetResponse(getDefaultParameterReadRequest());
            this.meterSerialNumber = response.getMeterSerialNumber().getSerialNumber().getText();
            this.defaultParameters = (ReadParametersResponse) response.getData();
        } catch (ParsingException e) {
            throw CommunicationException.protocolConnectFailed(e);
        }
    }

    public ReadParametersResponse readDefaultParameters() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getDefaultParameterReadRequest());
        return (ReadParametersResponse) response.getData();
    }

    public ReadParametersResponse readActualParametersWithDemandReset() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getActualParametersWithDemandResetReadRequest());
        return (ReadParametersResponse) response.getData();
    }

    public ReadParametersResponse readParameters(int channelGroup) throws ParsingException {
        return readParameters(LoadProfileDataSelector.newFullProfileDataSelector(), channelGroup);
    }

    public ReadParametersResponse readParameters(LoadProfileDataSelector dataSelector, int channelGroup) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getParameterReadRequest(dataSelector, channelGroup));
        return (ReadParametersResponse) response.getData();
    }

    public ReadParametersResponse readPreviousParameters(int channelGroup) throws ParsingException {
        return readPreviousParameters(LoadProfileDataSelector.newFullProfileDataSelector(), channelGroup);
    }

    public ReadParametersResponse readPreviousParameters(LoadProfileDataSelector dataSelector, int channelGroup) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getPreviousParameterReadRequest(dataSelector, channelGroup));
        return (ReadParametersResponse) response.getData();
    }

    private RequestFrame getDefaultParameterReadRequest() {
        return getParameterReadRequest(Function.FunctionCode.ACTUAL_PARAMETERS, LoadProfileDataSelector.newFullProfileDataSelector(), 0);
    }

    private RequestFrame getActualParametersWithDemandResetReadRequest() {
        return getParameterReadRequest(Function.FunctionCode.ACTUAL_PARAMETERS_WITH_DEMAND_RESET, LoadProfileDataSelector.newFullProfileDataSelector(), 0);
    }

    private RequestFrame getParameterReadRequest(LoadProfileDataSelector dataSelector, int channelGroup) {
        return getParameterReadRequest(Function.FunctionCode.ACTUAL_PARAMETERS, dataSelector, channelGroup);
    }

    private RequestFrame getPreviousParameterReadRequest(LoadProfileDataSelector dataSelector, int channelGroup) {
        return getParameterReadRequest(Function.FunctionCode.PREVIOUS_PARAMETERS, dataSelector, channelGroup);
    }

    private RequestFrame getParameterReadRequest(Function.FunctionCode functionCode, LoadProfileDataSelector dataSelector, int channelGroup) {
        ReadParametersRequest requestData = new ReadParametersRequest(getTimeZone());
        requestData.setLoadProfileBlockCount(dataSelector.getBlockCount());
        requestData.setChannelGroupSelection(channelGroup);
        requestData.setLoadProfileReadSizeArgument(new LoadProfileReadSizeArgument(dataSelector.getReadSizeArgument()));

        RequestFrame request = getBasicRequestFrame(functionCode);
        request.setData(requestData);
        return request;
    }

    public RegisterReadResponse readActualRegisters(int channelGroup) throws ParsingException {
        // Read out the parameters for the appropriate channel group
        RequestFrame parameterReadRequest = getParameterReadRequest(Function.FunctionCode.ACTUAL_PARAMETERS, LoadProfileDataSelector.newFullProfileDataSelector(), channelGroup - 1);
        sendFrameGetResponse(parameterReadRequest);

        // Read out the registers
        ResponseFrame response = sendFrameGetResponse(getRegisterReadRequest(true));
        return (RegisterReadResponse) response.getData();
    }

    public RegisterReadResponse readBillingRegisters(int channelGroup) throws ParsingException {
        // Read out the parameters for the appropriate channel group
        RequestFrame parameterReadRequest = getParameterReadRequest(Function.FunctionCode.PREVIOUS_PARAMETERS, LoadProfileDataSelector.newFullProfileDataSelector(), channelGroup - 1);
        sendFrameGetResponse(parameterReadRequest);

        // Read out the registers
        ResponseFrame response = sendFrameGetResponse(getRegisterReadRequest(false));
        return (RegisterReadResponse) response.getData();
    }

    private RequestFrame getRegisterReadRequest(boolean currentRegisters) {
        RequestFrame request = getBasicRequestFrame(
                currentRegisters
                        ? Function.FunctionCode.CURRENT_REGISTERS
                        : Function.FunctionCode.PREVIOUS_REGISTERS
        );
        request.setData(new RegisterReadRequest(getTimeZone()));
        return request;
    }

    public InstrumentationPageResponse readInstrumentationPage() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getInstrumentationPageRequest());
        return (InstrumentationPageResponse) response.getData();
    }

    private RequestFrame getInstrumentationPageRequest() {
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.INSTRUMENTATION_PAGE);
        request.setData(new RegisterReadRequest(getTimeZone()));
        return request;
    }

    public PowerFailLogResponse readPowerFailLog() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getPowerFailLogRequest());
        return (PowerFailLogResponse) response.getData();
    }

    private RequestFrame getPowerFailLogRequest() {
        PowerFailLogRequest requestData = new PowerFailLogRequest(getTimeZone());
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.POWER_FAIL_LOG);
        request.setData(requestData);
        return request;
    }

    public HistoryLogResponse readHistoryLog() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getHistoryLogRequest());
        return (HistoryLogResponse) response.getData();
    }

    private RequestFrame getHistoryLogRequest() {
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.HISTORY_LOG);
        request.setData(new HistoryLogRequest(getTimeZone()));
        return request;
    }

    public LoadProfileReadoutResponse readCurrentBillingLoadProfileData(int maxNrOfSegments) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getCurrentBillingLoadProfileReadoutRequest(), maxNrOfSegments);
        return (LoadProfileReadoutResponse) response.getData();
    }

    private RequestFrame getCurrentBillingLoadProfileReadoutRequest() {
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.LP_OF_CURRENT_BILLING);
        request.setData(new LoadProfileReadoutRequest(getTimeZone()));
        return request;
    }

    public LoadProfileReadoutResponse readPreviousBillingLoadProfileData(int maxNrOfSegments) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getPreviousBillingLoadProfileReadoutRequest(), maxNrOfSegments);
        return (LoadProfileReadoutResponse) response.getData();
    }

    private RequestFrame getPreviousBillingLoadProfileReadoutRequest() {
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.LP_OF_PREVIOUS_BILLING);
        request.setData(new LoadProfileReadoutRequest(getTimeZone()));
        return request;
    }

    public ConfigureHolidayListResponse configureHolidays(HolidayRecords holidayRecords) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getConfigureHolidayListRequest(holidayRecords));
        return (ConfigureHolidayListResponse) response.getData();
    }

    private RequestFrame getConfigureHolidayListRequest(HolidayRecords holidayRecords) {
        ConfigureHolidayListRequest configureHolidayListRequest = new ConfigureHolidayListRequest(getTimeZone());
        configureHolidayListRequest.setHolidayRecords(holidayRecords);

        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.CONFIGURE_HOLIDAY_LIST);
        request.setData(configureHolidayListRequest);
        return request;
    }

    public ConfigureDstResponse configureDstParameters(DstConfigurationRecord dstConfigurationRecord) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getConfigureDstParametersRequest(dstConfigurationRecord));
        return (ConfigureDstResponse) response.getData();
    }

    private RequestFrame getConfigureDstParametersRequest(DstConfigurationRecord dstConfigurationRecord) {
        ConfigureDstRequest configureDstRequest = new ConfigureDstRequest(getTimeZone());
        configureDstRequest.setDstConfigurationRecord(dstConfigurationRecord);

        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.CONFIGURE_DST);
        request.setData(configureDstRequest);
        return request;
    }

    public ConfigureAutomaticDemandResetResponse configureAutomaticDemandReset(AutomaticDemandResetConfigurationRecord automaticDemandResetConfigurationRecord) throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getConfigureAutomaticDemandResetRequest(automaticDemandResetConfigurationRecord));
        return (ConfigureAutomaticDemandResetResponse) response.getData();
    }

    private RequestFrame getConfigureAutomaticDemandResetRequest(AutomaticDemandResetConfigurationRecord automaticDemandResetRecord) {
        ConfigureAutomaticDemandResetRequest configureAutomaticDemandResetRequest = new ConfigureAutomaticDemandResetRequest(getTimeZone());
        configureAutomaticDemandResetRequest.setAutomaticDemandResetConfigurationRecord(automaticDemandResetRecord);

        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.CONFIGURE_AUTOMATIC_DEMAND_RESET);
        request.setData(configureAutomaticDemandResetRequest);
        return request;
    }

    public ReadInstallationCodeResponse readInstallationCode() throws ParsingException {
        ResponseFrame response = sendFrameGetResponse(getReadInstallationCodeRequest());
        return (ReadInstallationCodeResponse) response.getData();
    }

    private RequestFrame getReadInstallationCodeRequest() {
        ReadInstallationCodeRequest readInstallationCodeRequest = new ReadInstallationCodeRequest(getTimeZone());
        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.READ_INSTALLATION_CODE);
        request.setData(readInstallationCodeRequest);
        return request;
    }

    public void setTime(Date timeToSet) throws AbntException {
        AutomaticDemandResetConfigurationRecord demandResetConfigurationRecord = new AutomaticDemandResetConfigurationRecord();
        demandResetConfigurationRecord.setDemandResetCondition((AutomaticDemandResetCondition) getDefaultParameters().getField(ReadParameterFields.automaticDemandResetCondition));
        demandResetConfigurationRecord.setDayOfDemandReset((BcdEncodedField) getDefaultParameters().getField(ReadParameterFields.automaticDemandResetDay));
        demandResetConfigurationRecord.setHourOfDemandReset((BcdEncodedField) getDefaultParameters().getField(ReadParameterFields.automaticDemandResetHour));

        Calendar calendar = getNextOccurrenceOfAutomaticDemandReset(demandResetConfigurationRecord);
        int timeShift = (int) (timeToSet.getTime() - new Date().getTime());
        if (Math.abs(timeShift) >= NUMBER_OF_MILLIS_IN_5_MIN) { // Only program date/time change if time difference exceeds 5 min.
            Calendar shiftedCal = (Calendar) calendar.clone();
            shiftedCal.add(Calendar.MILLISECOND, (timeShift - timeShift % NUMBER_OF_MILLIS_IN_5_MIN));    // And round down the timeShift to a multiple of 5 min

            if (shiftedCal.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) {
                changeDate(shiftedCal);   // Only change the date if necessary
            }
            changeTime(shiftedCal);
        }
    }

    public void forceTime() throws AbntException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        Calendar shiftedCal = Calendar.getInstance(getTimeZone());
        // Round down calendar to 5 min
        shiftedCal.setTimeInMillis(calendar.getTimeInMillis() - calendar.getTimeInMillis() % NUMBER_OF_MILLIS_IN_5_MIN);

        if (shiftedCal.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) {
            changeDate(shiftedCal);   // Only change the date if necessary
        }
        changeTime(shiftedCal);
    }

    private DateModificationResponse changeDate(Calendar calendar) throws AbntException {
        ResponseFrame response = sendFrameGetResponse(getDateModificationRequest(calendar));
        return (DateModificationResponse) response.getData();
    }

    private RequestFrame getDateModificationRequest(Calendar calendar) {
        DateModificationRequest dateModificationRequest = new DateModificationRequest(getTimeZone());
        dateModificationRequest.applyCalendar(calendar);

        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.DATE_CHANGE);
        request.setData(dateModificationRequest);
        return request;
    }

    private TimeModificationResponse changeTime(Calendar calendar) throws AbntException {
        ResponseFrame response = sendFrameGetResponse(getTimeModificationRequest(calendar));
        return (TimeModificationResponse) response.getData();
    }

    private RequestFrame getTimeModificationRequest(Calendar calendar) {
        TimeModificationRequest timeModificationRequest = new TimeModificationRequest(getTimeZone());
        timeModificationRequest.applyCalendar(calendar);

        RequestFrame request = getBasicRequestFrame(Function.FunctionCode.TIME_CHANGE);
        request.setData(timeModificationRequest);
        return request;
    }

    private Calendar getNextOccurrenceOfAutomaticDemandReset(AutomaticDemandResetConfigurationRecord demandResetConfig) throws AbntException {
        Calendar currentTimeCal = Calendar.getInstance(getTimeZone());
        Calendar nextBillingResetCal = Calendar.getInstance(getTimeZone());

        if (demandResetConfig.getDemandResetCondition().getDemandResetCondition().equals(AutomaticDemandResetCondition.ResetCondition.DISABLED)) {
            throw new AbntException("Automatic demand reset is not enabled - therefore date/time set cannot be programmed");
        }

        nextBillingResetCal.set(Calendar.HOUR_OF_DAY, (int) demandResetConfig.getHourOfDemandReset().getValue());
        nextBillingResetCal.set(Calendar.DAY_OF_MONTH, (int) demandResetConfig.getHourOfDemandReset().getValue());
        nextBillingResetCal.set(Calendar.MINUTE, 0);
        nextBillingResetCal.set(Calendar.SECOND, 0);
        nextBillingResetCal.set(Calendar.MILLISECOND, 0);

        if (nextBillingResetCal.before(currentTimeCal)) {   // If date is in past, then go to next month
            nextBillingResetCal.set(Calendar.MONTH, nextBillingResetCal.get(Calendar.MONTH) + 1);
        }

        return nextBillingResetCal;
    }

    private RequestFrame getBasicRequestFrame(Function.FunctionCode functionCode) {
        RequestFrame request = new RequestFrame(getTimeZone());
        request.setFunction(Function.fromFunctionCode(functionCode));
        request.setReaderSerialNumber(getReaderSerialNumber());
        return request;
    }

    private ResponseFrame sendFrameGetResponse(RequestFrame requestFrame) throws ParsingException {
        ResponseFrame response = getConnection().sendFrameGetResponse(requestFrame);
        try {
            response.doParseData();    // Parsing of the frame data content is done here
            return response;
        } catch (UnknownFunctionCodeParsingException e) {
            throw CommunicationException.unexpectedResponse(e);
        }
    }

    private ResponseFrame sendFrameGetResponse(RequestFrame requestFrame, int maxNrOfSegments) throws ParsingException {
        ResponseFrame response = getConnection().sendFrameGetResponse(requestFrame, maxNrOfSegments);
        try {
            response.doParseData();    // Parsing of the frame data content is done here
            return response;
        } catch (UnknownFunctionCodeParsingException e) {
            throw CommunicationException.unexpectedResponse(e);
        }
    }

    public Connection getConnection() {
        if (this.connection == null) {
            if (comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
                this.connection = new OpticalConnection(getComChannel(), getProperties());
            } else if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.SocketComChannel) { // Serial ComChannel or a transparent socket ComChannel
                this.connection = new SerialConnection(getComChannel(), getProperties());
            } else {
                throw DeviceConfigurationException.unexpectedComChannel(ComChannelType.SerialComChannel.name() + ", " + ComChannelType.OpticalComChannel.name(), comChannel.getClass().getSimpleName());
            }
        }
        return connection;
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public void setComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    public AbntProperties getProperties() {
        if (properties == null) {
            properties = new AbntProperties(this.propertySpecService);
        }
        return properties;
    }

    public void setProperties(AbntProperties properties) {
        this.properties = properties;
    }

    public TimeZone getTimeZone() {
        return getProperties().getTimeZone();
    }

    public ReaderSerialNumber getReaderSerialNumber() {
        int serialNumber = getProperties().getReaderSerialNumber();
        return new ReaderSerialNumber(serialNumber);
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber.trim().replaceFirst("^0+(?!$)", "");
    }

    public ReadParametersResponse getDefaultParameters() {
        return defaultParameters;
    }
}