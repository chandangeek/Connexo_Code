package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.G3NetworkManagementAttributes;
import com.energyict.dlms.cosem.methods.G3NetworkManagementMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/20/12
 * Time: 3:39 PM
 */
public class G3NetworkManagement extends AbstractCosemObject {

    /**
     * The cached node list
     */
    private Array nodeList = null;

    /**
     * The default obisCode as used in the RTU+Server
     */
    private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.128.0.2.255");

    /**
     * @return The default obisCode as defined in the RTU+Server
     */
    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public G3NetworkManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.G3_NETWORK_MANAGEMENT.getClassId();
    }

    /**
     * The get node list from the cache or from the meter on first read
     *
     * @return The node list
     * @throws java.io.IOException If there occurred an error while reading the node list
     */
    public final Array getNodeList() throws IOException {
        if (this.nodeList == null) {
            this.nodeList = readNodeList();
        }
        return nodeList;
    }

    /**
     * Read the node list directly from the meter
     *
     * @return The node list
     */
    public final Array readNodeList() throws IOException {
        this.nodeList = readDataType(G3NetworkManagementAttributes.NODE_LIST, Array.class);
        return this.nodeList;
    }

    /**
     * @return <code>true</code> if there is a fixed GMK in use on the G3 PLC network
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final boolean readUseFixedGmk() throws IOException {
        return readDataType(G3NetworkManagementAttributes.USE_FIXED_GMK, BooleanObject.class).getState();
    }

    /**
     * @return The 16 byte fixed GMK value as byte array
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final byte[] readFixedGmk() throws IOException {
        return readDataType(G3NetworkManagementAttributes.FIXED_GMK, OctetString.class).getOctetStr();
    }

    /**
     * @return <code>true</code> if the SNR service is enabled
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final boolean readSnrServiceEnabled() throws IOException {
        return readDataType(G3NetworkManagementAttributes.SNR_ENABLED, BooleanObject.class).getState();
    }

    /**
     * @return The SNR service interval
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final long readSnrServiceInterval() throws IOException {
        return readDataType(G3NetworkManagementAttributes.SNR_INTERVAL, Unsigned32.class).getValue();
    }

    /**
     * @return The SNR service quiet time
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final int readSnrServiceQuietTime() throws IOException {
        return readDataType(G3NetworkManagementAttributes.SNR_QUIET_TIME, Unsigned8.class).getValue();
    }

    /**
     * @return The SNR service payload
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final byte[] readSnrServicePayload() throws IOException {
        return readDataType(G3NetworkManagementAttributes.SNR_PAYLOAD, OctetString.class).getOctetStr();
    }

    /**
     * @return <code>true</code> if the keep alive service is enabled
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final boolean readKeepAliveServiceEnabled() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_ENABLED, BooleanObject.class).getState();
    }

    /**
     * @return The keep alive service schedule interval in seconds
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final long readKeepAliveServiceScheduleInterval() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_SCHEDULE_INTERVAL, Unsigned32.class).getValue();
    }

    /**
     * @return The keep alive service bucket size
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final int readKeepAliveServiceBucketSize() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_BUCKET_SIZE, Unsigned16.class).getValue();
    }

    /**
     * @return The keep alive service max inactive meter time in seconds
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final long readKeepAliveServiceMaxInactiveMeterTime() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_MAX_INACTIVE_METER_TIME, Unsigned32.class).getValue();
    }

    /**
     * @return The keep alive service retries
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final int readKeepAliveServiceRetries() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_RETRIES, Unsigned8.class).getValue();
    }

    /**
     * @return The keep alive service timeout in seconds
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final int readKeepAliveServiceTimeout() throws IOException {
        return readDataType(G3NetworkManagementAttributes.KEEP_ALIVE_TIMEOUT, Unsigned16.class).getValue();
    }

    /**
     * @return <code>true<code/> if automatic route is enabled
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final boolean readAutomaticRouteManagementEnabled() throws IOException {
        return readDataType(G3NetworkManagementAttributes.AUTOMATIC_ROUTE_MANAGEMENT_ENABLED, BooleanObject.class).getState();
    }

    /**
     * Performs a ping to the given node, returns <code>true</code> if the ping was successful, <code>false</code> if it wasn't.
     *
     * @param macAddress Hex string containing the EUI-64 address of the target node.
     * @return <code>true</code> if the ping request was successful, <code>false</code> if not.
     * @throws java.io.IOException If an IO error occurs when performing the ping.
     */
    public final boolean pingNode(final String macAddress, int timeout) throws IOException {
        Structure structure = new Structure(extractEUI64(macAddress), new Unsigned16(timeout));
        return this.methodInvoke(G3NetworkManagementMethods.PING, structure, BooleanObject.class).getState();
    }

    /**
     * Extracts the EUI64 address for a node from the MAC hex string. Returns it in the form of an {@link com.energyict.dlms.axrdencoding.OctetString}.
     *
     * @param macAddress Hex string containing the MAC address.
     * @return An {@link com.energyict.dlms.axrdencoding.OctetString} containing the MAC address.
     * @throws java.io.IOException In case the passed string has an invalid format or <code>null</code>.
     */
    private static final OctetString extractEUI64(final String macAddress) throws IOException {
        if (macAddress == null) {
            throw new IOException("MAC address string should not be null !");
        }

        final byte[] eui64 = DLMSUtils.getBytesFromHexString(macAddress, null);

        if (eui64 == null || eui64.length != 8) {
            throw new IOException("MAC address passed to this method [" + macAddress + "] is invalid : expected hex string containing exactly 8 bytes, this one contains [" + (eui64 != null ? eui64.length : "UNKNOWN") + "] bytes");
        }

        return OctetString.fromByteArray(eui64);
    }

    /**
     * Requests the path to a particular node.
     *
     * @param macAddress Hex string containing the EUI64 address of the path for.
     * @return A list of EUI64 addresses representing the path to the given node.
     * @throws java.io.IOException In case of an IO error during method execution.
     */
    public final List<String> requestPath(final String macAddress) throws IOException {
        final Array response = this.methodInvoke(G3NetworkManagementMethods.PATH_REQUEST, extractEUI64(macAddress), Array.class);

        final List<String> path = new ArrayList<String>(response.nrOfDataTypes());

        for (final AbstractDataType element : response) {
            path.add((DLMSUtils.getHexStringFromBytes(((OctetString) element).getOctetStr(), "")));
        }

        return path;
    }

    /**
     * Requests a route to be formed towards the given node from the data concentrator. Results in an RREQ on the G3 NWK.
     *
     * @param macAddress Hex string containing the EUI64 address of the node to request a route to.
     * @return <code>true</code> if the route request was successful, <code>false</code> if it failed (possibly because the node
     *         was not known).
     * @throws java.io.IOException If an IO error occurs during execution of the method.
     */
    public final boolean requestRoute(final String macAddress) throws IOException {
        return this.methodInvoke(G3NetworkManagementMethods.ROUTE_REQUEST, extractEUI64(macAddress), BooleanObject.class).getState();
    }

    /**
     * Detaches the node with the given EUI64 from the network.
     *
     * @param macAddress Hex string containing the EUI64 address of the node to detach.
     * @return <code>true</code> if the node was detached successfully, <code>false</code> if it failed (possibly because the node
     *         was not known).
     * @throws java.io.IOException If an IO error occurs during execution of the method.
     */
    public final boolean detachNode(final String macAddress) throws IOException {
        return this.methodInvoke(G3NetworkManagementMethods.DETACH, extractEUI64(macAddress), BooleanObject.class).getState();
    }
}
