package com.energyict.dlms.protocolimplv2;

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
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * V2 version of the DlmsSession object, using a comChannel instead of input/output streams.
 * For usage with DeviceProtocols (not MeterProtocol or SmartMeterProtocol)
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/02/11
 * Time: 18:18
 */
public class DlmsSession implements ProtocolLink {

    private final ComChannel comChannel;
    private final DlmsSessionProperties properties;
    private final ApplicationServiceObjectV2 aso;
    private final DLMSMeterConfig dlmsMeterConfig;
    private final SecureConnection dlmsConnection;
    private final CosemObjectFactory cosemObjectFactory;

    public DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        this(comChannel, properties, null, "");
    }

    public DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, HHUSignOnV2 hhuSignOn, String deviceId) {
        this.comChannel = comChannel;
        this.properties = properties;
        this.cosemObjectFactory = new CosemObjectFactory(this, getProperties().isBulkRequest());
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(getProperties().getManufacturer());
        this.aso = buildAso();
        this.dlmsConnection = new SecureConnection(this.aso, defineTransportDLMSConnection());
        this.dlmsConnection.setInvokeIdAndPriorityHandler(getProperties().getInvokeIdAndPriorityHandler());
        if (hhuSignOn != null) {
            this.dlmsConnection.setHHUSignOn(hhuSignOn, deviceId);
        }
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

    public ApplicationServiceObject getAso() {
        return aso;
    }

    /**
     * Configure the DLMSConnection which is used for dataTransportation
     *
     * @return the newly defined DLMSConnection
     */
    protected DlmsV2Connection defineTransportDLMSConnection() {
        if (ComChannelType.SERIAL_COM_CHANNEL.is(comChannel) || ComChannelType.OPTICAL_COM_CHANNEL.is(comChannel)) {
            return new HDLCConnection(comChannel, getProperties());
        } else if (ComChannelType.SOCKET_COM_CHANNEL.is(comChannel)) {
            return new TCPIPConnection(comChannel, getProperties());
        } else {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_COMCHANNEL, ComChannelType.SERIAL_COM_CHANNEL.name() + ", " + ComChannelType.SOCKET_COM_CHANNEL.name(), comChannel.getClass().getSimpleName());
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
     * Build a new ApplicationServiceObject, using the DlmsSessionProperties
     */
    protected ApplicationServiceObjectV2 buildAso() {
        if (getProperties().isNtaSimulationTool()) {
            return new ApplicationServiceObjectV2(buildXDlmsAse(), this, buildSecurityContext(), getContextId(), getProperties().getSerialNumber().getBytes(), null);
        } else {
            return new ApplicationServiceObjectV2(buildXDlmsAse(), this, buildSecurityContext(), getContextId());
        }
    }

    /**
     * Build a new xDLMSAse, using the DlmsSessionProperties
     */
    protected XdlmsAse buildXDlmsAse() {
        return new XdlmsAse(
                (getProperties().getCipheringType() == CipheringType.DEDICATED) ? getProperties().getSecurityProvider().getDedicatedKey() : null,
                getProperties().getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().needsResponse(),
                getProperties().getProposedQOS(),
                getProperties().getProposedDLMSVersion(),
                getProperties().getConformanceBlock(),
                getProperties().getMaxRecPDUSize()
        );
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
                0,
                (getProperties().getSystemIdentifier() == null) ? null : getProperties().getSystemIdentifier(),
                getProperties().getSecurityProvider(),
                getProperties().getCipheringType().getType()
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
