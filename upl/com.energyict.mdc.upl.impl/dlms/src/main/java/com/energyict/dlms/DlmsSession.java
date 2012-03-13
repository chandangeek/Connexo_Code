package com.energyict.dlms;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 11/02/11
 * Time: 18:18
 */
public class DlmsSession implements ProtocolLink {

    private DlmsSessionProperties properties;
    private ApplicationServiceObject aso;
    private DLMSMeterConfig dlmsMeterConfig;
    private Logger logger;
    private TimeZone timeZone;
    private DLMSConnection dlmsConnection;
    private InputStream in;
    private OutputStream out;
    private CosemObjectFactory cosemObjectFactory;

    public DlmsSession(InputStream in, OutputStream out, Logger logger, DlmsSessionProperties properties, TimeZone timeZone) {
        this.in = in;
        this.out = out;
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
    }

    public void init() throws IOException {
        this.cosemObjectFactory = new CosemObjectFactory(this, getProperties().isBulkRequest());
        this.aso = buildAso();
        if (dlmsConnection == null) {
            this.dlmsConnection = new SecureConnection(this.aso, defineTransportDLMSConnection());
        }
        this.dlmsConnection.setInvokeIdAndPriority(getProperties().getInvokeIdAndPriority());
        this.dlmsConnection.setIskraWrapper(getProperties().getIskraWrapper());
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(getProperties().getManufacturer());
    }

    public void disconnect() {
        try {
            if ((this.aso != null) && (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED)) {
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
        } catch (DLMSConnectionException e) {
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    public void connect() throws IOException {
        init();
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();
            }
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e, "Exception occurred while connection DLMSStream");
        }
    }

    /**
     * Configure the DLMSConnection which is used for dataTransportation
     *
     * @return the newly defined DLMSConnection
     * @throws DLMSConnectionException if addressingMode is unknown
     * @throws IOException             if connectionMode is unknown
     */
    private DLMSConnection defineTransportDLMSConnection() throws IOException {
        DLMSConnection transportConnection;
        switch (getProperties().getConnectionMode()) {
            case HDLC:
                try {
                    transportConnection = new HDLC2Connection(
                            in, out,
                            getProperties().getTimeout(),
                            getProperties().getForcedDelay(),
                            getProperties().getRetries(),
                            getProperties().getClientMacAddress(),
                            getProperties().getLowerHDLCAddress(),
                            getProperties().getUpperHDLCAddress(),
                            getProperties().getAddressingMode(),
                            getProperties().getInformationFieldSize(),
                            5
                    );
                } catch (DLMSConnectionException e) {
                    throw new NestedIOException(e);
                }
                break;
            case TCPIP:
                transportConnection = new TCPIPConnection(
                        in, out,
                        getProperties().getTimeout(),
                        getProperties().getForcedDelay(),
                        getProperties().getRetries(),
                        getProperties().getClientMacAddress(),
                        getProperties().getDestinationWPortNumber()
                );
                break;
            case COSEM_APDU:
                transportConnection = new CosemPDUConnection(
                        in, out,
                        getProperties().getTimeout(),
                        getProperties().getForcedDelay(),
                        getProperties().getRetries(),
                        getProperties().getClientMacAddress(),
                        getProperties().getDestinationWPortNumber()
                );
                break;
            case LLC:
                transportConnection = new LLCConnection(
                        in, out,
                        getProperties().getTimeout(),
                        getProperties().getForcedDelay(),
                        getProperties().getRetries(),
                        getProperties().getClientMacAddress(),
                        getProperties().getDestinationWPortNumber()
                );
                break;
            default:
                throw new IOException("Unknown connectionMode: " + getProperties().getConnectionMode() + " - Only 0(HDLC), 1(TCP), 2(COSEM_APDU) and 3(LLC) are allowed");
        }

        return transportConnection;
    }

    /**
     * Build a new ApplicationServiceObject, using the DLMSProperties
     *
     * @return
     */
    private ApplicationServiceObject buildAso() throws IOException {
        SecurityContext sc = buildSecurityContext();
        if (getProperties().isNtaSimulationTool()) {
            return new ApplicationServiceObject(buildXDlmsAse(), this, sc, getContextId(), getProperties().getSerialNumber().getBytes(), null);
        } else {
            return new ApplicationServiceObject(buildXDlmsAse(), this, sc, getContextId());
        }
    }

    /**
     * Build a new xDLMSAse, using the DLMSProperties
     *
     * @return
     */
    private XdlmsAse buildXDlmsAse() throws IOException {
        return new XdlmsAse(
                (getProperties().getCipheringType() == CipheringType.DEDICATED) ? getProperties().getSecurityProvider().getDedicatedKey() : null, true,
                getProperties().getProposedQOS(),
                getProperties().getProposedDLMSVersion(),
                getProperties().getConformanceBlock(),
                getProperties().getMaxRecPDUSize()
        );
    }

    /**
     * Define the contextID of the associationServiceObject.
     * Depending on the reference(see {@link ProtocolLink#LN_REFERENCE} and {@link ProtocolLink#SN_REFERENCE}, the value can be different.
     *
     * @return the contextId
     */
    private int getContextId() {
        if (getProperties().isSNReference()) {
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
     * Build a new SecurityContext, using the DLMSProperties
     *
     * @return
     */
    private SecurityContext buildSecurityContext() {
        return new SecurityContext(
                getProperties().getDataTransportSecurityLevel(),
                getProperties().getAuthenticationSecurityLevel(),
                0, // TODO: check what this means
                (getProperties().getSystemIdentifier() == null) ? null : getProperties().getSystemIdentifier().getBytes(),
                getProperties().getSecurityProvider(),
                getProperties().getCipheringType().getType()
        );
    }


    public DlmsSessionProperties getProperties() {
        return properties;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isRequestTimeZone() {
        return getProperties().isRequestTimeZone();
    }

    public int getReference() {
        return getProperties().getReference().getReference();
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    public StoredValues getStoredValues() {
        return null;
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    public DLMSMeterConfig getMeterConfig() {
        return dlmsMeterConfig;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    /**
     * Setter for the DLMSConnection
     *
     * @param dlmsConnection the new DLMSConnection to set
     */
    public void setDlmsConnection(DLMSConnection dlmsConnection) {
        this.dlmsConnection = dlmsConnection;
    }

    /**
     * Update the used TimeZone
     *
     * @param timeZone the new TimeZone to use
     */
    public void updateTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
