package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.LittleEndianInputStream;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.PrimeRegisterForChannelIdentifier;
import com.energyict.protocolimplv2.messages.convertor.EIWebMessageConverter;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class ProtocolHandler {

    private final ResponseWriter responseWriter;
    private final InboundDAO inboundDAO;
    private final EIWebCryptographer cryptographer;
    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private ContentType contentType;
    private PacketBuilder packetBuilder;
    private ProfileBuilder profileBuilder;
    private List<CollectedRegister> registerData = new ArrayList<>();
    private CollectedConfigurationInformation configurationInformation;
    private CollectedLogBook deviceLogBook;
    private LegacyMessageConverter messageConverter = null;

    public ProtocolHandler(ResponseWriter responseWriter, InboundDiscoveryContext context) {
        super();
        this.responseWriter = responseWriter;
        this.inboundDAO = context.getInboundDAO();
        this.cryptographer = new EIWebCryptographer(context);
        this.collectedDataFactory = context.getCollectedDataFactory();
        this.propertySpecService = context.getPropertySpecService();
        this.nlsService = context.getNlsService();
        this.converter = context.getConverter();
        this.keyAccessorTypeExtractor = context.getKeyAccessorTypeExtractor();
    }

    private void setContentType(HttpServletRequest request) {
        contentType = ContentType.fromRequest(request);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return this.packetBuilder.getDeviceIdentifier();
    }

    public String getAdditionalInfo() {
        return this.packetBuilder.getAdditionalInfo().toString();
    }

    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = new ArrayList<>();
        if (this.profileBuilder != null) {
            this.packetBuilder.addCollectedData(collectedData);
            this.profileBuilder.addCollectedData(collectedData);
            collectedData.addAll(this.registerData);
            if (this.configurationInformation != null) {
                collectedData.add(this.configurationInformation);
            }
            CollectedData logBookEvents = getLogBookEvents();
            if (logBookEvents != null) {
                collectedData.add(logBookEvents);
            }
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
                    meterProtocolEvents.add(createMeterEvent(meterEvent.getTime(), meterEvent.getProtocolCode(), meterEvent.getMessage(), meterEvent.getEiCode()));
                }
                deviceLogBook.setCollectedMeterEvents(meterProtocolEvents);
            }

        }
        return deviceLogBook;
    }

    private void processMeterReadings(ProfileBuilder profileBuilder) {
        Date now = new Date();
        List<BigDecimal> meterReadings = profileBuilder.getMeterReadings();
        for (int i = 0; i < meterReadings.size(); i++) {
            BigDecimal value = meterReadings.get(i);
            ChannelInfo channelInfo = profileBuilder.getProfileData().getChannel(i);
            PrimeRegisterForChannelIdentifier registerIdentifier = new PrimeRegisterForChannelIdentifier(this.getDeviceIdentifier(), channelInfo.getChannelId(), null);

            CollectedRegister reading = this.collectedDataFactory.createDefaultCollectedRegister(registerIdentifier);
            reading.setReadTime(now);
            reading.setCollectedData(new Quantity(value, Unit.get(BaseUnit.COUNT)), "???");
            this.registerData.add(reading);
        }
    }

    private void processConfigurationInformation(ProfileBuilder profileBuilder) {
        this.configurationInformation = this.collectedDataFactory.createCollectedConfigurationInformation(this.getDeviceIdentifier(), "EIWebConfig", "xml", profileBuilder.getConfigFile());
    }

    public void handle(HttpServletRequest request, Logger logger) {
        try {
            setContentType(request);
            this.packetBuilder = new PacketBuilder(this.cryptographer, logger, collectedDataFactory);
            this.contentType.dispatch(request, this);
            if (this.packetBuilder.isConfigFileMode()) {
                this.profileBuilder = new ProfileBuilder(this.packetBuilder, collectedDataFactory);
                this.processConfigurationInformation(this.profileBuilder);
            } else if ((this.packetBuilder.getVersion() & 0x0080) == 0) {            // bit 8 indicates that the message is an alert
                this.profileBuilder = new ProfileBuilder(this.packetBuilder, collectedDataFactory);
                this.processMeterReadings(this.profileBuilder);
                this.profileBuilder.removeFutureData(logger, this.sevenDaysFromNow());
            } else {
                this.processEvents();
            }
            this.confirmSentMessagesAndSendPending();
        } catch (IOException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    private void confirmSentMessagesAndSendPending() {
        int nrOfAcceptedMessages = 0;
        if (this.packetBuilder.getNrOfAcceptedMessages() != null) {
            nrOfAcceptedMessages = this.packetBuilder.getNrOfAcceptedMessages();
        }
        List<OfflineDeviceMessage> pendingMessages = this.inboundDAO.confirmSentMessagesAndGetPending(this.getDeviceIdentifier(), nrOfAcceptedMessages);
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
                throw DataEncryptionException.dataEncryptionException();
            }
            is.readByte(); // alarmid
            int channel = is.readByte() & 0xFF; // ignored for now
            int status = is.readByte() & 0xFF;
            String tag = is.readString(40);
            /* For the moment these deviceLogBook/alarms are not linked to a channel.
             * So they are just added to the description from the device.*/
            tag += " (channel " + channel + ")";
            /* status == 0 start of alarm, status == 1 stop of alarm */
            int meterEvent;
            if (status == 1) {
                meterEvent = MeterEvent.APPLICATION_ALERT_START;
            } else {
                meterEvent = MeterEvent.APPLICATION_ALERT_STOP;
            }
            meterEvents.add(createMeterEvent(date, status, tag, meterEvent));
        }
        getDeviceLogBook().setCollectedMeterEvents(meterEvents);
    }

    private MeterProtocolEvent createMeterEvent(Date date, int status, String message, int meterEvent) {
        return new MeterProtocolEvent(date, meterEvent, meterEvent, EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(meterEvent), message, 0, status);
    }

    private Date sevenDaysFromNow() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 7);
        return calendar.getTime();
    }

    private void sendMessages(List<OfflineDeviceMessage> pendingMessages) {
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            this.responseWriter.add(getMessageConverter().toMessageEntry(pendingMessage).getContent());
        }
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new EIWebMessageConverter(this.propertySpecService, this.nlsService, this.converter, this.keyAccessorTypeExtractor);
        }
        return messageConverter;
    }

    private void processBinary(HttpServletRequest request) {
        try {
            Map parameters = parseParameters(request.getQueryString());
            this.packetBuilder.parseNrOfAcceptedMessages((String) parameters.get("xmlctr"));
            this.packetBuilder.parse(request.getInputStream(), (String) parameters.get("sn"));
        } catch (IOException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
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
            throw ConnectionCommunicationException.unexpectedIOException(e);
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
            this.deviceLogBook = this.collectedDataFactory.createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifier(), LogBook.GENERIC_LOGBOOK_TYPE_OBISCODE));
        }
        return this.deviceLogBook;
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

        public static ContentType fromRequest(HttpServletRequest request) {
            for (ContentType contentType : values()) {
                if (contentType.matches(request)) {
                    return contentType;
                }
            }
            throw CommunicationException.unsupportedUrlContentType(request.getContentType());
        }

        public abstract boolean matches(HttpServletRequest request);

        public abstract void dispatch(HttpServletRequest request, ProtocolHandler handler);
    }


}