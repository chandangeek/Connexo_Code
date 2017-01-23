package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.NetworkManagementAttributes;
import com.energyict.dlms.cosem.methods.NetworkManagementMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class NetworkManagement extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.5.255");

    private NetworkMgmtParameters networkMgmtParameters;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public NetworkManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.NETWORK_MANAGEMENT.getClassId();
    }

    /**
     * Getter for the network management parameters
     * @return the network management parameters
     * @throws IOException
     */
    public NetworkMgmtParameters getNetworkMgmtParameters() throws IOException {
        if (this.networkMgmtParameters == null) {
            readNetworkMgmtParameters();
        }
        return networkMgmtParameters;
    }

    /**
     * Read the network management parameters from the device
     *
     * @return the network management parameters
     * @throws IOException
     */
    private NetworkMgmtParameters readNetworkMgmtParameters() throws IOException {
        this.networkMgmtParameters = new NetworkMgmtParameters(new Structure(getResponseData(NetworkManagementAttributes.NETWORK_MGMT_PARAMETERS), 0, 0));
        return this.networkMgmtParameters;
    }

    /**
     * Getter for the discover duration
     * @return the discovery process duration in minutes
     * @throws IOException
     */
    public Unsigned32 getDiscoverDuration() throws IOException {
        return getNetworkMgmtParameters().getDiscoverDuration();
    }

    /**
     * Getter for the discover interval
     *
     * @return the discovery process interval in hours
     * @throws IOException
     */
    public Unsigned8 getDiscoverInterval() throws IOException {
        return getNetworkMgmtParameters().getDiscoverInterval();
    }

    /**
     * Getter for the repeater call interval
     *
     * @return the repeater call interval in minutes
     * @throws IOException
     */
    public Unsigned32 getRepeaterCallInterval() throws IOException {
        return getNetworkMgmtParameters().getRepeaterCallInterval();
    }

    /**
     * Getter for the repeater call threshold
     *
     * @return the repeater call threshold in dBV
     * @throws IOException
     */
    public Unsigned16 getRepeaterCallThreshold() throws IOException {
        return getNetworkMgmtParameters().getRepeaterCallThreshold();
    }

    /**
     * Getter for the repeater call timeslots
     *
     * @return the the number of time slots reserved for new meters during the repeater call
     * @throws IOException
     */
    public Unsigned8 getRepeaterCallTimeslots() throws IOException {
       return getNetworkMgmtParameters().getRepeaterCallTimeslots();
    }

    /**
     * Setter for the network Management Parameters Structure
     *
     * @param networkMgmtParametersStructure the Network Management Parameters to write
     * @throws IOException
     */
    public void writeNetworkMgmtParameters(Structure networkMgmtParametersStructure) throws IOException {
        write(NetworkManagementAttributes.NETWORK_MGMT_PARAMETERS, networkMgmtParametersStructure.getBEREncodedByteArray());
        this.networkMgmtParameters = new NetworkMgmtParameters(networkMgmtParametersStructure);
    }

    /**
     * Run new meter discovery on demand
     *
     * @throws java.io.IOException
     */
    public final void runMeterDiscovery() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_METER_DISCOVERY);
    }

    /**
     * Run alarm meter discovery on demand
     *
     * @throws java.io.IOException
     */
    public final void runAlarmMeterDiscovery() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_ALARM_METER_DISCOVERY);
    }

    /**
     * Run repeater call  on demand
     *
     * @throws java.io.IOException
     */
    public final void runRepeaterCall() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_REPEATER_CALL);
    }

    public class NetworkMgmtParameters extends Structure {

        private Unsigned32 discoverDuration;
        private Unsigned8 discoverInterval;
        private Unsigned32 repeaterCallInterval;
        private Unsigned16 repeaterCallThreshold;
        private Unsigned8 repeaterCallTimeslots;

        private NetworkMgmtParameters(Structure structure) throws IOException {
            if (structure.nrOfDataTypes() != 5) {
                throw new IOException("NetworkMgmtParameters, Structure has invalid length");
            }

            try {
                this.discoverDuration = (Unsigned32) structure.getNextDataType();
                this.discoverInterval = (Unsigned8) structure.getNextDataType();
                this.repeaterCallInterval = (Unsigned32) structure.getNextDataType();
                this.repeaterCallThreshold = (Unsigned16) structure.getNextDataType();
                this.repeaterCallTimeslots = (Unsigned8) structure.getNextDataType();
            } catch (ClassCastException e) {
                throw new IOException("NetworkMgmtParameters, Failed to parse the structure");
            }
        }

        public Unsigned32 getDiscoverDuration() {
            return discoverDuration;
        }

        public Unsigned8 getDiscoverInterval() {
            return discoverInterval;
        }

        public Unsigned32 getRepeaterCallInterval() {
            return repeaterCallInterval;
        }

        public Unsigned16 getRepeaterCallThreshold() {
            return repeaterCallThreshold;
        }

        public Unsigned8 getRepeaterCallTimeslots() {
            return repeaterCallTimeslots;
        }
    }
}
