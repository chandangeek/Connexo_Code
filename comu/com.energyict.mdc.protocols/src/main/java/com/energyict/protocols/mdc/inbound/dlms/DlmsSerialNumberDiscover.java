package com.energyict.protocols.mdc.inbound.dlms;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.io.ComChannelInputStreamAdapter;
import com.energyict.mdc.io.ComChannelOutputStreamAdapter;
import com.energyict.protocols.mdc.inbound.dlms.aso.SimpleApplicationServiceObject;
import com.energyict.protocols.mdc.inbound.general.AbstractDiscover;
import com.energyict.protocols.mdc.inbound.general.InboundConnection;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
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

    private static final int DEFAULT_PUBLIC_CLIENT_MAC_ADDRESS = 16;
    private static final int DEFAULT_SERVER_ADDRESS = 1;
    private static final ObisCode DEFAULT_DEVICE_ID_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");

    private static final int DEFAULT_FORCED_DELAY = 0;
    private static final int DEFAULT_ISKRA_WRAPPER = 1;
    private static final int DEFAULT_INVOKE_ID_AND_PRIORITY = 66; // 0x42, 0b01000010 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]

    private final Clock clock;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private DLMSConnection dlmsConnection;
    private SimpleApplicationServiceObject aso;

    @Inject
    public DlmsSerialNumberDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService, Clock clock, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService);
        this.clock = clock;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
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
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }

        return DiscoverResultType.IDENTIFIER;
    }

    private void setInboundConnection() {
        ComChannel comChannel = this.getComChannel();
        this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.clock, this.issueService, this.getReadingTypeUtilService(), this.collectedDataFactory, this.meteringService, this.getThesaurus(), getIdentificationService()));
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
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.getPropertySpecService().basicPropertySpec(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, false, new BigDecimalFactory()));
        propertySpecs.add(this.getPropertySpecService().basicPropertySpec(DlmsProtocolProperties.SERVER_MAC_ADDRESS, false, new BigDecimalFactory()));
        propertySpecs.add(this.getPropertySpecService().basicPropertySpec(DEVICE_ID_OBISCODE_KEY, false, new ObisCodeValueFactory()));
        return propertySpecs;
    }

    public int getClientMacAddressProperty() {
        return getTypedProperties().getIntegerProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, new BigDecimal(DEFAULT_PUBLIC_CLIENT_MAC_ADDRESS)).intValue();
    }

    public int getServerMacAddressProperty() {
        return getTypedProperties().getIntegerProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS, new BigDecimal(DEFAULT_SERVER_ADDRESS)).intValue();
    }

    public ObisCode getDeviceIdObisCodeProperty() {
        return (ObisCode) getTypedProperties().getProperty(DEVICE_ID_OBISCODE_KEY, DEFAULT_DEVICE_ID_OBISCODE);
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-05 15:41:18 +0200 (vr, 05 apr 2013) $";
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }
}
