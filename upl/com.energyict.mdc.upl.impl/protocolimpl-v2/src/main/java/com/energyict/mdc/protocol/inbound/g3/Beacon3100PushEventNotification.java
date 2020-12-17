package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100AbstractEventLog;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the Beacon3100 protocol to connect to the DC device.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class Beacon3100PushEventNotification extends PushEventNotification {

    /**
     * JSON keys for PLC_G3_REGISTER_NODE event
     */
    public static final String JSON_METER_IDENTIFIER = "MeterIdentifier";
    public static final String JSON_SAP_DLMS_GW = "SAP_DLMS_GW";
    public static final String JSON_SAP_DLMS_MIR = "SAP_DLMS_MIR";
    public static final String JSON_SAP_IPV_6 = "SAP_IPV6";
    public static final String JSON_SAP_IPV_4 = "SAP_IPV4";
    public static final String JSON_SAP_802_15_4_ID = "SAP_802_15_4_ID";
    public enum TopologyAction {
        REMOVE,
        ADD;
    }
    /**
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    protected static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    private static final int PLC_G3_REGISTER_NODE = 0xC20000;
    private static final int PLC_G3_UNREGISTER_NODE = 0xC30000;
    private static final int PLC_G3_NODE_LINK_LOST = 0xCB0000;
    private static final int PLC_G3_NODE_LINK_RECOVERED = 0xCC0000;
    protected boolean provideProtocolJavaClasName = true;
    /**
     * Used to pass back any topology changes observed during push notifications
     */
    protected CollectedTopology collectedTopology;

    protected final PropertySpecService propertySpecService;
    private final CollectedDataFactory collectedDataFactory;

    public Beacon3100PushEventNotification(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
    }

    protected BeaconPSKProvider getPskProvider() {
        return BeaconPSKProviderFactory.getInstance(provideProtocolJavaClasName).getPSKProvider(getDeviceIdentifier());
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG);
        }
        return parser;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        List<com.energyict.mdc.upl.properties.PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, false, PropertyTranslationKeys.V2_G3_PROVIDE_PROTOCOL_JAVA_CLASS_NAME, this.propertySpecService::booleanSpec)
                        .setDefaultValue(Boolean.TRUE)
                        .finish());
        return propertySpecs;
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-12-17$";
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        super.setUPLProperties(properties);
        this.provideProtocolJavaClasName = properties.getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
    }

    @Override
    public DiscoverResultType doDiscovery() {
        DiscoverResultType discoverResultType = super.doDiscovery();

        // do a specific Beacon post-processing of the push event
        try {
            searchForTopologyUpdateEvents();
        } catch (JSONException e) {
            getContext().getLogger().log(Level.WARNING, "Exception while parsing inbound message: " + e.getMessage(), e);
        }

        return discoverResultType;
    }

    private void searchForTopologyUpdateEvents() throws JSONException {
        MeterProtocolEvent receivedEvent = getMeterProtocolEvent();

        if (receivedEvent == null) {
            return;
        }

        switch (receivedEvent.getProtocolCode()) {

            /**
             * Generates when node successfully joins the PAN.
             */
            case PLC_G3_REGISTER_NODE:
                this.collectedTopology = extractTopologyUpdateFromRegisterEvent(receivedEvent);
                break;

            /**
             * Generated when node is considered lost (i.e. does no longer respond without proper de-registration from the network)
             */
            case PLC_G3_NODE_LINK_LOST:
                this.collectedTopology = extractNodeInformation(extractReceivedDescription(receivedEvent), TopologyAction.REMOVE);
                break;

            /**
             * Generated when a node recovered from the lost state (i.e. it is reachable again after being unreachable for prolonged period of time).
             */
            case PLC_G3_NODE_LINK_RECOVERED:
                this.collectedTopology = extractNodeInformation(extractReceivedDescription(receivedEvent), TopologyAction.ADD);
                break;

            /**
             * Generated when node leaves the PAN.
             */
            case PLC_G3_UNREGISTER_NODE:
                this.collectedTopology = extractNodeInformation(extractReceivedDescription(receivedEvent), TopologyAction.REMOVE);
        }

        logWhatWeDiscovered();
    }

    private void logWhatWeDiscovered() {
        if (collectedTopology != null) {
            if (collectedTopology.getJoinedSlaveDeviceIdentifiers() != null) {
                getContext().getLogger().info("> joined devices: " + collectedTopology.getJoinedSlaveDeviceIdentifiers());
            }
            if (collectedTopology.getLostSlaveDeviceIdentifiers() != null) {
                getContext().getLogger().info("> lost devices: " + collectedTopology.getLostSlaveDeviceIdentifiers());
            }
            if (collectedTopology.getAdditionalCollectedDeviceInfo() != null) {
                getContext().getLogger().info("> device parameters: " + collectedTopology.getAdditionalCollectedDeviceInfo().toString());
            }
        }
    }

    public CollectedTopology extractNodeInformation(String macAddress, TopologyAction action) {
        if (macAddress == null || macAddress.isEmpty()) {
            return null;
        }

        macAddress = macAddress.replace(":", "").replace(".", "");
        DeviceIdentifier slaveDeviceIdentified = new DialHomeIdDeviceIdentifier(macAddress);

        CollectedTopology deviceTopology = collectedDataFactory.createCollectedTopology(getDeviceIdentifier());

        switch (action) {
            case ADD:
                ObservationDateProperty lastSeenDateInfo = new ObservationDateProperty(G3Properties.PROP_LASTSEENDATE, getNow());
                deviceTopology.addJoinedSlaveDevice(slaveDeviceIdentified, lastSeenDateInfo);
                break;

            case REMOVE:
                deviceTopology.addLostSlaveDevice(slaveDeviceIdentified);
                break;
        }

        return deviceTopology;
    }
    /**
    * Generates when node successfully joins the PAN.
    */
    public CollectedTopology extractTopologyUpdateFromRegisterEvent(MeterProtocolEvent receivedEvent) throws JSONException {
        String message = extractReceivedDescription(receivedEvent);

        if (message == null || message.isEmpty()) {
            return null;
        }

        CollectedTopology deviceTopology = null;

        deviceTopology = extractRegisterEventBeacon10(message);
        if (deviceTopology != null) {
            return deviceTopology;
        }

        deviceTopology = extractRegisterEventBeacon11(message);

        return deviceTopology;
    }

    /**
     * Extract topology update from an register event sent by an beacon 1.11, in JSON format:
     * <p/>
     * Received message contains JSON structure with EUI-64 of the meter, and list of possible service access points for that meter.
     * Included SAP list depends on the configuration.
     * <p/>
     * Keys present in the JSON include:
     * MeterIdentifier: holds the EUI-64 of the meter
     * SAP_802_15_4_ID: short address of the meter on the PAN
     * SAP_DLMS_GW: DLMS gateway virtual logical device ID (if the gateway is enabled)
     * SAP_DLMS_MIR: DLMS data-concentrator mirrored logical device (if the meter is scheduled for readout)
     * SAP_IPV6: Routable IPv6 address of the meter (if the border routing functionality is enabled)
     * SAP_IPV4: Routable IPv4 address of the meter (if the border routing functionality is enabled)
     */
    private CollectedTopology extractRegisterEventBeacon11(String message) throws JSONException {

        JSONObject json;

        try {
            json = new JSONObject(message);
        } catch (Exception e) {
            getContext().getLogger().warning("- message is not a JSON: " + e.getMessage());
            return null;
        }

        DeviceIdentifier slaveDeviceIdentifier = null;

        if (json.has(JSON_METER_IDENTIFIER)) {
            String meterIdentifier = json.get(JSON_METER_IDENTIFIER).toString();
            String macAddress = meterIdentifier.replace(":", "").replace(".", "");
            slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
        }

        if (slaveDeviceIdentifier == null) {
            // no information about a slave device
            return null;
        }

        // we have a slave device, so create the topology object to be filled in
        CollectedTopology deviceTopology = collectedDataFactory.createCollectedTopology(getDeviceIdentifier());

        BigDecimal lastSeenDate = getNow();
        ObservationDateProperty lastSeenDateInfo = new ObservationDateProperty(G3Properties.PROP_LASTSEENDATE, lastSeenDate);
        deviceTopology.addJoinedSlaveDevice(slaveDeviceIdentifier, lastSeenDateInfo);

        if (json.has(JSON_SAP_DLMS_GW)) {
            int SAP_DLMS_GW = getJsonInt(json, JSON_SAP_DLMS_GW);
            if (SAP_DLMS_GW > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, SAP_DLMS_GW
                        )
                );
            }
        }

        if (json.has(JSON_SAP_DLMS_MIR)) {
            int SAP_DLMS_MIR = getJsonInt(json, JSON_SAP_DLMS_MIR);
            if (SAP_DLMS_MIR > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, SAP_DLMS_MIR
                        )
                );
            }
        }

        if (json.has(JSON_SAP_IPV_6)) {
            String SAP_IPV6 = json.getString(JSON_SAP_IPV_6);
            if (SAP_IPV6 != null && SAP_IPV6.length() > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AM540ConfigurationSupport.IP_V6_ADDRESS, SAP_IPV6
                        )
                );
            }
        }

        if (json.has(JSON_SAP_IPV_4)) {
            String SAP_IPV4 = json.getString(JSON_SAP_IPV_4);
            if (SAP_IPV4 != null && SAP_IPV4.length() > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AM540ConfigurationSupport.IP_V4_ADDRESS, SAP_IPV4
                        )
                );
            }
        }

        if (json.has(JSON_SAP_802_15_4_ID)) {
            int SAP_802_15_4_ID = getJsonInt(json, JSON_SAP_802_15_4_ID);
            if (SAP_802_15_4_ID > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AM540ConfigurationSupport.SHORT_ADDRESS_PAN, SAP_802_15_4_ID
                        )
                );
            }
        }

        deviceTopology.addAdditionalCollectedDeviceInfo(
                collectedDataFactory.createCollectedDeviceProtocolProperty(
                        slaveDeviceIdentifier,
                        G3Properties.PROP_LASTSEENDATE,
                        lastSeenDate
                )
        );

        return deviceTopology;
    }

    /**
     * Decode an event received from an Beacon 1.10, in the format:
     * Node [0223:7EFF:FEFD:A955] [0x0056] has registered on the network
     *
     * @param message
     * @return
     */
    private CollectedTopology extractRegisterEventBeacon10(String message) {
        if (!message.startsWith("Node [")) {
            return null;
        }

        String[] parts = message.split("[\\[\\]]");
        if (parts.length < 2) {
            return null;
        }
        String macAddress = parts[1];
        return extractNodeInformation(macAddress, TopologyAction.ADD);
    }

    private int getJsonInt(JSONObject json, String key) throws JSONException {
        String value = json.getString(key);
        if (value.contains("0x")) {
            return Integer.parseInt(value.replace("0x", ""), 16);
        } else {
            return Integer.parseInt(value);
        }
    }

    private BigDecimal getNow() {
        long mills = new Date().getTime();
        return BigDecimal.valueOf(mills);
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = super.getCollectedData();
        if (collectedTopology != null) {
            collectedData.add(collectedTopology);
        }

        return collectedData;
    }

    /**
     * We extract only the part received from device. The description mapped in our MeterEvent class will be removed
     * @param receivedEvent
     * @return
     */
    protected String extractReceivedDescription(MeterProtocolEvent receivedEvent) {
        String prefixToReplace = Beacon3100AbstractEventLog.getDescriptionPrefix(receivedEvent.getEiCode());
        return receivedEvent.getMessage().replaceFirst(prefixToReplace, "");
    }

}