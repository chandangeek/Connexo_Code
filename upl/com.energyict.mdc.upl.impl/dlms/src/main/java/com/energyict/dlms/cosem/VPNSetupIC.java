package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.VPNSetupAttributes;
import com.energyict.dlms.cosem.methods.VPNSetupMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * VPN setup IC
 * class id = 20029, version = 0, logical name = 0-160:96.128.0.255 (00A0608000FF)
 * The VPN setup IC is a manufacturer-specific COSEM IC which can be used for configuring and quering the VPN link status. Changes made to the attributes of this IC take effect after
 * closing the DLMS association, or after invoking the refresh_vpn_config method.
 */
public class VPNSetupIC extends AbstractCosemObject {

    private static final ObisCode OBIS_CODE_VPN_SETUP = ObisCode.fromString("0.160.96.128.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public VPNSetupIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.VPN_SETUP.getClassId();
    }

    @Override
    public ObisCode getObisCode() {
        return getDefaultObisCode();
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE_VPN_SETUP;
    }

    /**
     *
     * @return VPN enabled/disabled state
     * @throws java.io.IOException
     */
    public BooleanObject vpnEnabled() throws IOException {
        return readDataType(VPNSetupAttributes.VPN_ENABLED, BooleanObject.class);
    }

    /**
     * @return the VPN type:
     * vpn_type_enum ::= enum:
     * (0) IPSec/IKEv2,
     * (1) PPTP,
     * (2) OpenVPN,
     * (3) OpenVPN-NL
     * @throws java.io.IOException
     */
    public TypeEnum vpnType() throws IOException {
        return readDataType(VPNSetupAttributes.VPN_TYPE, TypeEnum.class);
    }

    /**
     * @return Remote VPN endpoint; Can be either ip address or hostname.
     * @throws java.io.IOException
     */
    public OctetString gatewayAddress() throws IOException {
        return readDataType(VPNSetupAttributes.GATEWAY_ADDRESS, OctetString.class);
    }

    /**
     * @return Authentication type:
                authentication_type ::= enum:
                (0) IKEv2 with PSK,
                (1) IKEv2 with certificates,
                (2) IKEv2 using EAP-TLS
     * @throws java.io.IOException
     */
    public TypeEnum authenticationType() throws IOException {
        return readDataType(VPNSetupAttributes.AUTHENTICATION_TYPE, TypeEnum.class);
    }

    /**
     *
     * @return Local identifier used during IKE SA
     * @throws java.io.IOException
     */
    public OctetString localIdentifier() throws IOException {
        return readDataType(VPNSetupAttributes.LOCAL_IDENTIFIER, OctetString.class);
    }

    /**
     *
     * @return Remote identifier expected during IKE SA
     * @throws java.io.IOException
     */
    public OctetString remoteIdentifier() throws IOException {
        return readDataType(VPNSetupAttributes.REMOTE_IDENTIFIER, OctetString.class);
    }

    /**
     *
     * @return Local certificate used during IKE. Contains X.509 certificate in DER format.
     * @throws java.io.IOException
     */
    public OctetString localCertificate() throws IOException {
        return readDataType(VPNSetupAttributes.LOCAL_CERTIFICATE, OctetString.class);
    }

    /**
     *
     * @return Remote certificate expected during IKE. Contains X.509 certificate in DER format.
     * @throws java.io.IOException
     */
    public OctetString remoteCertificate() throws IOException {
        return readDataType(VPNSetupAttributes.REMOTE_CERTIFICATE, OctetString.class);
    }

    /**
     *
     * @return Shared secret used during IKE.
     * @throws java.io.IOException
     */
    public OctetString sharedSecret() throws IOException {
        return readDataType(VPNSetupAttributes.SHARED_SECRET, OctetString.class);
    }

    /**
     *
     * @return Virtual IPs enabled/disabled state.
     * @throws java.io.IOException
     */
    public BooleanObject requestVirtualIP() throws IOException {
        return readDataType(VPNSetupAttributes.REQUEST_VIRTUAL_IP, BooleanObject.class);
    }

    /**
     *
     * @return IPCompression enabled/disabled state.
     * @throws java.io.IOException
     */
    public BooleanObject compressionEnabled() throws IOException {
        return readDataType(VPNSetupAttributes.COMPRESSION_ENABLED, BooleanObject.class);
    }

    /**
     *
     * @return  status info on the current VPN link:
            vpn_status ::= structure
            {
            local_subnet: octet-string,
            remote_subnet: octet-string,
            status: octet-string
            }
     * @throws java.io.IOException
     */
    public Structure vpnStatus() throws IOException {
        return readDataType(VPNSetupAttributes.VPN_STATUS, Structure.class);
    }

    /**
     * Triggers an explicit refresh of the VPN configuration using the attributes set previously.
     * request_data ::= <ignored>
     * response_data ::= integer(0)
     * @throws java.io.IOException
     */
    public final void refreshVPNConfig() throws IOException {
        this.methodInvoke(VPNSetupMethods.REFRESH_VPN_CONFIG, new Integer8(0));
    }

}
