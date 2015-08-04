package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.protocolimplv2.identifiers.PrimeRegisterForChannelIdentifier;
import com.energyict.protocolimplv2.messages.convertor.EIWebMessageConverter;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.LittleEndianInputStream;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ProtocolHandler {

    private ContentType contentType;
    private ResponseWriter responseWriter;
    private InboundDiscoveryContext inboundDiscoveryContext;
    private Cryptographer cryptographer;
    private PacketBuilder packetBuilder;
    private ProfileBuilder profileBuilder;
    private List<CollectedRegister> registerData = new ArrayList<>();
    private CollectedConfigurationInformation configurationInformation;
    private CollectedLogBook deviceLogBook;
    private LegacyMessageConverter messageConverter = null;
    private final Clock clock;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final IssueService issueService;

    public ProtocolHandler(ResponseWriter responseWriter, InboundDiscoveryContext inboundDiscoveryContext, Cryptographer cryptographer, Clock clock, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, IssueService issueService) {
        super();
        this.responseWriter = responseWriter;
        this.inboundDiscoveryContext = inboundDiscoveryContext;
        this.cryptographer = cryptographer;
        this.clock = clock;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.issueService = issueService;
    }

    private enum ContentType {
        BINARY {
            @Override
            public boolean matches(HttpServletRequest request) {
                return EIWebConstants.BINARY_CONTENT_TYPE_INDICATOR.equals(request.getContentType());
            }

            @Override
            public void dispatch(HttpServletRequest request, ProtocolHandler handler) {
                handler.processBinary(request);
            }
        },

        PLAINTEXT {
            @Override
            public boolean matches(HttpServletRequest request) {
                return EIWebConstants.PLAINTEXT_CONTENT_TYPE_INDICATOR.equals(request.getContentType());
            }

            @Override
            public void dispatch(HttpServletRequest request, ProtocolHandler handler) {
                handler.processPlainText(request);
            }
        };

        public abstract boolean matches(HttpServletRequest request);

        public abstract void dispatch(HttpServletRequest request, ProtocolHandler handler);

        public static ContentType fromRequest(HttpServletRequest request) {
            for (ContentType contentType : values()) {
                if (contentType.matches(request)) {
                    return contentType;
                }
            }
            throw new CommunicationException(MessageSeeds.UNSUPPORTED_URL_CONTENT_TYPE, request.getContentType());
        }
    }

    private void setContentType(HttpServletRequest request) {
        contentType = ContentType.fromRequest(request);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return this.packetBuilder.getDeviceIdentifier();
    }

    public List<CollectedData> getCollectedData(OfflineDevice offlineDevice) {
        List<CollectedData> collectedData = new ArrayList<>();
        this.packetBuilder.addCollectedData(collectedData);
        this.profileBuilder.addCollectedData(collectedData, offlineDevice);
        collectedData.addAll(this.registerData);
        if (this.configurationInformation != null) {
            collectedData.add(this.configurationInformation);
        }
        CollectedData logBookEvents = getLogBookEvents();
        if (logBookEvents != null) {
            collectedData.add(logBookEvents);
        }
        return collectedData;
    }

    private CollectedData getLogBookEvents() {
        CollectedLogBook deviceLogBook = this.getDeviceLogBook();
        if (deviceLogBook != null) {
            List<MeterEvent> meterEvents = this.profileBuilder.getProfileData().getMeterEvents();
            if (meterEvents != null && !meterEvents.isEmpty()) {
                List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
                for (MeterEvent meterEvent : meterEvents) {
                    Optional<MeterProtocolEvent> meterProtocolEvent = createMeterEvent(meterEvent.getTime(), meterEvent.getProtocolCode(), meterEvent.getMessage(), meterEvent.getEiCode());
                    if (meterProtocolEvent.isPresent()) {
                        meterProtocolEvents.add(meterProtocolEvent.get());
                    }
                    else {
                        // Todo: need to add failure information that the log book is not supported because the powerDown event type does not exist
                    }
                }
                deviceLogBook.setMeterEvents(meterProtocolEvents);
            }

        }
        return deviceLogBook;
    }

    private void processMeterReadings(ProfileBuilder profileBuilder) {
        Instant now = this.clock.instant();
        List<BigDecimal> meterReadings = profileBuilder.getMeterReadings();
        for (int i = 0; i < meterReadings.size(); i++) {
            BigDecimal value = meterReadings.get(i);
            ChannelInfo channelInfo = profileBuilder.getProfileData().getChannel(i);
            PrimeRegisterForChannelIdentifier registerIdentifier;
            registerIdentifier = new PrimeRegisterForChannelIdentifier(
                    this.getDeviceIdentifier(), channelInfo.getChannelObisCode(), channelInfo.getChannelObisCode(), channelInfo.getChannelId());
            CollectedRegister reading = this.getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier, channelInfo.getReadingType());
            reading.setReadTime(now);
            reading.setCollectedData(new Quantity(value, Unit.get(BaseUnit.COUNT)), "???");
            this.registerData.add(reading);
        }
    }

    private void processConfigurationInformation(ProfileBuilder profileBuilder) {
        this.configurationInformation = this.getCollectedDataFactory().createCollectedConfigurationInformation(this.getDeviceIdentifier(), "xml", profileBuilder.getConfigFile());
    }

    public void handle(HttpServletRequest request, Logger logger) {
        try {
            setContentType(request);
            this.packetBuilder = new PacketBuilder(this.cryptographer, logger, identificationService, collectedDataFactory);
            this.contentType.dispatch(request, this);
            if (this.packetBuilder.isConfigFileMode()) {
                this.profileBuilder = new ProfileBuilder(this.packetBuilder, identificationService, issueService);
                this.processConfigurationInformation(this.profileBuilder);
            } else if ((this.packetBuilder.getVersion() & 0x0080) == 0) {            // bit 8 indicates that the message is an alert
                this.profileBuilder = new ProfileBuilder(this.packetBuilder, identificationService, issueService);
                this.processMeterReadings(this.profileBuilder);
                this.profileBuilder.removeFutureData(logger, Date.from(this.sevenDaysFromNow()));
            } else {
                this.processEvents();
            }
            this.confirmSentMessagesAndSendPending();
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    private void confirmSentMessagesAndSendPending() {
        int nrOfAcceptedMessages = 0;
        if (this.packetBuilder.getNrOfAcceptedMessages() != null) {
            nrOfAcceptedMessages = this.packetBuilder.getNrOfAcceptedMessages();
        }
        List<OfflineDeviceMessage> pendingMessages = this.inboundDiscoveryContext.confirmSentMessagesAndGetPending(this.getDeviceIdentifier(), nrOfAcceptedMessages);
        this.sendMessages(pendingMessages);
    }

    private void processEvents() throws IOException {
        byte[] data = this.packetBuilder.getData();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianInputStream is = new LittleEndianInputStream(bis);
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        for (int i = 0; i < this.packetBuilder.getNrOfRecords(); i++) {
            long ldate = (is.readLEUnsignedInt() + EIWebConstants.SECONDS10YEARS) * 1000;
            Date date = new Date(ldate);
            if ((i == 0) && (!this.packetBuilder.isTimeCorrect(date))) {
                throw new DataEncryptionException(MessageSeeds.ENCRYPTION_ERROR, this.getDeviceIdentifier());
            }
            is.readByte(); // alarmid
            int channel = is.readByte() & 0xFF; // ignored for now
            int status = is.readByte() & 0xFF;
            String tag = is.readString(40);
            /* For the moment these deviceLogBook/alarms are not linked to a channel.
             * So they are just added to the description from the device.*/
            tag += " (channel " + channel + ")";
            /* status == 0 start of alarm, status == 1 stop of alarm */
            int meterEventCode;
            if (status == 1) {
                meterEventCode = MeterEvent.APPLICATION_ALERT_START;
            } else {
                meterEventCode = MeterEvent.APPLICATION_ALERT_STOP;
            }
            Optional<MeterProtocolEvent> meterEvent = createMeterEvent(date, status, tag, meterEventCode);
            if (meterEvent.isPresent()) {
                meterEvents.add(meterEvent.get());
            }
            else {
                getDeviceLogBook().setFailureInformation(
                        ResultType.NotSupported,
                        this.issueService.newWarning(
                                deviceLogBook,
                                com.energyict.mdc.protocol.api.MessageSeeds.END_DEVICE_EVENT_TYPE_NOT_SUPPORTED.getKey(),
                                EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()));
            }
        }
        getDeviceLogBook().setMeterEvents(meterEvents);
    }

    private Optional<MeterProtocolEvent> createMeterEvent(Date date, int status, String message, int meterEvent) {
        return EndDeviceEventTypeMapping
                .getEventTypeCorrespondingToEISCode(meterEvent, this.meteringService)
                .map(endDeviceEventType -> new MeterProtocolEvent(date, meterEvent, meterEvent, endDeviceEventType, message, 0, status));
    }

    private Instant sevenDaysFromNow() {
        return this.clock.instant().plus(Period.ofDays(7));
    }

    private void sendMessages(List<OfflineDeviceMessage> pendingMessages) {
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            this.responseWriter.add(getMessageConverter().toMessageEntry(pendingMessage).getContent());
        }
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new EIWebMessageConverter();
        }
        return messageConverter;
    }

    private void processBinary(HttpServletRequest request) {
        try {
            Map parameters = parseParameters(request.getQueryString());
            this.packetBuilder.parseNrOfAcceptedMessages((String) parameters.get("xmlctr"));
            this.packetBuilder.parse(request.getInputStream(), (String) parameters.get("sn"));
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    private void processPlainText(HttpServletRequest request) {
        try {
            this.packetBuilder.parse(
                    request.getParameter(EIWebConstants.DEVICE_ID_URL_PARAMETER_NAME),
                    request.getParameter("seq"),
                    request.getParameter("utc"),
                    request.getParameter("code"),
                    request.getParameter("statebits"),
                    request.getParameter(EIWebConstants.MASK_URL_PARAMETER_NAME),
                    request.getParameter(EIWebConstants.METER_DATA_PARAMETER_NAME),
                    request.getParameter("ip"),
                    request.getParameter("sn"),
                    request.getParameter(EIWebConstants.MESSAGE_COUNTER_URL_PARAMETER_NAME));
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    private Map parseParameters(String in) {
        Map<String, String> result = new HashMap<>();
        if (in == null) {
            return result;
        }
        String[] keyValuePairs = in.split("&");
        for (int i = 0; i < keyValuePairs.length; i++) {
            String[] tokens = keyValuePairs[i].split("=");
            if (tokens.length == 2) {
                result.put(tokens[0], tokens[1]);
            }
        }
        return result;
    }

    private CollectedLogBook getDeviceLogBook() {
        if (this.deviceLogBook == null) {
            this.getCollectedDataFactory().createCollectedLogBook(this.identificationService.createLogbookIdentifierByObisCodeAndDeviceIdentifier(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE, getDeviceIdentifier()));
        }
        return this.deviceLogBook;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return this.packetBuilder.getCollectedDataFactory();
    }

}