package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.mbus.parser.MerlinMbusParser;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Merlin implements BinaryInboundDeviceProtocol {

    private static final String VERSION = "1.0";
    public static final int BUFFER_SIZE = 1024;

    protected MerlinLogger logger = new MerlinLogger(Logger.getLogger(this.getClass().getName()));

    protected InboundDiscoveryContext context;
    protected ComChannel comChannel;
    protected CollectedLogBook collectedLogBook;

    private List<CollectedDeviceInfo> collectedDeviceInfoList;
    private List<CollectedData> collectedDataList;
    private MerlinMbusParser parser; // delegate parsing to dedicated class for ease of testing
    private InboundContext inboundContext; // all overhead required


    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }



    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        // Nothing to do here, nothing expected back
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return null;
    }



    @Override
    public List<CollectedData> getCollectedData() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public DiscoverResultType doDiscovery() {

        readAndParseInboundFrame();



        return DiscoverResultType.DATA;
    }

    private MerlinMbusParser getParser() {
        if (this.parser == null){
            this.parser = new MerlinMbusParser(getInboundContext());
        }

        return this.parser;
    }

    private InboundContext getInboundContext() {
        if (this.inboundContext != null){
            this.inboundContext = new InboundContext(getLogger());
        }

        return this.inboundContext;
    }

    private MerlinLogger getLogger() {
        return this.logger;
    }


    private void readAndParseInboundFrame() {
        byte[] buffer = new byte[BUFFER_SIZE];
        getComChannel().startReading();
        final int readBytes = getComChannel().read(buffer);
        getLogger().log("Received", buffer, readBytes);

        if (readBytes < BUFFER_SIZE){
            byte[] payload = new byte[readBytes];
            System.arraycopy(buffer, 0, payload, 0, readBytes);
            getParser().parse(payload);
        } else {
            getLogger().log("WARN: buffer overflow!");
            //TODO: keep reading
        }


    }



}
