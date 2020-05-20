/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.pluggable.PluggableClassType;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLConnectionTypeAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnectionTypePluggableClassTranslationKeys;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ConnectionTypePluggableClass} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (10:43)
 */
public final class ConnectionTypePluggableClassImpl extends PluggableClassWrapper<ConnectionType> implements ConnectionTypePluggableClass {

    private final CustomPropertySetService customPropertySetService;
    private final ProtocolPluggableService protocolPluggableService;
    private final PropertySpecService propertySpecService;

    @Inject
    public ConnectionTypePluggableClassImpl(EventService eventService, Thesaurus thesaurus, CustomPropertySetService customPropertySetService, ProtocolPluggableService protocolPluggableService, PropertySpecService propertySpecService) {
        super(eventService, thesaurus);
        this.customPropertySetService = customPropertySetService;
        this.protocolPluggableService = protocolPluggableService;
        this.propertySpecService = propertySpecService;
    }

    static ConnectionTypePluggableClassImpl from(DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(ConnectionTypePluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    ConnectionTypePluggableClassImpl initializeFrom(PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    public Discriminator discriminator() {
        return Discriminator.CONNECTIONTYE;
    }

    @Override
    protected void validateLicense() {
        // No license information to validate
    }

    @Override
    protected ConnectionType newInstance(PluggableClass pluggableClass) {
        return this.newInstance(pluggableClass.getJavaClassName());
    }

    private ConnectionType newInstance(String javaClassName) {
        return this.protocolPluggableService.createConnectionType(javaClassName);
    }

    @Override
    public String getTranslationKey() {
        return super.getName();
    }

    @Override
    public String getName() {
        return ConnectionTypePluggableClassTranslationKeys.translationFor(this, getThesaurus());
    }

    @Override
    public void save() {
        this.registerCustomPropertySet();
        super.save();
    }

    @Override
    public void delete() {
        this.newInstance().getCustomPropertySet().ifPresent(this::unregister);
        super.delete();
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        ConnectionType connectionType = this.newInstance();
        return connectionType.getPropertySpec(name);
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public ConnectionType getConnectionType() {
        ConnectionType connectionType = this.newInstance();
        connectionType.copyProperties(this.getProperties(connectionType.getPropertySpecs()));
        return connectionType;
    }

    @Override
    public PluggableClassType getPluggableClassType() {
        return PluggableClassType.ConnectionType;
    }

    public void registerCustomPropertySet() {
        this.newInstance().getCustomPropertySet().ifPresent(this::register);
    }

    private void register(CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet) {
        this.customPropertySetService.addSystemCustomPropertySet(customPropertySet);
    }

    private void unregister(CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet) {
        this.customPropertySetService.removeSystemCustomPropertySet(customPropertySet);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.CONNECTIONTYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.CONNECTIONTYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.CONNECTIONTYPE;
    }

    @Override
    public boolean isInstance(ConnectionType connectionType) {
        String javaClassName;
        if (connectionType instanceof UPLConnectionTypeAdapter) {
            javaClassName = ((UPLConnectionTypeAdapter) connectionType).getUplConnectionType().getClass().getName();
        } else {
            javaClassName = connectionType.getClass().getName();
        }

        return this.getJavaClassName().equals(javaClassName);
    }

    @Override
    public CustomPropertySetValues getPropertiesFor(ConnectionProvider connectionProvider, Instant effectiveTimestamp) {
        return this.newInstance()
                .getCustomPropertySet()
                .map(propertySet -> {
                    if (propertySet.isVersioned()) {
                        return this.customPropertySetService.getUniqueValuesFor(propertySet, connectionProvider, effectiveTimestamp);
                    } else {
                        return this.customPropertySetService.getUniqueValuesFor(propertySet, connectionProvider);
                    }
                })
                .orElseGet(() -> CustomPropertySetValues.emptyFrom(effectiveTimestamp));
    }

    @Override
    public void setPropertiesFor(ConnectionProvider connectionProvider, CustomPropertySetValues values, Instant effectiveTimestamp) {
        this.newInstance()
                .getCustomPropertySet()
                .ifPresent(propertySet -> {
                    if (propertySet.isVersioned()){
                        this.customPropertySetService.setValuesFor(propertySet, connectionProvider, values, effectiveTimestamp);
                    } else {
                        this.customPropertySetService.setValuesFor(propertySet, connectionProvider, values);
                    }
                });
    }

    @Override
    public void removePropertiesFor(ConnectionProvider connectionProvider) {
        this.newInstance()
                .getCustomPropertySet()
                .ifPresent(propertySet -> this.customPropertySetService.removeValuesFor(propertySet, connectionProvider));
    }

    @Override
    public int getNrOfRetries() {
        TypedProperties properties = getPluggableClass().getProperties(Arrays.asList(getNumberOfRetriesPropertySpec()));
        return properties.getIntegerProperty(NR_OF_RETRIES_ATTRIBUTE_NAME, BigDecimal.valueOf(DEFAULT_NR_OF_RETRIES)).intValue();
    }

    @Override
    public void updateNrOfRetries(int nrOfRetries) {
        this.getPluggableClass().setProperty(getNumberOfRetriesPropertySpec(), BigDecimal.valueOf(nrOfRetries));
    }

    private PropertySpec getNumberOfRetriesPropertySpec() {
        return propertySpecService
                .bigDecimalSpec()
                .named(TranslationKeys.NR_OF_RETRIES)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(BigDecimal.valueOf(DEFAULT_NR_OF_RETRIES))
                .finish();
    }
}