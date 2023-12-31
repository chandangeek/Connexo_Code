/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.mbus.check.CheckFrameDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.check.CheckFrameParser;
import com.energyict.mdc.protocol.inbound.mbus.factory.MerlinCollectedDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.MerlinMBusCollectedDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.MerlinMetaDataExtractor;
import com.energyict.mdc.protocol.inbound.mbus.parser.MerlinMBusParser;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
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

    private MerlinMBusParser parser; // delegate parsing to dedicated class for ease of testing
    private InboundContext inboundContext; // all overhead required
    private MerlinCollectedDataFactory factory;
    private MerlinMetaDataExtractor metaDataFactory;
    private CheckFrameParser checkFrameParser;


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
        if (this.factory != null) {
            return factory.getDeviceIdentifier();
        }

        if (this.metaDataFactory != null) {
            return metaDataFactory.getDeviceIdentifier();
        }

        if (this.checkFrameParser != null) {
            return new DeviceIdentifierBySerialNumber(checkFrameParser.getDeviceId());
        }

        return null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        if (this.factory != null) {
            return factory.getCollectedData();
        }
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

    private MerlinMBusParser getParser() {
        if (this.parser == null){
            this.parser = new MerlinMBusParser(getInboundContext());
        }

        return this.parser;
    }

    private InboundContext getInboundContext() {
        if (this.inboundContext == null){
            this.inboundContext = new InboundContext(getLogger(), getContext());
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
        String sourceAddress = getSourceAddress();
        getLogger().info("Received ["+sourceAddress+"]", buffer, readBytes);

        byte[] payload;
        if (readBytes < BUFFER_SIZE){
            payload = new byte[readBytes];
            System.arraycopy(buffer, 0, payload, 0, readBytes);
        } else {
            getLogger().info("WARN: buffer overflow detected, will consider only the first " + BUFFER_SIZE + " bytes");
            payload = new byte[BUFFER_SIZE];
            System.arraycopy(buffer, 0, payload, 0, BUFFER_SIZE);
        }

        doParse(payload);
    }

    private String getSourceAddress() {
        return "n/a";
        // ((com.energyict.mdc.channel.ip.datagrams.DatagramComChannel)comChannel).getRemoteAddress()
        //return getComChannel().getRemoteAddress().orElse("n/a");
    }

    private void doParse(byte[] payload) {
        if (canParseAsCheckFrame(payload)) {
            // simple communication test frame (non-MBus), will use dedicated factory
            this.factory = new CheckFrameDataFactory(checkFrameParser, getInboundContext());
            return;
        }

        // 1. parse only the telegram header to get meta-data
       Telegram encryptedTelegram = getParser().parseHeader(payload);

        // 2. Get Connexo core properties for the meta extracted
        this.metaDataFactory = new MerlinMetaDataExtractor(encryptedTelegram, getInboundContext());

        if (!metaDataFactory.isValid()) {
            return;
        }

        // 3. Decrypt and parse
        Telegram decodedTelegram;
        try {
            decodedTelegram = getParser().parse();
        } catch (Exception ex) {
            getLogger().error("Could not decrypt and parse telegram!", ex);
            return;
        }

        if (decodedTelegram.decryptionError()) {
            getLogger().error("Cannot decrypt telegram! Check the EK!");
            //throw DataEncryptionException.dataEncryptionException();
            return;
        }

        try {
            // 4. Collect the actual data
            this.factory = new MerlinMBusCollectedDataFactory(decodedTelegram, getInboundContext());
        } catch (Exception ex) {
            getLogger().error("Cannot parse and extract data from decrypted telegram", ex);
        }

    }

    private boolean canParseAsCheckFrame(byte[] payload) {
        this.checkFrameParser = new CheckFrameParser(payload);
        if (!checkFrameParser.isCheckFrame()) {
            return false;
        }

        getLogger().info("Check frame: " + checkFrameParser.toString());

        return true;
    }


}
