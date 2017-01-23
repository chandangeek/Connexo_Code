package com.energyict.dlms.cosem;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.Ipv6SetupAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * <p>
 * Straightforward implementation of the IPv6 Setup class defined according to BlueBook 12
 * </p>
 * <p>
 * Information from BlueBook:
 * <p>
 * The IPv6 setup IC allows modelling the setup of the IPv6 layer, handling all information
 * related to the IPv6 address settings associated to a given device and to a lower layer
 * connection on which these settings are used.
 * <br/><br/>
 * There shall be an instance of this IC in a device for each different network interface
 * implemented. For example, if a device has two interfaces (using the UDP/IP and/or TCP/IP
 * profile on both of them), there shall be two instances of the IPv6 setup IC in that device: one
 * for each of these interfaces.
 * </p>
 * <p>
 * Copyrights EnergyICT
 * Date: 1/6/15
 * Time: 8:46 AM
 */
public class IPv6Setup extends AbstractCosemObject {

    /**
     * The <b>Default</b> logical Device Name of the IPv6Setup Object (0.0.25.7.0.255)
     */
    static final byte[] LN = new byte[]{0, 0, 25, 7, 0, (byte) 255};

    /**
     * Reference to the DataLink layer setup Object by it's logical name
     */
    private OctetString dl_Reference = null;

    /**
     * Defines the IPv6 address configuration mode. Possible values:
     * <ul>
     * <li>0 - Automatic configuration (default)</li>
     * <li>1 - DHCPv6</li>
     * <li>2 - Manual</li>
     * <li>3 - ND (Neighbour discovery</li>
     * </ul>
     */
    private TypeEnum addressConfigMode = null;

    /**
     * Carries unicast IPv6 address(es) assigned to the related interface of the
     * physical device on the network (unique local unicast, link local unicast and /
     * or global unicast addresses). An IPv6 address can be either (static) or
     * (dynamic) or both.
     */
    private Array unicastIPv6Addresses = null;

    /**
     * Contains an array of IPv6 addresses used for multicast.
     */
    private Array multicastIPv6Addresses = null;

    /**
     * Contains the IPv6 addresses of the IPv6 gateway device.
     */
    private Array gatewayIPv6Addresses = null;

    /**
     * Contains the IPv6 address of the primary Domain Name Server (DNS).
     * If no IPv6 address is assigned, the length of the octet-string shall be 0
     */
    private OctetString primaryDNSAddress = null;

    /**
     * Contains the IPv6 address of the secondary Domain Name Server (DNS).
     * If no IPv6 address is assigned, the length of the octet-string shall be 0.
     */
    private OctetString secondaryDNSAddress = null;

    /**
     * Contains the traffic class element of the IPv6 header. The 6 most-significant
     * bits are used for DSCP (Differentiated Services Codepoint), which is used to
     * classify packets as specified in RFC 2474, clause 3.
     */
    private Unsigned8 trafficClass = null;

    /**
     * Contains the configuration to be used for both routers and hosts to support
     * the Neighbor Discovery protocol for IPv6
     */
    private Array neighborDiscoverySetup = null;

    public IPv6Setup(ProtocolLink protocolLink){
        super(protocolLink,new ObjectReference(LN));
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public IPv6Setup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.IPV6_SETUP.getClassId();
    }

    /**
     * The current ObisCode
     */
    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    /**
     * Read the {@link #dl_Reference} from the device
     */
    public OctetString readDLReference() throws IOException {
        this.dl_Reference = OctetString.fromByteArray(getResponseData(Ipv6SetupAttributes.DL_REFERENCE));
        return this.dl_Reference;
    }

    /**
     * Getter for the dl_Reference. If the object is null read if from the device
     */
    public OctetString getDLReference() throws IOException {
        if (this.dl_Reference == null) {
            return readDLReference();
        }
        return this.dl_Reference;
    }

    /**
     * Write the {@link #dl_Reference} to the device
     */
    public void writeDLReference(OctetString dlReference) throws IOException {
        write(Ipv6SetupAttributes.DL_REFERENCE, dlReference.getBEREncodedByteArray());
        this.dl_Reference = dlReference;
    }

    /**
     * Read the unicast IPv6 from the device
     */
    public Array readUnicastIpv6Addresses() throws IOException {
        this.unicastIPv6Addresses = (Array) AXDRDecoder.decode(getResponseData(Ipv6SetupAttributes.UNICAST_IPV6_ADDRESSES));
        return this.unicastIPv6Addresses;
    }

    /**
     * Lazy getter for the unicast IPv6 address
     */
    public Array getUnicastIPv6Addresses() throws IOException {
        if (this.unicastIPv6Addresses == null) {
            return readUnicastIpv6Addresses();
        }
        return this.unicastIPv6Addresses;
    }

