package com.energyict.mdc.protocol.inbound.dlms;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.dlms.aso.ObisCodeWithDefaultValuePropertySpec;
import com.energyict.mdc.protocol.inbound.dlms.aso.SimpleApplicationServiceObject;
import com.energyict.mdc.protocol.inbound.general.AbstractDiscover;
import com.energyict.mdc.protocol.inbound.general.InboundConnection;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Inbound device discovery created for the DLMS protocol
 * In this case, a meter opens an inbound connection to the comserver but it doesn't send any frames.
 * We should send an unencrypted request for identification (using the public client) to know which RTU and schedule has to be executed.
 * Extra requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 *
 * @author sva
 * @since 26/10/12 (11:40)
 */
public class DlmsSerialNumberDiscover extends AbstractDiscover {

    private static final String DEVICE_ID_OBISCODE_KEY = "DeviceIdObisCode";

    private static final BigDecimal DEFAULT_PUBLIC_CLIENT_MAC_ADDRESS = new BigDecimal(16);
    private static final BigDecimal DEFAULT_SERVER_ADDRESS = new BigDecimal(1);
    private static final ObisCode DEFAULT_DEVICE_ID_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");

    private static final int DEFAULT_FORCED_DELAY = 0;
    private static final int DEFAULT_ISKRA_WRAPPER = 1;
    private static final int DEFAULT_INVOKE_ID_AND_PRIORITY = 66; // 0x42, 0b01000010 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]

    private DLMSConnection dlmsConnection;
    private SimpleApplicationServiceObject aso;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public DlmsSerialNumberDiscover(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService);
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public InboundDeviceProtocol.DiscoverResultType doDiscovery() {
        try {
            setInboundConnection();
            init();
            connect();
            try {
                setSerialNumber(getMeterSerialNumberWithRetries());
            } finally {
                disconnect();
            }
        } catch (IOException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }

        return DiscoverResultType.IDENTIFIER;
    }

    private void setInboundConnection() {
        ComChannel comChannel = this.getComChannel();
        this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.collectedDataFactory, this.issueFactory));
    }

    public void init() throws IOException {
        this.dlmsConnection = new EnhancedTCPIPConnection(
                new ComChannelInputStreamAdapter(getComChannel()),
                new ComChannelOutputStreamAdapter(getComChannel()),
                getTimeOutProperty(),
                DEFAULT_FORCED_DELAY,
                getRetriesProperty(),
                getClientMacAddressProperty(),
                getServerMacAddressProperty(),
                Logger.getAnonymousLogger()
        );
        this.aso = buildAso();
        this.dlmsConnection.setInvokeIdAndPriorityHandler(getInvokeIdAndPriorityHandler());

        this.dlmsConnection.setIskraWrapper(DEFAULT_ISKRA_WRAPPER);
    }

    private InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriorityBytes = (byte) (DEFAULT_INVOKE_ID_AND_PRIORITY & 0x0FF);
        return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriorityBytes);
    }

    /**
     * Build a new ApplicationServiceObject
     *
     * @return
     */
    private SimpleApplicationServiceObject buildAso() {
        return new SimpleApplicationServiceObject(dlmsConnection);
    }

    private void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            this.aso.createAssociation();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e, "Exception occurred while connecting to the public client");
        }
    }

    private String getMeterSerialNumberWithRetries() throws IOException {
        int currentTry = 0;

        while (currentTry < (getRetriesProperty() + 1)) {
            try {
                byte[] request = buildGetRequest(1, getDeviceIdObisCodeProperty().getLN(), (byte) 2);
                byte[] response = dlmsConnection.sendRequest(request);
                AbstractDataType abstractDataType = ((EnhancedTCPIPConnection) dlmsConnection).parseResponse(response);
                if (abstractDataType.isOctetString()) {
                    return ((OctetString) abstractDataType).stringValue();
                }
            } catch (IOException e) {
                if (currentTry == getRetriesProperty()) {
                    throw new NestedIOException(e, "Failed to read the serial number.");
                }
            }
            currentTry++;
        }
        throw new IOException("Failed to read the serial number.");
    }

    private byte[] buildGetRequest(int classId, byte[] LN, byte bAttr) {
        byte[] readRequestArray = new byte[DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE];

        readRequestArray[0] = (byte) 0xE6;  // Destination_LSAP
        readRequestArray[1] = (byte) 0xE6;  // Source_LSAP
        readRequestArray[2] = 0x00;         // LLC_Quality
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_GETREQUEST;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] = DLMSCOSEMGlobals.COSEM_GETREQUEST_NORMAL; // get request normal
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] = getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriority(); //invoke id and priority
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

        for (int i = 0; i < 6; i++) {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_LN + i] = LN[i];
        }

        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ATTR] = bAttr;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0; // Selective access descriptor NOT present
        return readRequestArray;
    }

    public void disconnect() {
        try {
            if ((this.aso != null)) {
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException e) {
        } catch (DLMSConnectionException e) {
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        return; // No need to provide a response - the public client association is already closed.
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        List<com.energyict.mdc.upl.properties.PropertySpec> propertySpecs = super.getPropertySpecs();
        PropertySpecService propertySpecService = this.getPropertySpecService();
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, false, propertySpecService::bigDecimalSpec)
                        .setDefaultValue(DEFAULT_PUBLIC_CLIENT_MAC_ADDRESS)
                        .finish());
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(DlmsProtocolProperties.SERVER_MAC_ADDRESS, false, propertySpecService::bigDecimalSpec)
                        .setDefaultValue(DEFAULT_SERVER_ADDRESS)
                        .finish());
        propertySpecs.add(new ObisCodeWithDefaultValuePropertySpec(DEVICE_ID_OBISCODE_KEY, DEFAULT_DEVICE_ID_OBISCODE));
        return propertySpecs;
    }

    public int getClientMacAddressProperty() {
        return getTypedProperties().getIntegerProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, DEFAULT_PUBLIC_CLIENT_MAC_ADDRESS).intValue();
    }

    public int getServerMacAddressProperty() {
        return getTypedProperties().getIntegerProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS, DEFAULT_SERVER_ADDRESS).intValue();
    }

    public ObisCode getDeviceIdObisCodeProperty() {
        return (ObisCode) getTypedProperties().getProperty(DEVICE_ID_OBISCODE_KEY, DEFAULT_DEVICE_ID_OBISCODE);
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }
}