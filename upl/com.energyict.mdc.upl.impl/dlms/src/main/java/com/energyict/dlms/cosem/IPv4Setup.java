/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.Ipv4SetupAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;

/**
 * <p>
 * This allows modelling the setup of the IPv4 layer, handling all information is related to the IP
 * Address settings associated to a given device and to a lower layer connection on which these
 * settings are used.
 * There shall be an instance of this IC in a device  for each different network interface implemented.
 * For example, if a device has two interfaces (using  the TCP/IP profile on both of them), there shall
 * be two instances of the IPv4 setup IC in that device: one for each of these interfaces.
 * </p>
 *
 * @author gna
 */
public class IPv4Setup extends AbstractCosemObject {

    /**
     * Reference to the DataLink layer setup Object by it's logical name
     */
    private OctetString dl_Reference = null;
    /**
     * Carries the value of the IP address of this physical device on the network to which the device is connected
     */
    private Unsigned32 ipAddress = null;
    /**
     * Contains an array of IP addresses
     */
    private Array multicastIPAddress = null;
    /**
     * Contains the necessary parameters to support the selected IP options
     */
    private Array ipOptions = null;
    /**
     * Contains the subnet mask
     */
    private Unsigned32 subnetMask = null;
    /**
     * Contains the IP address of the gateway device
     */
    private Unsigned32 gatewayIPAddress = null;
    /**
     * Indication whether DHCP is used
     */
    private BooleanObject useDHCPFlag = null;                //TODO
    /**
     * The IP Address of the primary Domain Name Server (DNS).
     */
    private Unsigned32 primaryDNSAddress = null;
    /**
     * The IP Address of the secondary Domain Name Server (DNS).
     */
    private Unsigned32 secondaryDNSAddress = null;

    /**
     * The <b>Default</b> logical Device Name of the IPv4Setup Object (0.0.25.1.0.255)
     */
    static final byte[] LN = new byte[]{0, 0, 25, 1, 0, (byte) 255};

    /**
     * Constructor for the object with the default {@link #LN} Logical Name
     */
    public IPv4Setup(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
    }

    /**
     * Constructor for the object with a given ObjectReference(including Logical Name)
     */
    public IPv4Setup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * The current ObisCode
     */
    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    /**
     * @return the classId of the IPv4Setup object
     */
    protected int getClassId() {
        return DLMSClassId.IPV4_SETUP.getClassId();
    }

    /**
     * Read the {@link #dl_Reference} from the device
     */
    public OctetString readDLReference() throws IOException {
        this.dl_Reference = OctetString.fromByteArray(getResponseData(Ipv4SetupAttributes.DL_REFERENCE));
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
        write(Ipv4SetupAttributes.DL_REFERENCE, dlReference.getBEREncodedByteArray());
        this.dl_Reference = dlReference;
    }

    /**
     * Read the IP-address from the device
     *
     * @return the IP-address as a double-long-unsigned
     * @throws java.io.IOException if we failed to read the Ip-address
     */
    public Unsigned32 readIPAddress() throws IOException {
        this.ipAddress = new Unsigned32(getResponseData(Ipv4SetupAttributes.IP_ADDRESS), 0);
        return this.ipAddress;
    }

