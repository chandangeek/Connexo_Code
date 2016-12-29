package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.properties.UplToMdwPropertySpecAdapter;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.HolidayRecords;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParameterFields;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetCondition;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstEnablementStatus;
import com.energyict.protocolimplv2.abnt.common.structure.field.EventField;
import com.energyict.protocolimplv2.abnt.common.structure.field.HistoryLogRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.HolidayRecord;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import javax.xml.parsers.ParserConfigurationException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.EndOfDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.StartOfDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.day;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableAutomaticDemandResetAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.hour;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

/**
 * @author sva
 * @since 16/09/2014 - 15:42
 */
public class MessageFactory implements DeviceMessageSupport {

    private static final int NUMBER_OF_MILLIS_PER_MIN = 60 * 1000;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy");

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final AbstractAbntProtocol meterProtocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final Converter converter;
    private final NlsService nlsService;
    private final PropertySpecService propertySpecService;
    private final Extractor extractor;

    public MessageFactory(AbstractAbntProtocol meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        this.meterProtocol = meterProtocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.converter = converter;
        this.nlsService = nlsService;
        this.propertySpecService = propertySpecService;
        this.extractor = extractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    DeviceActionMessage.DEMAND_RESET.get(this.propertySpecService, this.nlsService, this.converter),
                    DeviceActionMessage.DemandResetWithForceClock.get(this.propertySpecService, this.nlsService, this.converter),
                    ConfigurationChangeDeviceMessage.ConfigureAutomaticDemandReset.get(this.propertySpecService, this.nlsService, this.converter),
                    ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND.get(this.propertySpecService, this.nlsService, this.converter),
                    ClockDeviceMessage.EnableOrDisableDST.get(this.propertySpecService, this.nlsService, this.converter),
                    ClockDeviceMessage.ConfigureDSTWithoutHour.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList collectedMessages = this.collectedDataFactory.createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(DeviceActionMessage.DEMAND_RESET)) {
                    demandReset();
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.DemandResetWithForceClock)) {
                    demandResetWithForceClock();
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureAutomaticDemandReset)) {
                    configureAutomaticDemandReset(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
                    configureHolidayList(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.ConfigureDSTWithoutHour)) {
                    configureDST(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    messageNotSupported(collectedMessage, pendingMessage);
                }
            } catch (AbntException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                messageFailed(collectedMessage, pendingMessage, e.getMessage());
            }
            collectedMessages.addCollectedMessage(collectedMessage);
        }
        return collectedMessages;
    }

    private void demandReset() throws AbntException {
        if (allowedToDoDemandReset()) {
            doDemandReset();
        } else {
            throw new AbntException("Not allowed to execute a manual billing reset, cause there is a date/time change pending. Only manual billing reset with force clock is allowed at this point.");
        }
    }

    /**
     * Check if it is allowed to execute a manual billing reset
     * If there are date/time changes pending to be executed the next automatic billing reset,
     * then it is <b>not</b> allowed to execute a manual billing reset in between.
     *
     * @return true if demand reset is allowed
     * @throws ParsingException
     */
    private boolean allowedToDoDemandReset() throws ParsingException {
        DateTimeField dateTimeOfLastDemandResetField = (DateTimeField) getMeterProtocol().getRequestFactory().getDefaultParameters().getField(ReadParameterFields.dateTimeOfLastDemandReset);
        Date dateOfLastDemandReset = dateTimeOfLastDemandResetField.getDate(getMeterProtocol().getTimeZone());
        HistoryLogResponse historyLog = getMeterProtocol().getRequestFactory().readHistoryLog();
        for (HistoryLogRecord historyLogRecord : historyLog.getEventLog()) {
            if (historyLogRecord.getEvent().getEvent().equals(EventField.Event.COMMAND_29) ||
                    historyLogRecord.getEvent().getEvent().equals(EventField.Event.COMMAND_30)) {
                if (historyLogRecord.getEventDate().getDate(getMeterProtocol().getTimeZone()).after(dateOfLastDemandReset)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void demandResetWithForceClock() throws AbntException {
        getMeterProtocol().getRequestFactory().forceTime();
        doDemandReset();
    }

    private void doDemandReset() throws ParsingException {
        getMeterProtocol().getRequestFactory().readActualParametersWithDemandReset();
    }

    private void configureAutomaticDemandReset(OfflineDeviceMessage pendingMessage) throws ParsingException {
        boolean enableAutomaticDemandReset = ProtocolTools.getBooleanFromString(
                getDeviceMessageAttributeValue(pendingMessage, enableAutomaticDemandResetAttributeName)
        );

        AutomaticDemandResetConfigurationRecord configurationRecord = new AutomaticDemandResetConfigurationRecord();
        configurationRecord.setDemandResetCondition(AutomaticDemandResetCondition.fromConditionCode(enableAutomaticDemandReset ? 1 : 0));
        configurationRecord.setDayOfDemandReset(new BcdEncodedField(getDeviceMessageAttributeValue(pendingMessage, day)));
        configurationRecord.setHourOfDemandReset(new BcdEncodedField(getDeviceMessageAttributeValue(pendingMessage, hour)));

        getMeterProtocol().getRequestFactory().configureAutomaticDemandReset(configurationRecord);
    }

    private void configureHolidayList(OfflineDeviceMessage pendingMessage) throws AbntException {
        AbntActivityCalendarXmlParser activityCalendarXmlParser = new AbntActivityCalendarXmlParser();
        activityCalendarXmlParser.parseContent(getDeviceMessageAttributeValue(pendingMessage, specialDaysCodeTableAttributeName));

        HolidayRecords holidayRecords = new HolidayRecords();
        for (String holiday : activityCalendarXmlParser.getSpecialDays()) {
            holidayRecords.addHolidayRecord(new HolidayRecord(holiday));
        }

        getMeterProtocol().getRequestFactory().configureHolidays(holidayRecords);
    }

    private void configureDST(OfflineDeviceMessage pendingMessage) throws AbntException {
        DstConfigurationRecord dstConfigurationRecord = new DstConfigurationRecord();
        boolean enableDST = ProtocolTools.getBooleanFromString(getDeviceMessageAttributeValue(pendingMessage, enableDSTAttributeName));
        dstConfigurationRecord.setDstEnablementStatus(DstEnablementStatus.fromStatusCode(enableDST ? 1 : 0));

        if (enableDST) {
            Calendar startOfDstCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            startOfDstCal.setTimeInMillis(Long.parseLong(getDeviceMessageAttributeValue(pendingMessage, StartOfDSTAttributeName)));
            dstConfigurationRecord.setDayOfStartOfDst(new BcdEncodedField(String.valueOf(startOfDstCal.get(Calendar.DAY_OF_MONTH))));
            dstConfigurationRecord.setMonthOfStartOfDst(new BcdEncodedField(String.valueOf(startOfDstCal.get(Calendar.MONTH) + 1)));   // Java Calendar month is 0-based

            Calendar endOfDstCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            endOfDstCal.setTimeInMillis(Long.parseLong(getDeviceMessageAttributeValue(pendingMessage, EndOfDSTAttributeName)));
            dstConfigurationRecord.setDayOfEndOfDst(new BcdEncodedField(String.valueOf(endOfDstCal.get(Calendar.DAY_OF_MONTH))));
            dstConfigurationRecord.setMonthOfEndOfDst(new BcdEncodedField(String.valueOf(endOfDstCal.get(Calendar.MONTH) + 1)));   // Java Calendar month is 0-based

            if (startOfDstCal.getTimeInMillis() == endOfDstCal.getTimeInMillis()) {
                throw new AbntException("Invalid DST configuration: start and end date of DST should be different");
            }
        } else {
            // Encode dummy dates
            dstConfigurationRecord.setDayOfStartOfDst(new BcdEncodedField("20"));
            dstConfigurationRecord.setMonthOfStartOfDst(new BcdEncodedField("09"));
            dstConfigurationRecord.setDayOfEndOfDst(new BcdEncodedField("20"));
            dstConfigurationRecord.setMonthOfEndOfDst(new BcdEncodedField("03"));
        }

        getMeterProtocol().getRequestFactory().configureDstParameters(dstConfigurationRecord);
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    private void messageNotSupported(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        collectedMessage.setFailureInformation(ResultType.NotSupported,
                this.issueFactory.createWarning(
                        pendingMessage, "DeviceMessage.notSupported",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName()
                )
        );
    }

    protected void messageFailed(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, String errorMessage) {
        collectedMessage.setFailureInformation(ResultType.InCompatible,
                this.issueFactory.createWarning(
                        pendingMessage, "DeviceMessage.failed",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName(),
                        errorMessage)
        );
        collectedMessage.setDeviceProtocolInformation(errorMessage);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    /**
     * Searches for the {@link OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then an IOException is thrown
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute
     */
    protected String getDeviceMessageAttributeValue(OfflineDeviceMessage offlineDeviceMessage, String attributeName) {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute.getValue();
            }
        }
        throw DataParseException.ioException(new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attributeName));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return this.format(UplToMdwPropertySpecAdapter.adapt(propertySpec), messageAttribute);
    }

    private String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof Date) {
            Date date = (Date) messageAttribute;    //Date, expressed in EIMaster system timezone, which can be different than ComServer timezone
            Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            gmtCal.setTimeInMillis(date.getTime() - (date.getTimezoneOffset() * NUMBER_OF_MILLIS_PER_MIN)); // Date will be converted to GMT
            if (propertySpec.getName().equals(StartOfDSTAttributeName) || propertySpec.getName().equals(EndOfDSTAttributeName)) {
                return String.valueOf(gmtCal.getTimeInMillis());    // Epoch
            } else {
                return dateFormatter.format(gmtCal.getTime());
            }
        } else if (messageAttribute instanceof TariffCalendar) {
            return convertCodeTableToXML((TariffCalendar) messageAttribute);
        } else {
            return messageAttribute.toString();
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
    }

    private String convertCodeTableToXML(TariffCalendar messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, this.extractor, 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }
}