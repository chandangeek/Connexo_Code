package com.energyict.dlms.protocolimplv2;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;
import com.energyict.dlms.protocolimplv2.connection.SecureConnection;
import com.energyict.dlms.protocolimplv2.connection.TCPIPConnection;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * V2 version of the DlmsSession object, using a comChannel instead of input/output streams.
 * For usage with DeviceProtocols (not MeterProtocol or SmartMeterProtocol)
 * <p>
 * Copyrights EnergyICT
 * Date: 11/02/11
 * Time: 18:18
 */
public class DlmsSession implements ProtocolLink {

    private final ComChannel comChannel;
    private final DlmsSessionProperties properties;
    protected ApplicationServiceObjectV2 aso;
    protected DLMSMeterConfig dlmsMeterConfig;
    protected SecureConnection dlmsConnection;
    protected CosemObjectFactory cosemObjectFactory;

    public DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        this(comChannel, properties, null, "");
    }

    public DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, String calledSystemTitle) {
        this.comChannel = comChannel;
        this.properties = properties;
        init(null, "", calledSystemTitle);
    }

    public DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, HHUSignOnV2 hhuSignOn, String deviceId) {
        this.comChannel = comChannel;
        this.properties = properties;
        init(hhuSignOn, deviceId, null);
    }

    protected void init(HHUSignOnV2 hhuSignOn, String deviceId, String calledSystemTitle) {
        this.cosemObjectFactory = new CosemObjectFactory(this, getProperties().isBulkRequest());
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(getProperties().getManufacturer());
        this.aso = buildAso(calledSystemTitle);
        this.dlmsConnection = new SecureConnection(this.aso, defineTransportDLMSConnection());
        this.dlmsConnection.setInvokeIdAndPriorityHandler(getProperties().getInvokeIdAndPriorityHandler());
        if (hhuSignOn != null) {
            this.dlmsConnection.setHHUSignOn(hhuSignOn, deviceId);
        }
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    /**
     * Init and set the connection state to connected, without actually opening the association to the meter.
     * This method can be used do create a new DlmsSession on a meter that has already an open association,
     * or with a meter that has a permanent association.
     *
     * @param serverMaxRecPduSize The max pdu size of the server
     * @param conformanceBlock    The negotiated conformance block
     */
    public void assumeConnected(final int serverMaxRecPduSize, final ConformanceBlock conformanceBlock) {
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
            final XdlmsAse xdlmsAse = this.aso.getAssociationControlServiceElement().getXdlmsAse();
            xdlmsAse.setMaxRecPDUServerSize(serverMaxRecPduSize);
            xdlmsAse.setNegotiatedConformance((int) conformanceBlock.getValue());
            xdlmsAse.setNegotiatedQOS((byte) getProperties().getProposedQOS());
            xdlmsAse.setNegotiatedDlmsVersion((byte) getProperties().getProposedDLMSVersion());
            this.aso.setAssociationState(ApplicationServiceObject.ASSOCIATION_CONNECTED);
        }
    }

    public ApplicationServiceObjectV2 getAso() {
        return aso;
    }

    /**
     * Configure the DLMSConnection which is used for dataTransportation
     *
     * @return the newly defined DLMSConnection
     */
    protected DlmsV2Connection defineTransportDLMSConnection() {
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            return new HDLCConnection(comChannel, getProperties());
        } else if (comChannel.getComChannelType() == ComChannelType.SocketComChannel) {
            return new TCPIPConnection(comChannel, getProperties());
        } else {
            throw DeviceConfigurationException.unexpectedComChannel(ComChannelType.SerialComChannel.name() + ", " + ComChannelType.SocketComChannel.name(), comChannel.getClass().getSimpleName());
        }
    }

    /**
     * Connect to the meter by opening the AA to the DLMS device.
     */
    public void connect() {
        getDlmsV2Connection().connectMAC();
        createAssociation();
    }

    /**
     * Disconnect by sending a release request to the device and closing the lower layer.
     */
    public void disconnect() {
        if ((this.aso != null)) {
            this.aso.releaseAssociation();
        }
        if (getDLMSConnection() != null) {
            getDlmsV2Connection().disconnectMAC();
        }
    }

    public void createAssociation() {
        createAssociation(0);
    }

    /**
     * Timeout is an optional parameter that is used for sending the AARQ only.
     * If 0 (default), use the normal timeout of the connection
     */
    public void createAssociation(int timeout) {
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
            this.aso.createAssociation(timeout);
        }
    }

    /**
     * Build a new ApplicationServiceObject
     */
    protected ApplicationServiceObjectV2 buildAso(String calledSystemTitleString) {
        if (calledSystemTitleString == null && getProperties().isNtaSimulationTool()) {
            calledSystemTitleString = getProperties().getSerialNumber();
        }

        return new ApplicationServiceObjectV2(
                buildXDlmsAse(),
                this,
                buildSecurityContext(),
                getContextId(),
                calledSystemTitleString == null ? null : calledSystemTitleString.getBytes(),
                null,
                getCallingAEQualifier());
    }

    /**
     * We fill our (client) signing certificate in the calling-AE-qualifier field, but only
     * if use digital signing (either for data transport security or for HLS7 authentication)
     * in this session, and we don't know the server signing certificate yet.
     * <p>
     * Note that this is the ASN.1 DER encoded version of the X.509 v3 certificate.
     */
    private byte[] getCallingAEQualifier() {
        if (getProperties().isGeneralSigning() || getProperties().getAuthenticationSecurityLevel() == DlmsSecuritySuite1And2Support.AuthenticationAccessLevelIds.ECDSA_AUTHENTICATION.getAccessLevel()) {
            if (getProperties().getSecurityProvider() instanceof GeneralCipheringSecurityProvider) {
                GeneralCipheringSecurityProvider generalCipheringSecurityProvider = (GeneralCipheringSecurityProvider) getProperties().getSecurityProvider();
                if (generalCipheringSecurityProvider.getServerSignatureCertificate() == null) {
                    try {
                        X509Certificate clientSigningCertificate = generalCipheringSecurityProvider.getClientSigningCertificate();
                        if (clientSigningCertificate == null) {
                            throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
                        }

                        return clientSigningCertificate.getEncoded();
                    } catch (CertificateEncodingException e) {
                        throw DeviceConfigurationException.invalidPropertyFormat(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, "x", "Should be a private key and a valid X.509 v3 certificate");
                    }
                }
            } else {
                throw CodingException.protocolImplementationError("General signing is not yet supported in the protocol you are using");
            }
        }
        return null;
    }


    /**
     * Build a new xDLMSAse, using the DlmsSessionProperties
     */
    protected XdlmsAse buildXDlmsAse() {
        return new XdlmsAse(
                (isDedicated()) ? getProperties().getSecurityProvider().getDedicatedKey() : null,
                getProperties().getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().needsResponse(),
                getProperties().getProposedQOS(),
                getProperties().getProposedDLMSVersion(),
                getProperties().getConformanceBlock(),
                getProperties().getMaxRecPDUSize()
        );
    }

    private boolean isDedicated() {
        return getProperties().getCipheringType() == CipheringType.DEDICATED || getProperties().getCipheringType() == CipheringType.GENERAL_DEDICATED;
    }

    /**
     * Define the contextID of the associationServiceObject.
     * Depending on the reference(see {@link com.energyict.dlms.ProtocolLink#LN_REFERENCE} and {@link com.energyict.dlms.ProtocolLink#SN_REFERENCE}, the value can be different.
     *
     * @return the contextId
     */
    protected int getContextId() {
        if (getProperties().getReference().equals(DLMSReference.SN)) {
            if (getProperties().getDataTransportSecurityLevel() == 0) {
                return AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING;
            } else {
                return AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING;
            }
        } else {
            if (getProperties().getDataTransportSecurityLevel() == 0) {
                return AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING;
            } else {
                return AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
            }
        }
    }

    /**
     * Build a new SecurityContext, using the DlmsSessionProperties
     */
    protected SecurityContext buildSecurityContext() {
        return new SecurityContext(
                getProperties().getDataTransportSecurityLevel(),
                getProperties().getAuthenticationSecurityLevel(),
                getProperties().getSecuritySuite(),
                (getProperties().getSystemIdentifier() == null) ? null : getProperties().getSystemIdentifier(),
                getProperties().getSecurityProvider(),
                getProperties().getCipheringType().getType(),
                getProperties().getGeneralCipheringKeyType()
        );
    }

    public DlmsSessionProperties getProperties() {
        return properties;
    }

    public Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }

    public boolean isRequestTimeZone() {
        return getProperties().isRequestTimeZone();
    }

    public int getReference() {
        return getProperties().getReference().getReference();
    }

    public TimeZone getTimeZone() {
        return getProperties().getTimeZone();
    }

    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    public StoredValues getStoredValues() {
        return null;     //Not used
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;    //the instance is a SecureConnection, this implements DLMSConnection.
    }

    /**
     * The V2 connection that does not throw IOExceptions, but instead handles errors itself by throwing the proper ComServer runtime exceptions.
     */
    public DlmsV2Connection getDlmsV2Connection() {
        return dlmsConnection;
    }

    public DLMSMeterConfig getMeterConfig() {
        return dlmsMeterConfig;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}