    public String getFormattedIPv6Address(OctetString ipv6Address){
        String unformatted = ProtocolUtils.outputHexString(ipv6Address.getOctetStr());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 32; i=i+4) {
            if(i>0){
                stringBuilder.append(":");
            }
            stringBuilder.append(unformatted.substring(i, i+4));
        }
        return stringBuilder.toString();
    }

    /**
     * Setter for the unicast IPv6 addresses
     */
    public void writeicastIpv6Address(Array unicastIPv6Addresses) throws IOException {
        write(Ipv6SetupAttributes.UNICAST_IPV6_ADDRESSES, unicastIPv6Addresses.getBEREncodedByteArray());
        this.unicastIPv6Addresses = unicastIPv6Addresses;
    }

    /**
     * Read the multicast IPv6 from the device
     */
    public Array readMulticastIpv6Addresses() throws IOException {
        this.multicastIPv6Addresses = (Array) AXDRDecoder.decode(getResponseData(Ipv6SetupAttributes.MULTICAST_IPV6_ADDRESSES));
        return this.multicastIPv6Addresses;
    }

    /**
     * Lazy getter for the multicast IPv6 address
     */
    public Array getMulticastIPv6Addresses() throws IOException {
        if (this.multicastIPv6Addresses == null) {
            return readMulticastIpv6Addresses();
        }
        return this.multicastIPv6Addresses;
    }

    /**
     * Setter for the multicast IPv6 addresses
     */
    public void writeMulticastIpv6Address(Array multicastIpv6Addresses) throws IOException {
        write(Ipv6SetupAttributes.MULTICAST_IPV6_ADDRESSES, multicastIpv6Addresses.getBEREncodedByteArray());
        this.multicastIPv6Addresses = multicastIpv6Addresses;
    }


    /**
     * Read the gateway IPv6 from the device
     */
    public Array readGatewayIpv6Addresses() throws IOException {
        this.gatewayIPv6Addresses = (Array) AXDRDecoder.decode(getResponseData(Ipv6SetupAttributes.GATEWAY_IPV6_ADDRESSES));
        return this.gatewayIPv6Addresses;
    }

    /**
     * Lazy getter for the gateway IPv6 address
     */
    public Array getGatewayIPv6Addresses() throws IOException {
        if (this.gatewayIPv6Addresses == null) {
            return readGatewayIpv6Addresses();
        }
        return this.gatewayIPv6Addresses;
    }

    /**
     * Setter for the gateway IPv6 addresses
     */
    public void writeGatewayIpv6Address(Array gatewayIpv6Addresses) throws IOException {
        write(Ipv6SetupAttributes.GATEWAY_IPV6_ADDRESSES, gatewayIpv6Addresses.getBEREncodedByteArray());
        this.gatewayIPv6Addresses = gatewayIpv6Addresses;
    }


    /**
     * Read the primary DNS Address from the device
     */
    public OctetString readPrimaryDNSAddress() throws IOException {
        this.primaryDNSAddress = OctetString.fromByteArray(getResponseData(Ipv6SetupAttributes.PRIMARY_DNS_ADDRESS));
        return this.primaryDNSAddress;
    }

    /**
     * Getter for the primary DNS Address. If the object is null read if from the device
     */
    public OctetString getPrimaryDNSAddress() throws IOException {
        if (this.primaryDNSAddress == null) {
            return readPrimaryDNSAddress();
        }
        return this.primaryDNSAddress;
    }

    /**
     * Write the primaryDNSAddress to the device
     */
    public void writePrimaryDNSAddress(OctetString primaryDNSAddress) throws IOException {
        write(Ipv6SetupAttributes.PRIMARY_DNS_ADDRESS, primaryDNSAddress.getBEREncodedByteArray());
        this.primaryDNSAddress = primaryDNSAddress;
    }

    /**
     * Read the secondary DNS Address from the device
     */
    public OctetString readSecondaryDNSAddress() throws IOException {
        this.secondaryDNSAddress = OctetString.fromByteArray(getResponseData(Ipv6SetupAttributes.SECONDARY_DNS_ADDRESS));
        return this.secondaryDNSAddress;
    }

    /**
     * Getter for the secondary DNS Address. If the object is null read if from the device
     */
    public OctetString getSecondaryDNSAddress() throws IOException {
        if (this.secondaryDNSAddress == null) {
            return readSecondaryDNSAddress();
        }
        return this.secondaryDNSAddress;
    }

    /**
     * Write the secondaryDNSAddress to the device
     */
    public void writeSecondaryDNSAddress(OctetString secondaryDNSAddress) throws IOException {
        write(Ipv6SetupAttributes.SECONDARY_DNS_ADDRESS, secondaryDNSAddress.getBEREncodedByteArray());
        this.secondaryDNSAddress = secondaryDNSAddress;
    }

    /**
     * Read the traffic class from the device
     */
    public Unsigned8 readTrafficClass() throws IOException {
        this.trafficClass = (Unsigned8) AXDRDecoder.decode(getResponseData(Ipv6SetupAttributes.TRAFFIC_CLASS));
        return this.trafficClass;
    }

    /**
     * Lazy cached getter for the traffic class
     */
    public Unsigned8 getTrafficClass() throws IOException {
        if (this.trafficClass == null) {
            return readTrafficClass();
        }
        return this.trafficClass;
    }

    /**
     * Setter for the traffic class
     */
    public void writeTrafficClass(Unsigned8 trafficClass) throws IOException {
        write(Ipv6SetupAttributes.TRAFFIC_CLASS, trafficClass.getBEREncodedByteArray());
        this.trafficClass = trafficClass;
    }



    /**
     * Read the neighbour discovery setup from the device
     */
    public Array readNeighbourDiscoverySetup() throws IOException {
        this.neighborDiscoverySetup = (Array) AXDRDecoder.decode(getResponseData(Ipv6SetupAttributes.NEIGHBOUR_DISCOVERY_SETUP));
        return this.neighborDiscoverySetup;
    }

    /**
     * Lazy getter for the neighbourDiscoverySetup
     */
    public Array getNeighborDiscoverySetup() throws IOException {
        if (this.neighborDiscoverySetup == null) {
            return readNeighbourDiscoverySetup();
        }
        return this.neighborDiscoverySetup;
    }

    /**
     * Setter for the neighbourDiscoverySetup
     */
    public void writeNeighbourDiscoverySetup(Array neighbourDiscoverySetup) throws IOException {
        write(Ipv6SetupAttributes.NEIGHBOUR_DISCOVERY_SETUP, neighbourDiscoverySetup.getBEREncodedByteArray());
        this.neighborDiscoverySetup = neighbourDiscoverySetup;
    }
}
