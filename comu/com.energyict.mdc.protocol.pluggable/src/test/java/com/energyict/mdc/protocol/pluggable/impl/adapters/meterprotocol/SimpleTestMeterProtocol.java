/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Simple class representing a MeterProtocol. Is only used for testing.
 * <p>
 * Date: 15/01/13
 * Time: 10:13
 */
public class SimpleTestMeterProtocol implements MeterProtocol, MessageProtocol, DeviceMessageSupport {

    public SimpleTestMeterProtocol() {
    }

    @Override
    public String getProtocolDescription() {
        return this.getClass().getName();
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        // nothing to set
    }

    @Override
    public void connect() throws IOException {
        // nothing to set
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }

    @Override
    public void disconnect() throws IOException {
        // nothing to set
    }

    @Override
    public String getProtocolVersion() {
        return null;          // nothing to set
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;          // nothing to set
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 0;          // nothing to set
    }

    @Override
    public Date getTime() throws IOException {
        return null;          // nothing to set
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        // nothing to set
    }

    @Override
    public void setTime() throws IOException {
        // nothing to set
    }

    @Override
    public void initializeDevice() throws IOException {
        // nothing to set
    }

    @Override
    public void release() throws IOException {
        // nothing to set
    }

    @Override
    public Serializable getCache() {
        return null;          // nothing to set
    }

    @Override
    public void setCache(Serializable cacheObject) {
        // nothing to set
    }

    @Override
    public String getVersion() {
        return null;          // nothing to set
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing to set
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        // nothing to do
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public List getMessageCategories() {
        return Collections.emptyList();
    }

    @Override
    public String writeMessage(Message msg) {
        return null;          // nothing to write
    }

    @Override
    public String writeTag(MessageTag tag) {
        return null;          // nothing to write
    }

    @Override
    public String writeValue(MessageValue value) {
        return null;          // nothing to write
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return String.valueOf(messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

}