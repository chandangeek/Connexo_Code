package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100AbstractEventLog;
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
    public static final String HEART_BEAT_ETH_1_GLOBAL = "eth1_global";
    public static final String HEART_BEAT_PPP_GLOBAL = "ppp_global";
    public static final String HEART_BEAT_DUMMY_1_GLOBAL = "dummy1_global";
    public static final String HEART_BEAT_DNS = "dns";
    public static final String HEART_BEAT_TOTAL_SPACE = "total_space";
    public static final String HEART_BEAT_USED_SPACE = "used_space";
    public static final String HEART_BEAT_FREE_SPACE = "free_space";
    public enum TopologyAction {
        REMOVE,
        ADD;
    }
    /**
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    protected static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    private static final int PLC_G3_REGISTER_NODE = 0xC20000;
    private static final int PLC_G3_UNREGISTER_NODE = 0xC30000;
    private static final int PLC_G3_NODE_LINK_LOST = 0xCB0000;
    private static final int PLC_G3_NODE_LINK_RECOVERED = 0xCC0000;
    private static final int BEACON_HEART_BEAT = 0xD10000;
    protected boolean provideProtocolJavaClasName = true;
    /**
     * Used to pass back any topology changes observed during push notifications
     */
    protected CollectedTopology collectedTopology;

    /**
     * Used to pass back register values collected by various push events
     */
    protected CollectedRegisterList collectedRegisterList;

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
        return "$Date: 2020-12-29$";
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
            processSpecificEvents();
        } catch (JSONException e) {
            getContext().getLogger().log(Level.WARNING, "Exception while parsing inbound message: " + e.getMessage(), e);
        }

        return discoverResultType;
    }

    private void processSpecificEvents() throws JSONException {
        MeterProtocolEvent receivedEvent = getMeterProtocolEvent();

        if (receivedEvent == null) {
            return;
        }

        switch (receivedEvent.getProtocolCode()) {

            /**
             * Periodic event pushing networking information about the Beacon
             */
            case BEACON_HEART_BEAT:
                handleHeartBeatEvent(receivedEvent);
                break;

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

        if (collectedRegisterList != null){
            collectedRegisterList.getCollectedRegisters().stream().forEach(r->{
                String obisCode="n/a"; // some failsafe checks ... GOD knows what we can receive here...
                if (r.getRegisterIdentifier()!=null && r.getRegisterIdentifier().getRegisterObisCode()!=null){
                    obisCode = r.getRegisterIdentifier().getRegisterObisCode().toString();
                }
                if (r.getText()!=null) {
                    getContext().getLogger().info("> text register: " + obisCode + "=" + r.getText());
                }
                if (r.getCollectedQuantity() != null){
                    getContext().getLogger().info("> number register: "+obisCode+"=" +r.getCollectedQuantity().toString());
                }
            });
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

        CollectedTopology deviceTopology = extractRegisterEventBeacon10(message);

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

        if (collectedRegisterList != null){
            collectedData.add(collectedRegisterList);
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
    /**
     * Parsing the payload of an Beacon heart-beat event:
     *
     * 	"dummy1_global": "0.0.0.0",
     * 	"eth1_global": "10.78.63.113/24",
     * 	"eth1_local": "fe80:0:0:0:224:28ff:fe00:b7de%eth1/64",
     * 	"gre1_global": "0.0.0.0",
     * 	"gre1_local": "0.0.0.0",
     * 	"ppp_global": "117.231.127.224/32",
     * 	"ppp_local": "0.0.0.0",
     * 	"dns": "nameserver 218.248.112.72\nnameserver 218.248.112.6",
     * 	"total_space": "0xb6bd8",
     * 	"used_space": "0x146b8",
     * 	"free_space": "0xa123c"
     * @param receivedEvent
     */

    private void handleHeartBeatEvent(MeterProtocolEvent receivedEvent) {
        String message = extractReceivedDescription(receivedEvent);
        boolean haveWAN = false;
        boolean haveWWAN = false;

        if (message == null || message.isEmpty()) {
            return;
        }

        JSONObject json;

        try {
            json = new JSONObject(message);
            getContext().getLogger().info("Parsing heart-beat message");

            collectedRegisterList = collectedDataFactory.createCollectedRegisterList(getDeviceIdentifier());

        } catch (Exception e) {
            getContext().getLogger().log(Level.SEVERE, "Exception parsing JSON message: " + e.getMessage(), e);
            return;
        }

        try {
            /* WAN IP address (outer-ip for Ethernet connections) */
            if (json.has(HEART_BEAT_ETH_1_GLOBAL)) {
                String eth1_global = json.getString(HEART_BEAT_ETH_1_GLOBAL);
                if (validIp(eth1_global)) {
                    addCollectedRegister(G3RegisterMapper.WAN_IP_ADDRESS, eth1_global);
                    haveWAN = true;
                }
            }
        } catch (Exception e) {
            getContext().getLogger().warning("Exception parsing JSON message (WAN-IP): " + e.getMessage());
        }

        try {
            /* WWAN IP address (outer-ip for modem connections) */
            if (json.has(HEART_BEAT_PPP_GLOBAL)) {
                String ppp_global = json.getString(HEART_BEAT_PPP_GLOBAL);
                if (validIp(ppp_global)) {
                    addCollectedRegister(G3RegisterMapper.WWAN_IP_ADDRESS, ppp_global);
                    haveWWAN = true;
                }
            }
        } catch (Exception e) {
            getContext().getLogger().warning("Exception parsing JSON message (WWAN-IP): " + e.getMessage());
        }

        try {
            /* Dummy/Loopback IP address (inner-ip aka VPN ip) */
            if (json.has(HEART_BEAT_DUMMY_1_GLOBAL)) {
                String dummy_1_global_global = json.getString(HEART_BEAT_DUMMY_1_GLOBAL);
                if (validIp(dummy_1_global_global)) {
                    addCollectedRegister(G3RegisterMapper.LOOPBACK_IP_ADDRESS, dummy_1_global_global);
                }
            }
        } catch (Exception e) {
            getContext().getLogger().warning("Exception parsing JSON message (loopback-IP): " + e.getMessage());
        }

        try {
            /* DNS servers */
            if (json.has(HEART_BEAT_DNS)) {
                String dns = json.getString(HEART_BEAT_DNS);
                parseDNS(dns, haveWAN, haveWWAN);
            }
        } catch (Exception e) {
            getContext().getLogger().warning("Exception parsing JSON message (DNS): " + e.getMessage());
        }

        try {
            parseMemory(json);
        } catch (Exception e) {
            getContext().getLogger().warning("Exception parsing JSON message (memory): " + e.getMessage());
        }


    }


    private boolean validIp(String ip) {
        if (ip==null || ip.isEmpty()){
            return false;
        }

        if ("0.0.0.0".equals(ip)){
            return false;
        }

        return true;
    }

    /**
     * Parses a string like this  "nameserver 218.248.112.72\nnameserver 218.248.112.6"
     * and adds it to the WAN (or WWAN?) name-servers registers
     */
    private void parseDNS(String dns, boolean haveWAN, boolean haveWWAN) {
        dns = dns
                .trim()
                .replace("\n", " ")
                .replace(";"," ")
                .replace(","," ")
                .replace("nameserver ", " ")
                .replace("  ", " ")
                .trim()     ;
        String[] nameServers = dns.split(" ");

        if (validIp(nameServers[0])) {
            if (haveWAN) {
                addCollectedRegister(G3RegisterMapper.WAN_PRIMARY_DNS_ADDRESS, nameServers[0]);
            }
            if (haveWWAN) {
                addCollectedRegister(G3RegisterMapper.WAN_SECONDARY_DNS_ADDRESS, nameServers[0]);
            }
        }

        if (validIp(nameServers[1])) {
            if (haveWAN) {
                addCollectedRegister(G3RegisterMapper.WWAN_PRIMARY_DNS_ADDRESS, nameServers[1]);
            }
            if (haveWWAN) {
                addCollectedRegister(G3RegisterMapper.WWAN_SECONDARY_DNS_ADDRESS, nameServers[1]);
            }
        }
    }


    /**
     * Parse and add JSON attributes
     *          "total_space": "0xb6bd8",
     *       	"used_space": "0x146b8",
     *       	"free_space": "0xa123c"
     *
     *
     */
    private void parseMemory(JSONObject jsonEvent) throws JSONException {
        JSONObject memory = new JSONObject();

        String[] objectsToMove = new String[]{HEART_BEAT_TOTAL_SPACE, HEART_BEAT_USED_SPACE, HEART_BEAT_FREE_SPACE};

        for (String key : objectsToMove){
            if (jsonEvent.has(key)){
                memory.put(key, getJsonInt(jsonEvent, key));
            }
        }
        addCollectedRegister(G3RegisterMapper.MEMORY_MANAGEMENT_ATTR2, memory.toString());
    }

    /**
     * Go through the tedious process of returning some data back to ComServer
     * @param obisCode what ObisCode to save data to
     * @param value actual value
     */
    private void addCollectedRegister(ObisCode obisCode, String value){
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());
        CollectedRegister collectedRegister = collectedDataFactory.createDeviceTextRegister(registerIdentifier);

        collectedRegister.setCollectedData(value);
        collectedRegister.setReadTime(new Date());

        collectedRegisterList.addCollectedRegister(collectedRegister);
    }
}

