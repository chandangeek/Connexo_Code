/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Mocked SmartMeterProtocol for PluggableClassTestUsages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (12:12)
 */
public class MockSmartMeterProtocol implements SmartMeterProtocol, MessageProtocol {

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) {
        return null;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public String getFirmwareVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public String getMeterSerialNumber() {
        return null;
    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public void setTime(Date newMeterTime) {

    }

    @Override
    public void initializeDevice() {

    }

    @Override
    public void release() {

    }

    public String getProtocolDescription() {
        return null;
    }

    @Override
    public Serializable getCache() {
        return null;
    }

    @Override
    public void setCache(Serializable cacheObject) {
        // nothing to do
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) {
        return null;
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public String getVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public void addProperties(TypedProperties properties) {

    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return null;
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return Collections.emptyList();
    }

    @Override
    public String writeMessage(Message msg) {
        return "";
    }

    @Override
    public String writeTag(MessageTag tag) {
        return "";
    }

    @Override
    public String writeValue(MessageValue value) {
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