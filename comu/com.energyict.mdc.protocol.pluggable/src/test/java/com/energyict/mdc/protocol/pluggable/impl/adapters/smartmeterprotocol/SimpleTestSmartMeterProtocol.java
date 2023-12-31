/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageResultExecutor;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class SimpleTestSmartMeterProtocol implements SmartMeterProtocol, MessageProtocol {

    private List<MessageResultExecutor> queryMessageResultExecutors = new ArrayList<>();
    private MessageResultExecutor applyMessageResultExecutor = () -> {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    };
    int queryMessageCounter = 0;

    public SimpleTestSmartMeterProtocol() {
        super();
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        // nothing to do
    }

    @Override
    public void connect() throws IOException {
        // nothing to do
    }

    @Override
    public void disconnect() throws IOException {
        // nothing to do
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public Date getTime() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        // nothing to do
    }

    @Override
    public void initializeDevice() throws IOException {
        // nothing to do
    }

    @Override
    public void release() throws IOException {
        // nothing to do
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public void setCache(Serializable cacheObject) {
        // nothing to do
    }

    @Override
    public Serializable getCache() {
        return null;  // nothing to do
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public String getVersion() {
        return null;  // nothing to do
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing to do
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    public void setApplyMessageResultExecutor(MessageResultExecutor messageResultExecutor) {
        applyMessageResultExecutor = messageResultExecutor;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        applyMessageResultExecutor.performMessageResult();
    }

    public void setQueryMessageResultExecutors(List<MessageResultExecutor> messageResultExecutors) {
        this.queryMessageResultExecutors = messageResultExecutors;
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getNextQueryMessageExecutor().performMessageResult();
    }

    private MessageResultExecutor getNextQueryMessageExecutor() throws IOException {
        if (queryMessageResultExecutors.isEmpty()) {
            throw new IOException("You didn't set the resultExecutors");
        }
        if (queryMessageCounter == queryMessageResultExecutors.size()) {
            queryMessageCounter = 0;
        }
        return queryMessageResultExecutors.get(queryMessageCounter++);
    }

    @Override
    public List getMessageCategories() {
        return null;  // nothing to do
    }

    @Override
    public String writeMessage(Message msg) {
        return null;  // nothing to do
    }

    @Override
    public String writeTag(MessageTag tag) {
        return null;  // nothing to do
    }

    @Override
    public String writeValue(MessageValue value) {
        return null;  // nothing to do
    }

    public String getProtocolDescription() {
        return "";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }
}