/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.upl.CoapBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.io.CoapBasedExchange;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.PENDING_MESSAGES_FLAG;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;

public class CrestSensorInboundProtocol implements CoapBasedInboundDeviceProtocol, CrestSensorConst {

    public static final String RESPONSE_OK = "0";
    public static final String RESPONSE_KO = "SERR";
    public static final String LOGGING_PAYLOAD_DETAILS = "logPayloadDetails";

    private final TypedProperties protocolProperties;
    private final PropertySpecService propertySpecService;
    private final List<CollectedData> collectedData = new ArrayList<>();
    private CoapBasedExchange exchange;
    private InboundDAO inboundDAO;
    private InboundDiscoveryContext context;
    private CrestObjectV2_1 crestObject;
    private LegacyMessageConverter messageConverter = null;
    public static final Logger logger = Logger.getLogger(CrestSensorInboundProtocol.class.getName());
    private int miliSeconds = 1000;

    public CrestSensorInboundProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        protocolProperties = com.energyict.mdc.upl.TypedProperties.empty();

    }

    private static String cborToJson(byte[] input) throws IOException {
        CBORFactory cborFactory = new CBORFactory();
        CBORParser cborParser = cborFactory.createParser(input);
        JsonFactory jsonFactory = new JsonFactory();
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
        while (cborParser.nextToken() != null) {
            jsonGenerator.copyCurrentEvent(cborParser);
        }
        jsonGenerator.flush();
        return stringWriter.toString();
    }

    @Override
    public void init(CoapBasedExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getVersion() {
        return "2022-04-10";
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
        this.inboundDAO = context.getInboundDAO();
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        byte[] cborInput = exchange.getRequestPayload();
        try {
            if (isLoggingPayloadDetails()) {
                getLogger().info("Payload received: " + Hex.encodeHexString(cborInput));
            }
            String json = cborToJson(cborInput);
            if (isLoggingPayloadDetails()) {
                getLogger().info("Payload decoded: " + json);
            }
            crestObject = new ObjectMapper().readValue(json, CrestObjectV2_1.class);
            handleData();
            sendNextMessage();
            exchange.respond(RESPONSE_OK);
        } catch (Exception e) {
            exchange.respond(RESPONSE_KO);
            getLogger().log(Level.SEVERE, "Payload error: " + e.getMessage(), e);
            throw ConnectionCommunicationException.unexpectedIOException(new IOException("Incorrect payload data", e));
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!DiscoverResponseType.SUCCESS.equals(responseType)) {
            getLogger().severe("Crest sensor inbound communication failure: " + responseType);
            exchange.respond(RESPONSE_KO);
        }
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierBySerialNumber(crestObject.getId());
    }

    @Override
    public String getAdditionalInformation() {
        return "";
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return collectedData;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new CrestSensorMessageConverter(this.propertySpecService, context.getNlsService(), context.getConverter());
        }
        return messageConverter;
    }

    private void sendNextMessage() {
        final OfflineDevice offlineDevice = getContext().getInboundDAO().getOfflineDevice(this.getDeviceIdentifier(), new DeviceOfflineFlags(PENDING_MESSAGES_FLAG));
        final Optional<OfflineDeviceMessage> nextPriorityPendingMessage = getNextPriorityPendingMessage(offlineDevice.getAllPendingDeviceMessages());

        // only send the oldest pending message as per device requirements
        if (nextPriorityPendingMessage.isPresent()) {
            OfflineDeviceMessage priorityMessage = nextPriorityPendingMessage.get();
            CollectedMessageList result = getContext().getCollectedDataFactory().createCollectedMessageList(Collections.singletonList(priorityMessage));
            CollectedMessage collectedMessage = getContext().getCollectedDataFactory().createCollectedMessage(priorityMessage.getMessageIdentifier());
            if (priorityMessage.getSpecification().equals(GeneralDeviceMessage.SEND_XML_MESSAGE)) {
                collectedMessage = sendXMLAttribute(priorityMessage, collectedMessage);
            } else if (priorityMessage.getSpecification().equals(GeneralDeviceMessage.SET_PSK)) {
                collectedMessage = setPSKValue(priorityMessage, collectedMessage);
            } else if (priorityMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE)) {
                collectedMessage = upgradeFirmware(priorityMessage, collectedMessage);
            } else if (priorityMessage.getSpecification().equals(GeneralDeviceMessage.RESET_FOTA)) {
                collectedMessage = sendXMLAttribute(priorityMessage, collectedMessage);
            } else if (priorityMessage.getSpecification().equals(GeneralDeviceMessage.SWITCH_BACK_PREVIOUS_FIRMWARE)) {
                collectedMessage = sendXMLAttribute(priorityMessage, collectedMessage);
            }
            result.addCollectedMessage(collectedMessage);
            this.collectedData.add(result);
        }
    }

    private Optional<OfflineDeviceMessage> getNextPriorityPendingMessage(List<OfflineDeviceMessage> pendingMessages) {
        final Comparator<OfflineDeviceMessage> creationDateComparator = Comparator.comparing(OfflineDeviceMessage::getCreationDate);
        List<OfflineDeviceMessage> priorityMessages = pendingMessages.stream().filter(m -> !m.isFirmwareMessage()).collect(Collectors.toList());
        List<OfflineDeviceMessage> lowPriorityMessages = pendingMessages.stream().filter(m -> m.isFirmwareMessage()).collect(Collectors.toList());

        if (!priorityMessages.isEmpty()) {
            return priorityMessages.stream().min(creationDateComparator);
        } else if (!lowPriorityMessages.isEmpty()) {
            return lowPriorityMessages.stream().min(creationDateComparator);
        }

        return Optional.empty();
    }

    private CollectedMessage upgradeFirmware(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        final String filePath = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getValue();
        final Integer firmwareFrameCounter = crestObject.getFmc();

        try {
            final List<String> firmwareFileLines = Files.readAllLines(Paths.get(filePath));
            final String lineToSend = firmwareFileLines.get(firmwareFrameCounter);
            exchange.respond(lineToSend);
            getLogger().info("Message sent: " + lineToSend + " line: " + firmwareFrameCounter);
            collectedMessage.setDeviceProtocolInformation("Sent package " + firmwareFrameCounter + "/" + firmwareFileLines.size());
            if (firmwareFileLines.size() == firmwareFrameCounter + 1) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            }
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.toString());
        }
        return collectedMessage;
    }

    private CollectedMessage sendXMLAttribute(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        String message = getMessageConverter().toMessageEntry(pendingMessage).getContent();
        exchange.respond(message);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    private CollectedMessage setPSKValue(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        String message = getMessageConverter().toMessageEntry(pendingMessage).getContent();
        exchange.respond(message);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    private void handleData() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierBySerialNumber(crestObject.getId());
        collectedData.add(buildCollectedFirmwareVersion(deviceIdentifier));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_TELECOM_PROVIDER_NAME, crestObject.getTel()));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_BATTERY_VOLTAGE, new Quantity(new BigDecimal(crestObject.getBat()), Unit.get(BaseUnit.VOLT, -3))));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_SERIAL_NUMBER, crestObject.getId()));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_CONNECTION_METHOD, crestObject.getCon()));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_CELL_ID, crestObject.getCID()));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_NR_OF_TRIES, new Quantity(new BigDecimal(crestObject.getTries()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_MEASUREMENT_SEND_INTERVAL, new Quantity(new BigDecimal(crestObject.getMsi()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_FOTA_MESSAGE_COUNTER, new Quantity(new BigDecimal(crestObject.getFmc()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_MEMORY_COUNTER, new Quantity(new BigDecimal(crestObject.getMem()), Unit.getUndefined())));
        if (crestObject.getT1() != null) {
            List<Integer>  temperatures = crestObject.getT1();
            if(!crestObject.getT1().isEmpty()){
                collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_AIR_TEMPERATURE, new Quantity(new BigDecimal(temperatures.get(temperatures.size() - 1) / (double) 10), Unit.get(BaseUnit.DEGREE))));
            }
        }
        if(crestObject.getH1() != null) {
            List<Integer> percents = crestObject.getH1();
            if (!percents.isEmpty()) {
                collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_AIR_HUMIDITY, String.valueOf(percents.get(percents.size() - 1) / (double) 10) + "%"));
            }
        }
        String signalQuality  = crestObject.getCsq();
        if (!signalQuality.isEmpty()) {
            collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_SIGNAL_QUALITY, new Quantity(new BigDecimal(applySignalQualityFormula(Integer.parseInt(signalQuality))),  Unit.get(BaseUnit.DECIBELMILLIWAT,-3))));
        }
        CollectedTopology collectedTopology = getContext().getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        if (!Strings.isNullOrEmpty(this.crestObject.getV1m())) {
            DeviceIdentifierBySerialNumber slave = new DeviceIdentifierBySerialNumber(getSlaveSerialNumber(crestObject.getV1m()));
            this.collectedData.add(buildCollectedLoadProfile(slave, crestObject.getV1()));
            collectedTopology.addSlaveDevice(slave);
        }
        if (!Strings.isNullOrEmpty(this.crestObject.getV2m())) {
            DeviceIdentifierBySerialNumber slave = new DeviceIdentifierBySerialNumber(getSlaveSerialNumber(crestObject.getV2m()));
            this.collectedData.add(buildCollectedLoadProfile(slave, crestObject.getV2()));
            collectedTopology.addSlaveDevice(slave);
        }
        this.collectedData.add(collectedTopology);
    }

    private String getSlaveSerialNumber(String mbusMessage) {
        byte[] mbusFrame = ProtocolTools.hexToBytes(mbusMessage);
        switch (ManufacturerID.forId(mbusFrame[12])) {
            case ACTARIS: {
                ActarisFrame6 frame6 = new ActarisFrame6(mbusFrame);
                return frame6.getFabricationNumber();
            }
            case FALCON: {
                FalconFrameSndUp frameSndUp = new FalconFrameSndUp(mbusFrame);
                return frameSndUp.getSerialNumber();
            }
            default:
                return "0";
        }
    }

    private CollectedData buildCollectedLoadProfile(DeviceIdentifier deviceIdentifier, List<Integer> v1) {
        int intervalInMinutes = getIntervalInMinutes();
        int intervalHours = intervalInMinutes / 60;
        int intervalMinutes = intervalInMinutes % 60;
        ObisCode loadProfileObisCode = new ObisCode(1, 0, 96, intervalHours, intervalMinutes, 255);
        LoadProfileIdentifier lpi = new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier);
        CollectedLoadProfile collectedLoadProfile = getContext().getCollectedDataFactory().createCollectedLoadProfile(lpi);
        collectedLoadProfile.setCollectedIntervalData(buildIntervalDataFromV1(v1), buildChannelMap());
        return collectedLoadProfile;
    }

    private List<ChannelInfo> buildChannelMap() {
        ChannelInfo channelInfo = new ChannelInfo(0, DEFAULT_LOAD_PROFILE_CHANNEL_OBIS_CODE, Unit.get(BaseUnit.LITER));
        channelInfo.setCumulative();
        return Arrays.asList(channelInfo);
    }

    private List<IntervalData> buildIntervalDataFromV1(List<Integer> v1) {
        List<IntervalData> intervalDataList = new ArrayList<>();
        for (int i = 0; i < (v1.size() / 2); i++) {
            intervalDataList.add(buildIntervalData(v1, i));
        }
        return intervalDataList;
    }

    private IntervalData buildIntervalData(List<Integer> v1, int number) {
        Date intervalDate = roundToInterval(buildDate(v1.get((number * 2) + 1)));
        Double value = v1.get((number * 2)) / (double) 1000;
        IntervalData intervalData = new IntervalData(intervalDate);
        IntervalValue intervalValue = new IntervalValue(value, 0, 0);
        intervalData.getIntervalValues().add(intervalValue);
        return intervalData;
    }

    private Date buildDate(int dateTime) {
        int min = (dateTime & 0x3F);
        int hour = (dateTime >> 8 & 0x1F);
        int day = (dateTime >> 16) & 0x1F;
        int month = (dateTime >> 24) & 0x1F;
        int year = (((dateTime >> 16) & 0xE0) >> 5) + (((dateTime >> 24) & 0xF0) >> 1);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, 2000 + year);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date roundToInterval(Date givenDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(EUROPE_AMSTERDAM_TIMEZONE));
        cal.setTime(givenDate);
        cal.add(Calendar.MINUTE, 1); // add 1 minute, sometimes the value is reported on the last minute of the previous interval
        int intervalInMinutes = getIntervalInMinutes();
        if (0 == intervalInMinutes) {
            cal.set(Calendar.MINUTE, 0);
        } else if (intervalInMinutes < 60) {
            cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) / intervalInMinutes) * intervalInMinutes);
        } else if (intervalInMinutes == 60) {
            cal.set(Calendar.MINUTE, 0);
        } else {
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, (cal.get(Calendar.HOUR_OF_DAY) / (intervalInMinutes / 60)) * (intervalInMinutes / 60));
        }
        return cal.getTime();
    }

    private int getIntervalInMinutes() {
        switch (crestObject.getMsi()) {
            case 0:
                return 5;
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 20;
            case 4:
                return 30;
            case 5:
                return 40;
            case 6:
                return 45;
            case 7:
                return 60;
            case 8:
                return 120;
            case 9:
                return 360;
        }
        return 0;
    }

    private CollectedData buildTextCollectedRegister(DeviceIdentifier deviceIdentifier, ObisCode obisCode, String text) {
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, deviceIdentifier);
        CollectedRegister register = getContext().getCollectedDataFactory().createTextCollectedRegister(registerIdentifier);
        register.setReadTime(new Date(Long.parseLong(crestObject.getTs()) * miliSeconds));
        register.setCollectedData(text);
        return register;
    }

    private CollectedData buildQuantityCollectedRegister(DeviceIdentifier deviceIdentifier, ObisCode obisCode, Quantity value) {
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, deviceIdentifier);
        CollectedRegister register = getContext().getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);
        register.setReadTime(new Date(Long.parseLong(crestObject.getTs()) * miliSeconds));
        register.setCollectedData(value);
        return register;
    }

    private CollectedData buildCollectedFirmwareVersion(DeviceIdentifier deviceIdentifier) {
        CollectedFirmwareVersion collectedFirmwareVersion = getContext().getCollectedDataFactory().createFirmwareVersionsCollectedData(deviceIdentifier);
        collectedFirmwareVersion.setActiveMeterFirmwareVersion(crestObject.getFw());
        return collectedFirmwareVersion;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(loggingPayloadDetailsPropertySpec());
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.protocolProperties.setAllProperties(properties);
    }

    protected PropertySpec loggingPayloadDetailsPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(LOGGING_PAYLOAD_DETAILS, true, PropertyTranslationKeys.V2_CREST_LOG_PAYLOAD_DETAILS, propertySpecService::booleanSpec).finish();
    }

    public TypedProperties getProtocolProperties() {
        return protocolProperties;
    }

    private boolean isLoggingPayloadDetails() {
        return getProtocolProperties().getTypedProperty(LOGGING_PAYLOAD_DETAILS, false);
    }

    protected Logger getLogger() {
        return getContext().getLogger();
    }

    public int applySignalQualityFormula(int signalQuality){
        return signalQuality * 2 - 113;
    }
}
