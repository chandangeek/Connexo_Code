/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Dummy MeterProtocol for PluggableClassTestUsages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (10:59)
 */
public class MockMeterProtocol implements MeterProtocol, MessageProtocol {

    private final PropertySpecService propertySpecService;
    private final String javaClassName;

    public MockMeterProtocol(PropertySpecService propertySpecService) {
        this(propertySpecService, MockMeterProtocol.class.getName());
    }

    public MockMeterProtocol(PropertySpecService propertySpecService, String javaClassName) {
        super();
        this.propertySpecService = propertySpecService;
        this.javaClassName = javaClassName;
    }

    @Override
    public String getProtocolDescription() {
        return javaClassName;
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {

    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public String getProtocolVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 0;
    }

    @Override
    public Date getTime() throws IOException {
        return null;
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {

    }

    @Override
    public void setTime() throws IOException {

    }

    @Override
    public void initializeDevice() throws IOException {

    }

    @Override
    public void release() throws IOException {

    }

    @Override
    public Serializable getCache() {
        return null;
    }

    @Override
    public void setCache(Serializable cacheObject) {

    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void addProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> required = new ArrayList<>(1);
        String propertyName = "RequiredProperty";
        required.add(
                this.propertySpecService
                        .stringSpec()
                        .named(propertyName, propertyName)
                        .describedAs(propertyName)
                        .markRequired()
                        .finish());
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<>(1);
        String propertyName = "OptionalProperty";
        optional.add(
                this.propertySpecService
                        .stringSpec()
                        .named(propertyName, propertyName)
                        .describedAs(propertyName)
                        .finish());
        return optional;
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
}