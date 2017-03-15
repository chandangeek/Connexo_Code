package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.LastSeenDateInfo;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
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
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    protected static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    protected boolean provideProtocolJavaClasName = true;

    /**
     * JSON keys for PLC_G3_REGISTER_NODE event
     */
    public static final String JSON_METER_IDENTIFIER = "MeterIdentifier";
    public static final String JSON_SAP_DLMS_GW = "SAP_DLMS_GW";
    public static final String JSON_SAP_DLMS_MIR = "SAP_DLMS_MIR";
    public static final String JSON_SAP_IPV_6 = "SAP_IPV6";
    public static final String JSON_SAP_IPV_4 = "SAP_IPV4";
    public static final String JSON_SAP_802_15_4_ID = "SAP_802_15_4_ID";

    /**
     * Used to pass back any topology changes observed during push notifications
     */
    protected CollectedTopology collectedTopology;

    private static final int PLC_G3_REGISTER_NODE = 0xC2;
    private static final int PLC_G3_UNREGISTER_NODE = 0xC3;
    private static final int PLC_G3_NODE_LINK_LOST = 0xCB;
    private static final int PLC_G3_NODE_LINK_RECOVERED = 0xCC;


    private enum TopologyAction {
        REMOVE,
        ADD
    }


    protected BeaconPSKProvider getPskProvider() {
        return BeaconPSKProviderFactory.getInstance(provideProtocolJavaClasName).getPSKProvider(getDeviceIdentifier(), getContext());
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG);
        }
        return parser;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        final List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(PropertySpecFactory.notNullableBooleanPropertySpec(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true));
        return optionalProperties;
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-03-14 18:03:36 +0100 (Tue, 15 Nov 2016)$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        super.addProperties(properties);
        this.provideProtocolJavaClasName = properties.<Boolean>getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
    }

    @Override
    public DiscoverResultType doDiscovery() {
        DiscoverResultType discoverResultType = super.doDiscovery();

        // do a specific Beacon post-processing of the push event
        try {
            searchForTopologyUpdateEvents();
        } catch (JSONException e) {
            getContext().getLogger().log(Level.WARNING,"Exception while parsing inbound message: "+e.getMessage(),e);
        }

        return discoverResultType;
    }

    private void searchForTopologyUpdateEvents() throws JSONException {
        MeterProtocolEvent receivedEvent = getMeterProtocolEvent();

        if (receivedEvent==null){
            return;
        }

        switch (receivedEvent.getProtocolCode()){

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
                this.collectedTopology = extractNodeInformation(receivedEvent, TopologyAction.REMOVE);
                break;

            /**
             * Generated when a node recovered from the lost state (i.e. it is reachable again after being unreachable for prolonged period of time).
             */
            case PLC_G3_NODE_LINK_RECOVERED:
                this.collectedTopology = extractNodeInformation(receivedEvent, TopologyAction.ADD);
                break;

            /**
             * Generated when node leaves the PAN.
             */
            case PLC_G3_UNREGISTER_NODE:
                this.collectedTopology = extractNodeInformation(receivedEvent, TopologyAction.REMOVE);
        }

        logWhatWeDiscovered();
    }

    private void logWhatWeDiscovered() {
        if (collectedTopology!=null) {
            if (collectedTopology.getJoinedSlaveDeviceIdentifiers()!=null) {
                getContext().getLogger().info("> joined devices: " + collectedTopology.getJoinedSlaveDeviceIdentifiers());
            }
            if (collectedTopology.getLostSlaveDeviceIdentifiers()!=null) {
                getContext().getLogger().info("> lost devices: " + collectedTopology.getLostSlaveDeviceIdentifiers());
            }
            if (collectedTopology.getAdditionalCollectedDeviceInfo()!=null) {
                getContext().getLogger().info("> device parameters: " + collectedTopology.getAdditionalCollectedDeviceInfo().toString());
            }
        }
    }

    private CollectedTopology extractNodeInformation(MeterProtocolEvent receivedEvent, TopologyAction action) {
        String macAddress = receivedEvent.getMessage();
        if (macAddress==null || macAddress.length()==0){
            return null;
        }

        macAddress = macAddress.replace(":","").replace(".","");
        DeviceIdentifier slaveDeviceIdentified = new DialHomeIdDeviceIdentifier(macAddress);

        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());

        switch (action){
            case ADD:
                LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo(G3Properties.PROP_LASTSEENDATE, getNow());
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
     *
     * Received message contains JSON structure with EUI-64 of the meter, and list of possible service access points for that meter.
     * Included SAP list depends on the configuration.
     *
     * Keys present in the JSON include:
     *      MeterIdentifier: holds the EUI-64 of the meter
     *      SAP_802_15_4_ID: short address of the meter on the PAN
     *      SAP_DLMS_GW: DLMS gateway virtual logical device ID (if the gateway is enabled)
     *      SAP_DLMS_MIR: DLMS data-concentrator mirrored logical device (if the meter is scheduled for readout)
     *      SAP_IPV6: Routable IPv6 address of the meter (if the border routing functionality is enabled)
     *      SAP_IPV4: Routable IPv4 address of the meter (if the border routing functionality is enabled)
     */
    public CollectedTopology extractTopologyUpdateFromRegisterEvent(MeterProtocolEvent receivedEvent) throws JSONException {
        JSONObject json = new JSONObject(receivedEvent.getMessage());
        DeviceIdentifier slaveDeviceIdentifier = null;

        CollectedTopology deviceTopology;

        if (json.has(JSON_METER_IDENTIFIER)) {
            String meterIdentifier = json.get(JSON_METER_IDENTIFIER).toString();
            String macAddress = meterIdentifier.replace(":", "").replace(".", "");
            slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
        }

        if (slaveDeviceIdentifier == null){
            // no information about a slave device
            return null;
        }

        // we have a slave device, so create the topology object to be filled in
        deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());

        BigDecimal lastSeenDate = getNow();
        LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo(G3Properties.PROP_LASTSEENDATE, lastSeenDate);
        deviceTopology.addJoinedSlaveDevice(slaveDeviceIdentifier, lastSeenDateInfo);

        if (json.has(JSON_SAP_DLMS_GW)) {
            int SAP_DLMS_GW = getJsonInt(json, JSON_SAP_DLMS_GW);
            if (SAP_DLMS_GW > 0) {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, SAP_DLMS_GW
                        )
                );
            }
        }


        if (json.has(JSON_SAP_DLMS_MIR)){
            int SAP_DLMS_MIR = getJsonInt(json, JSON_SAP_DLMS_MIR);
            if (SAP_DLMS_MIR > 0){
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, SAP_DLMS_MIR
                        )
                );
            }
        }

        if (json.has(JSON_SAP_IPV_6)){
            String SAP_IPV6 = json.getString(JSON_SAP_IPV_6);
            if (SAP_IPV6 != null && SAP_IPV6.length() > 0){
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier, AM540ConfigurationSupport.IP_V6_ADDRESS, SAP_IPV6
                        )
                );
            }
        }


        if (json.has(JSON_SAP_IPV_4)){
            String SAP_IPV4 = json.getString(JSON_SAP_IPV_4);
            if (SAP_IPV4 != null && SAP_IPV4.length() > 0){
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,  AM540ConfigurationSupport.IP_V4_ADDRESS, SAP_IPV4
                        )
                );
            }
        }

        if (json.has(JSON_SAP_802_15_4_ID)){
            int SAP_802_15_4_ID = getJsonInt(json, JSON_SAP_802_15_4_ID);
            if (SAP_802_15_4_ID > 0){
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,  AM540ConfigurationSupport.SHORT_ADDRESS_PAN, SAP_802_15_4_ID
                        )
                );
            }
        }

        deviceTopology.addAdditionalCollectedDeviceInfo(
                MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                        slaveDeviceIdentifier,
                        G3Properties.PROP_LASTSEENDATE,
                        lastSeenDate
                )
        );

        return deviceTopology;
    }

    private int getJsonInt(JSONObject json, String key) throws JSONException {
        String value = json.getString(key);
        if (value.contains("0x")){
            return Integer.parseInt(value.replace("0x",""), 16);
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
        if (collectedTopology != null){
            collectedData.add(collectedTopology);
        }

        return collectedData;
    }


}