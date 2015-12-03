package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.time.Instant;
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

    @Inject
    public ConnectionTypePluggableClassImpl(EventService eventService, Thesaurus thesaurus, CustomPropertySetService customPropertySetService, ProtocolPluggableService protocolPluggableService) {
        super(eventService, thesaurus);
        this.customPropertySetService = customPropertySetService;
        this.protocolPluggableService = protocolPluggableService;
    }

    static ConnectionTypePluggableClassImpl from (DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(ConnectionTypePluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    ConnectionTypePluggableClassImpl initializeFrom (PluggableClass pluggableClass) {
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

    private ConnectionType newInstance (String javaClassName) {
        return this.protocolPluggableService.createConnectionType(javaClassName);
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
    public ConnectionType getConnectionType () {
        ConnectionType connectionType = this.newInstance();
        connectionType.copyProperties(this.getProperties(connectionType.getPropertySpecs()));
        return connectionType;
    }

    @Override
    public PluggableClassType getPluggableClassType () {
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
    public boolean isInstance (ConnectionType connectionType) {
        return this.getJavaClassName().getClass().equals(connectionType.getClass().getName());
    }

    @Override
    public CustomPropertySetValues getPropertiesFor(ConnectionProvider connectionProvider, Instant effectiveTimestamp) {
        return this.newInstance()
                .getCustomPropertySet()
                .map(propertySet -> this.customPropertySetService.getUniqueValuesFor(propertySet, connectionProvider, effectiveTimestamp))
                .orElseGet(() -> CustomPropertySetValues.emptyFrom(effectiveTimestamp));
    }

    @Override
    public void setPropertiesFor(ConnectionProvider connectionProvider, CustomPropertySetValues values, Instant effectiveTimestamp) {
        this.newInstance()
                .getCustomPropertySet()
                .ifPresent(propertySet -> this.customPropertySetService.setValuesFor(propertySet, connectionProvider, values, effectiveTimestamp));
    }

    @Override
    public void removePropertiesFor(ConnectionProvider connectionProvider) {
        this.newInstance()
                .getCustomPropertySet()
                .ifPresent(propertySet -> this.customPropertySetService.removeValuesFor(propertySet, connectionProvider));
    }

}