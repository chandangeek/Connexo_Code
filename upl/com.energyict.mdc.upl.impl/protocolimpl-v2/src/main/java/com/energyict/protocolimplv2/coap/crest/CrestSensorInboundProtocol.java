package com.energyict.protocolimplv2.coap.crest;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.upl.CoapBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.io.CoapBasedExchange;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CrestSensorInboundProtocol implements CoapBasedInboundDeviceProtocol, CrestSensorConst {

    private final PropertySpecService propertySpecService;
    private final List<CollectedData> collectedData = new ArrayList<>();
    private CoapBasedExchange exchange;
    private InboundDAO inboundDAO;
    private InboundDiscoveryContext context;
    private CrestObjectV2_1 crestObject;
    private LegacyMessageConverter messageConverter = null;

    public CrestSensorInboundProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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

    private static byte[] HexStringToByteArray(String s) {
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            data[i / 2] = (Integer.decode("0x" + s.charAt(i) + s.charAt(i + 1))).byteValue();
        }
        return data;
    }

    @Override
    public void init(CoapBasedExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getVersion() {
        return "2022-10-30";
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
        String json;
        try {
            String hexString = new String(Hex.encodeHex(exchange.getRequestPayload()));
            json = cborToJson(HexStringToByteArray(hexString));
            crestObject = new ObjectMapper().readValue(json, CrestObjectV2_1.class);
            handleData();
            confirmSentMessagesAndSendPending();
        } catch (IOException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
        return DiscoverResultType.DATA;
    }

    private void confirmSentMessagesAndSendPending() {
        int nrOfAcceptedMessages = 0;
        List<OfflineDeviceMessage> pendingMessages = getContext().getInboundDAO().confirmSentMessagesAndGetPending(this.getDeviceIdentifier(), nrOfAcceptedMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            nrOfAcceptedMessages++;
            exchange.respond(getMessageConverter().toMessageEntry(pendingMessage).getContent());
        }
        getContext().getInboundDAO().confirmSentMessagesAndGetPending(this.getDeviceIdentifier(), nrOfAcceptedMessages);
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new CrestSensorMessageConverter(this.propertySpecService, context.getNlsService(), context.getConverter());
        }
        return messageConverter;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
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
        return true;
    }

    private void handleData() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierBySerialNumber(crestObject.getId());
        collectedData.add(buildCollectedFirmwareVersion(deviceIdentifier));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_TELECOM_PROVIDER_NAME, crestObject.getTel()));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_BATTERY_VOLTAGE, new Quantity(new BigDecimal(crestObject.getBat()), Unit.getUndefined())));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_SERIAL_NUMBER, crestObject.getId()));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_CONNECTION_METHOD, crestObject.getCon()));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_CELL_ID, crestObject.getcId()));
        collectedData.add(buildTextCollectedRegister(deviceIdentifier, OBIS_CODE_SIGNAL_QUALITY, crestObject.getCsq()));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_NR_OF_TRIES, new Quantity(new BigDecimal(crestObject.getTries()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_MEASUREMENT_SEND_INTERVAL, new Quantity(new BigDecimal(crestObject.getMsi()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_FOTA_MESSAGE_COUNTER, new Quantity(new BigDecimal(crestObject.getFmc()), Unit.getUndefined())));
        collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_MEMORY_COUNTER, new Quantity(new BigDecimal(crestObject.getMem()), Unit.getUndefined())));
        List<Integer> temperatures = crestObject.getT1();
        if (!temperatures.isEmpty()) {
            collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_AIR_TEMPERATURE, new Quantity(new BigDecimal(temperatures.get(temperatures.size() - 1)), Unit.get(BaseUnit.DEGREE_CELSIUS))));
        }
        List<Integer> percents = crestObject.getH1();
        if (!percents.isEmpty()) {
            collectedData.add(buildQuantityCollectedRegister(deviceIdentifier, OBIS_CODE_AIR_HUMIDITY, new Quantity(new BigDecimal(percents.get(percents.size() - 1)), Unit.get(BaseUnit.PERCENT))));
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
        CycleFrame6 frame6 = new CycleFrame6(ProtocolTools.hexToBytes(mbusMessage));
        return frame6.getFabricationNumber();
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
        ChannelInfo channelInfo = new ChannelInfo(0, DEFAULT_LOAD_PROFILE_CHANNEL_OBIS_CODE, Unit.get("l"));
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
        Integer value = v1.get((number * 2));
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
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
        cal.setTime(givenDate);
        cal.add(Calendar.MINUTE, 1); // add 1 minute, sometimes the value is reported on the last minute of the previous interval
        int intervalInMinutes = getIntervalInMinutes();
        if (intervalInMinutes < 60) {
            cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) / intervalInMinutes) * intervalInMinutes);
        } else if (intervalInMinutes == 60) {
            cal.set(Calendar.MINUTE, 0);
        } else {
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, cal.get((Calendar.HOUR_OF_DAY / (intervalInMinutes / 60)) * (intervalInMinutes / 60)));
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
        register.setReadTime(new Date());
        register.setCollectedData(text);
        return register;
    }

    private CollectedData buildQuantityCollectedRegister(DeviceIdentifier deviceIdentifier, ObisCode obisCode, Quantity value) {
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, deviceIdentifier);
        CollectedRegister register = getContext().getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);
        register.setReadTime(new Date());
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
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
    }
}
