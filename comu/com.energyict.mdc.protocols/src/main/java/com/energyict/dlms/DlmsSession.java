/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
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

public class DlmsSession implements ProtocolLink {

    private DlmsSessionProperties properties;
    protected final ApplicationServiceObject aso;
    protected DLMSMeterConfig dlmsMeterConfig;
    private Logger logger;
    private TimeZone timeZone;
    protected DLMSConnection dlmsConnection;
    private InputStream in;
    private OutputStream out;
    protected CosemObjectFactory cosemObjectFactory;
    protected HHUSignOn hhuSignOn = null;

    /**
     * @deprecated it is advised not ot use the input- and outputStream directly, use the ComChannel instead
     * @param in
     * @param out
     * @param logger
     * @param properties
     * @param timeZone
     * @deprecated it is advised not ot use the input- and outputStream directly, use the ComChannel instead
     */
    public DlmsSession(InputStream in, OutputStream out, Logger logger, DlmsSessionProperties properties, TimeZone timeZone) {
        this.in = in;
        this.out = out;
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
        aso = buildAso();
    }

    public ApplicationServiceObject getAso() {
        return aso;
    }

    public void setHhuSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn = hhuSignOn;
    }

    public void init() throws IOException {
        this.cosemObjectFactory = new CosemObjectFactory(this, getProperties().isBulkRequest());
        if (dlmsConnection == null) {
            this.dlmsConnection = new SecureConnection(this.aso, defineTransportDLMSConnection());
            if (hhuSignOn != null) {
                this.dlmsConnection.setHHUSignOn(this.hhuSignOn, "");
            }
        }
        this.dlmsConnection.setInvokeIdAndPriorityHandler(getProperties().getInvokeIdAndPriorityHandler());
        this.dlmsConnection.setIskraWrapper(getProperties().getIskraWrapper());
        this.dlmsMeterConfig = DLMSMeterConfig.getInstance(getProperties().getManufacturer());
    }


    /**
     * Make sure no bytes have been received for a period of X seconds (X = timeout property)
     */
    public void flushInputStream() throws IOException {
        long delay = getProperties().getTimeout();
        long timeout = System.currentTimeMillis() + delay;
        while (true) {
            int available = in.available();
            if (available > 0) {
                in.read(new byte[available]);
                timeout = System.currentTimeMillis() + delay;   //Update timeout moment when receiving bytes
            } else {
                delay();
            }
            if ((System.currentTimeMillis() - timeout) > 0) {
                break;
            }
        }
    }

    private void delay() throws NestedIOException {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        }
    }


    public void disconnect() {
        this.disconnect(true);
    }

    public void disconnect(boolean release) {
        try {
            if (release && (this.aso != null)) {
                logger.fine("Releasing the application association");
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException | DLMSConnectionException e) {
            getLogger().log(Level.FINEST, "Disconnect failed, " + e.getMessage());
        }
    }

    /**
     * Init and set the connection state to connected, without actually opening the association to the meter.
     * This method can be used do create a new DlmsSession on a meter that has already an open association,
     * or with a meter that has a permanent association.
     *
     * @param serverMaxRecPduSize The max pdu size of the server
     * @param conformanceBlock    The negotiated conformance block
     * @throws IOException
     */
    public void assumeConnected(final int serverMaxRecPduSize, final ConformanceBlock conformanceBlock) throws IOException {
        init();
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
            final XdlmsAse xdlmsAse = this.aso.getAssociationControlServiceElement().getXdlmsAse();
            xdlmsAse.setMaxRecPDUServerSize(serverMaxRecPduSize);
            xdlmsAse.setNegotiatedConformance((int) conformanceBlock.getValue());
            xdlmsAse.setNegotiatedQOS((byte) getProperties().getProposedQOS());
            xdlmsAse.setNegotiatedDlmsVersion((byte) getProperties().getProposedDLMSVersion());
            this.aso.setAssociationState(ApplicationServiceObject.ASSOCIATION_CONNECTED);
        }
    }

    /**
     * Connect to the meter by opening the AA to the dlms device.
     *
     * @throws IOException If the connect failed
     */
    public void connect() throws IOException {
        init();
        try {
            getDLMSConnection().connectMAC();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e, "Exception occurred while connection DLMSStream");
        }
        createAssociation();
    }

    public void createAssociation() throws IOException {
        createAssociation(0);
    }

    /**
     * Timeout is an optional parameter that is used for sending the AARQ only.
     * If 0 (default), use the normal timeout of the connection
     */
    public void createAssociation(int timeout) throws IOException {
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
            try {
                logger.fine("Setting up a new application association to the device");
                this.aso.createAssociation(timeout);
                logger.fine("Application association was created successfully");
            } catch (IOException e) {
                logger.fine("Application association failed, " + e.getMessage());
                throw e;
            } catch (DLMSConnectionException e) {
                logger.fine("Application association failed, " + e.getMessage());
                IOException exception = new IOException(e.getMessage());
                exception.initCause(e);
                throw exception;
            }
        } else {
            logger.fine("Application association was already open, continuing...");
        }
    }

    /**
     * Configure the DLMSConnection which is used for dataTransportation
     *
     * @return the newly defined DLMSConnection
     * @throws DLMSConnectionException if addressingMode is unknown
     * @throws IOException             if connectionMode is unknown
     */
    protected DLMSConnection defineTransportDLMSConnection() throws IOException {
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
                        getProperties().getDestinationWPortNumber(),
                        getLogger()
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
            case IF2:
                transportConnection = new IF2Connection(
                        in, out,
                        getProperties().getTimeout(),
                        getProperties().getRetries(),
                        getProperties().getForcedDelay(),
                        getProperties().getDeviceBufferSize(),
                        getProperties().getClientMacAddress(),
                        getProperties().getDestinationWPortNumber(),
                        getProperties().getLowerHDLCAddress(),
                        getLogger()
                );
                break;
            case HDLC_CONSERETH:
                try {
                    transportConnection = new ConserethHDLC2Connection(
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
            default:
                throw new ProtocolException("Unknown connectionMode: " + getProperties().getConnectionMode() + " - Only 0(HDLC), 1(TCP), 2(COSEM_APDU) and 3(LLC) are allowed");
        }

        return transportConnection;
    }

    /**
     * Build a new ApplicationServiceObject, using the DLMSProperties
     *
     * @return
     */
    protected ApplicationServiceObject buildAso() {
        if (getProperties().isNtaSimulationTool()) {
            return new ApplicationServiceObject(buildXDlmsAse(), this, buildSecurityContext(), getContextId(), getProperties().getSerialNumber().getBytes(), null, null);
        } else {
            return new ApplicationServiceObject(buildXDlmsAse(), this, buildSecurityContext(), getContextId());
        }
    }

    /**
     * Build a new xDLMSAse, using the DLMSProperties
     *
     * @return
     */
    protected XdlmsAse buildXDlmsAse()  {
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
     * Depending on the reference(see {@link ProtocolLink#LN_REFERENCE} and {@link ProtocolLink#SN_REFERENCE}, the value can be different.
     *
     * @return the contextId
     */
    protected int getContextId() {
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
    protected SecurityContext buildSecurityContext() {
        return new SecurityContext(
                getProperties().getDataTransportSecurityLevel(),
                getProperties().getAuthenticationSecurityLevel(),
                0, // TODO: check what this means
                (getProperties().getSystemIdentifier() == null) ? null : getProperties().getSystemIdentifier(),
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