    /**
     * Getter for the IP address
     *
     * @return the IP address in doted notation ( A.B.C.D.E.F)
     * @throws java.io.IOException
     */
    public String getIPAddress() throws IOException {
        StringBuffer builder = new StringBuffer();
        if (this.ipAddress == null) {
            readIPAddress();
        }
        if (this.ipAddress != null) {
            for (int i = 1; i < this.ipAddress.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append(".");
                }
                builder.append(Integer.toString(this.ipAddress.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct IP-address");
        }
    }

    /**
     * Setter for the IP address
     *
     * @param newIp the IP address to set in dotted notation
     * @throws java.io.IOException
     */
    public void setIPAddress(String newIp) throws IOException {
        int pointer = 0;
        byte[] ipByte = new byte[5];
        ipByte[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        for (int i = 1; i < ipByte.length; i++) {
            ipByte[i] = (byte) Integer.parseInt(newIp.substring(pointer, (newIp.indexOf(".", pointer) == -1) ? newIp.length() : newIp.indexOf(".", pointer)));
            pointer = newIp.indexOf(".", pointer) + 1;
        }
        writeIPAddress(new Unsigned32(ipByte, 0));
    }

    /**
     * Setter for th IP address
     *
     * @param newIp the IP address to set as an {@link com.energyict.dlms.axrdencoding.Unsigned32}
     * @throws java.io.IOException
     */
    public void writeIPAddress(Unsigned32 newIp) throws IOException {
        write(Ipv4SetupAttributes.IP_ADDRESS, newIp.getBEREncodedByteArray());
        this.ipAddress = newIp;
    }

    public static void main(String args[]) {
        try {
            String str = "10.0.0.214";
            IPv4Setup ipv4 = new IPv4Setup(null, null);
            ipv4.setIPAddress(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Read the MulticastIp address list from the device
     *
     * @return an Array of multicast IP addresses
     * @throws java.io.IOException
     */
    public Array readMulticastIPAddress() throws IOException {
        this.multicastIPAddress = (Array) AXDRDecoder.decode(getResponseData(Ipv4SetupAttributes.MULTICAST_IP_ADDRESS));
        return this.multicastIPAddress;
    }

    /**
     * Getter for the Multicast IP address. If the array is null then it is read from the device
     *
     * @return an Array of multicast IP addresses
     * @throws java.io.IOException
     */
    public Array getMulticastIPAddress() throws IOException {
        if (this.multicastIPAddress == null) {
            return readMulticastIPAddress();
        }
        return this.multicastIPAddress;
    }

    /**
     * Setter for the Multicast IP addresses
     *
     * @param multicastIPAddress the array of IP addresses to set
     * @throws java.io.IOException
     */
    public void writeMulticastIPAddress(Array multicastIPAddress) throws IOException {
        write(Ipv4SetupAttributes.MULTICAST_IP_ADDRESS, multicastIPAddress.getBEREncodedByteArray());
        this.multicastIPAddress = multicastIPAddress;
    }

    /**
     * Read the IP options from the device
     *
     * @return the array of IP Options
     * @throws java.io.IOException
     */
    public Array readIPOptions() throws IOException {
        this.ipOptions = (Array) AXDRDecoder.decode(getResponseData(Ipv4SetupAttributes.IP_OPTIONS));
        return this.ipOptions;
    }

    /**
     * Get the IP options, if the IP options is null then read it from the device
     *
     * @return the array of IP Options
     * @throws java.io.IOException
     */
    public Array getIPOptions() throws IOException {
        if (this.ipOptions == null) {
            return readIPOptions();
        }
        return this.ipOptions;
    }

    /**
     * Setter for the IP Options
     *
     * @param ipOptions the IP Options to set
     * @throws java.io.IOException
     */
    public void writeIPOptions(Array ipOptions) throws IOException {
        write(Ipv4SetupAttributes.IP_OPTIONS, ipOptions.getBEREncodedByteArray());
        this.ipOptions = ipOptions;
    }

    /**
     * Getter for the subnetMaks. Will always be read from the device
     *
     * @return the Subnetmask as an Unsigned32
     * @throws java.io.IOException
     */
    public Unsigned32 readSubnetMask() throws IOException {
        this.subnetMask = new Unsigned32(getResponseData(Ipv4SetupAttributes.SUBNET_MASK), 0);
        return this.subnetMask;
    }

    /**
     * Getter for the SubnetMask. If the mask is null then we read it from the device
     *
     * @return the subnetmask in dotted notation
     * @throws java.io.IOException
     */
    public String getSubnetMask() throws IOException {
        if (this.subnetMask == null) {
            readSubnetMask();
        }
        if (this.subnetMask != null) {
            StringBuffer builder = new StringBuffer();
            for (int i = 1; i < this.subnetMask.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append(".");
                }
                builder.append(Integer.toString(this.subnetMask.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct subnetmask.");
        }
    }

    /**
     * Setter for the subnetmask
     *
     * @param subnetMask the subnetmask to set
     * @throws java.io.IOException
     */
    public void writeSubnetMask(Unsigned32 subnetMask) throws IOException {
        write(Ipv4SetupAttributes.SUBNET_MASK, subnetMask.getBEREncodedByteArray());
        this.subnetMask = subnetMask;
    }

    /**
     * Setter for the subnetmask in dotted notation
     *
     * @param subnetMask the subnetmask to set
     * @throws java.io.IOException
     */
    public void setSubnetMask(String subnetMask) throws IOException {
        int pointer = 0;
        byte[] subnetByte = new byte[5];
        subnetByte[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        for (int i = 1; i < subnetByte.length; i++) {
            subnetByte[i] = (byte) Integer.parseInt(subnetMask.substring(pointer, (subnetMask.indexOf(".", pointer) == -1) ? subnetMask.length() : subnetMask.indexOf(".", pointer)));
            pointer = subnetMask.indexOf(".", pointer) + 1;
        }
        writeSubnetMask(new Unsigned32(subnetByte, 0));
    }

    /**
     * Getter for the GateWay IP address. Address will always be read from the device
     *
     * @return the gateWay Ip address
     * @throws java.io.IOException
     */
    public Unsigned32 readGatewayIPAddress() throws IOException {
        this.gatewayIPAddress = new Unsigned32(getResponseData(Ipv4SetupAttributes.GATEWAY_IP_ADDRESS), 0);
        return this.gatewayIPAddress;
    }

    /**
     * Getter for the GateWay IP Address. If the gateway IP address is null then we read it from the device.
     *
     * @return the gateway Ip address in dotted notation
     * @throws java.io.IOException
     */
    public String getGatewayIPAddress() throws IOException {
        if (this.gatewayIPAddress == null) {
            readGatewayIPAddress();
        }
        if (this.gatewayIPAddress != null) {
            StringBuffer builder = new StringBuffer();
            for (int i = 1; i < this.gatewayIPAddress.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append(".");
                }
                builder.append(Integer.toString(this.gatewayIPAddress.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct gateWay IP address");
        }
    }

    /**
     * Setter for the GateWayIP address
     *
     * @param gatewayIPAddress the gateWay IP Address to set
     * @throws java.io.IOException
     */
    public void writeGatewayIPAddress(Unsigned32 gatewayIPAddress) throws IOException {
        write(Ipv4SetupAttributes.GATEWAY_IP_ADDRESS, gatewayIPAddress.getBEREncodedByteArray());
        this.gatewayIPAddress = gatewayIPAddress;
    }

    /**
     * Setter for the gateway IP address
     *
     * @param gatewayIPAddress the gateway IP address in dotted notation
     * @throws java.io.IOException
     */
    public void setGatewayIPAddress(String gatewayIPAddress) throws IOException {
        int pointer = 0;
        byte[] gatewayByte = new byte[5];
        gatewayByte[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        for (int i = 1; i < gatewayByte.length; i++) {
            gatewayByte[i] = (byte) Integer.parseInt(gatewayIPAddress.substring(pointer, (gatewayIPAddress.indexOf(".", pointer) == -1) ? gatewayIPAddress.length() : gatewayIPAddress.indexOf(".", pointer)));
            pointer = gatewayIPAddress.indexOf(".", pointer) + 1;
        }
        writeGatewayIPAddress(new Unsigned32(gatewayByte, 0));
    }

    /**
     * Getter for the primary DNS address. The address will always be read from the device
     *
     * @return the primary DNS address
     * @throws java.io.IOException
     */
    public Unsigned32 readPrimaryDNSAddress() throws IOException {
        this.primaryDNSAddress = new Unsigned32(getResponseData(Ipv4SetupAttributes.PRIMARY_DNS_ADDRESS), 0);
        return this.primaryDNSAddress;
    }

    /**
     * Getter for the primary DNS address. If the address is null then it will be read from the device
     *
     * @return the primary DNS address
     * @throws java.io.IOException
     */
    public String getPrimaryDNSAddress() throws IOException {
        if (this.primaryDNSAddress == null) {
            readPrimaryDNSAddress();
        }
        if (this.primaryDNSAddress != null) {
            StringBuffer builder = new StringBuffer();
            for (int i = 1; i < this.primaryDNSAddress.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append(".");
                }
                builder.append(Integer.toString(this.primaryDNSAddress.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct primary DNS Address.");
        }
    }

    /**
     * Setter for the primary DNS address
     *
     * @param primaryDNSAddress the primary DNS address
     * @throws java.io.IOException
     */
    public void writePrimaryDNSAddress(Unsigned32 primaryDNSAddress) throws IOException {
        write(Ipv4SetupAttributes.PRIMARY_DNS_ADDRESS, primaryDNSAddress.getBEREncodedByteArray());
        this.primaryDNSAddress = primaryDNSAddress;
    }

    /**
     * Setter for the primary DNS address
     *
     * @param primaryDNSAddress the primary DNS address in dotted notation
     * @throws java.io.IOException
     */
    public void setPrimaryDNSAddress(String primaryDNSAddress) throws IOException {
        int pointer = 0;
        byte[] primaryDNSByte = new byte[5];
        primaryDNSByte[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        for (int i = 1; i < primaryDNSByte.length; i++) {
            primaryDNSByte[i] = (byte) Integer.parseInt(primaryDNSAddress.substring(pointer, (primaryDNSAddress.indexOf(".", pointer) == -1) ? primaryDNSAddress.length() : primaryDNSAddress.indexOf(".", pointer)));
            pointer = primaryDNSAddress.indexOf(".", pointer) + 1;
        }
        writePrimaryDNSAddress(new Unsigned32(primaryDNSByte, 0));
    }

    /**
     * Getter for the secondary DNS address. The address will always be read from the device
     *
     * @return the secondary DNS address
     * @throws java.io.IOException
     */
    public Unsigned32 readSecondaryDNSAddress() throws IOException {
        this.secondaryDNSAddress = new Unsigned32(getResponseData(Ipv4SetupAttributes.SECONDARY_DNS_ADDRESS), 0);
        return this.secondaryDNSAddress;
    }

    /**
     * Getter for the secondary DNS address. If the address is null it will be read from the device.
     *
     * @return the secondary DNS address in dotted notation
     * @throws java.io.IOException
     */
    public String getSecondaryDNSAddress() throws IOException {
        if (this.secondaryDNSAddress == null) {
            readSecondaryDNSAddress();
        }
        if (this.secondaryDNSAddress != null) {
            StringBuffer builder = new StringBuffer();
            for (int i = 1; i < this.secondaryDNSAddress.getBEREncodedByteArray().length; i++) {
                if (i != 1) {
                    builder.append(".");
                }
                builder.append(Integer.toString(this.secondaryDNSAddress.getBEREncodedByteArray()[i] & 0xff));
            }
            return builder.toString();
        } else {
            throw new ProtocolException("Could not get a correct secondary DNS address.");
        }
    }

    /**
     * Setter for the secondary DNS address
     *
     * @param secondaryDNSAddress the secondary DNS address to set
     * @throws java.io.IOException
     */
    public void writeSecondaryDNSAddress(Unsigned32 secondaryDNSAddress) throws IOException {
        write(Ipv4SetupAttributes.SECONDARY_DNS_ADDRESS, secondaryDNSAddress.getBEREncodedByteArray());
        this.secondaryDNSAddress = secondaryDNSAddress;
    }

    /**
     * Setter for the secondary DNS address.
     *
     * @param secondaryDNSAddress the secondary DNS address in dotted notation.
     * @throws java.io.IOException
     */
    public void setSecondaryDNSAddress(String secondaryDNSAddress) throws IOException {
        int pointer = 0;
        byte[] secondaryDNSByte = new byte[5];
        secondaryDNSByte[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        for (int i = 1; i < secondaryDNSByte.length; i++) {
            secondaryDNSByte[i] = (byte) Integer.parseInt(secondaryDNSAddress.substring(pointer, (secondaryDNSAddress.indexOf(".", pointer) == -1) ? secondaryDNSAddress.length() : secondaryDNSAddress.indexOf(".", pointer)));
            pointer = secondaryDNSAddress.indexOf(".", pointer) + 1;
        }
        writeSecondaryDNSAddress(new Unsigned32(secondaryDNSByte, 0));
    }

    /**
     * Getter for the DHCP flag. Will always be read from the device
     *
     * @return
     */
    public BooleanObject readDHCPFlag() throws IOException {
        this.useDHCPFlag = new BooleanObject(getResponseData(Ipv4SetupAttributes.USE_DHCP_FLAG), 0);
        return this.useDHCPFlag;
    }

    /**
     * Getter for the DHCP flag. If the flag is null then we will read it from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public boolean getDHCPFlag() throws IOException {
        if (this.useDHCPFlag == null) {
            readDHCPFlag();
        }
        if (this.useDHCPFlag != null) {
            return this.useDHCPFlag.getState();
        } else {
            throw new ProtocolException("Could not correctly read the DHCP flag.");
        }
    }

    /**
     * Setter for the DHCPflag.
     *
     * @param dhcpFlag the DHCP flag to set as an AXDR encoded boolean
     * @throws java.io.IOException
     */
    public void writeDHCPFlag(BooleanObject dhcpFlag) throws IOException {
        write(Ipv4SetupAttributes.USE_DHCP_FLAG, dhcpFlag.getBEREncodedByteArray());
        this.useDHCPFlag = dhcpFlag;
    }

    /**
     * Setter for the DHCPFlag.
     *
     * @param dhcpFlag the DHCP flag as a java boolean
     * @throws java.io.IOException
     */
    public void setDHCPFlag(boolean dhcpFlag) throws IOException {
        writeDHCPFlag(new BooleanObject(dhcpFlag));
    }
}
