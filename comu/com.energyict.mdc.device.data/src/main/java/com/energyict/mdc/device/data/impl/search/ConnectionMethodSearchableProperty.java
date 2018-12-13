/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectionMethodSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.connection.method";
    private final ProtocolPluggableService protocolPluggableService;
    private final PropertySpecService propertySpecService;
    private DeviceSearchDomain domain;

    @Inject
    public ConnectionMethodSearchableProperty(ProtocolPluggableService protocolPluggableService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.protocolPluggableService = protocolPluggableService;
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ConnectionMethodInfo;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ConnectionMethodInfo) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    ConnectionMethodSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (");
        builder.append("select device " +
                "from DDC_CONNECTIONTASK " +
                "where ");
        builder.add(this.toSqlFragment("CONNECTIONTYPEPLUGGABLECLASS", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        if (value instanceof ConnectionMethodInfo) {
            statement.setObject(bindPosition, ((ConnectionMethodInfo) value).getId());
        } else {
            super.bindSingleValue(statement, bindPosition, value);
        }
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return true;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public PropertySpec getSpecification() {
        return propertySpecService
                .specForValuesOf(new ConnectionMethodValueFactory(this.protocolPluggableService))
                .named(this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(getPossibleValues())
                .markExhaustive()
                .finish();
    }

    private ConnectionMethodInfo[] getPossibleValues() {
        List<ConnectionMethodInfo> connectionMethods = this.protocolPluggableService.findAllConnectionTypePluggableClasses()
                .stream()
                .map(ConnectionMethodInfo::new)
                .collect(Collectors.toList());
        return connectionMethods.toArray(new ConnectionMethodInfo[connectionMethods.size()]);
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.CONNECTION_METHOD;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    static class ConnectionMethodValueFactory extends SearchHelperValueFactory<ConnectionMethodInfo> {
        private final ProtocolPluggableService protocolPluggableService;

        private ConnectionMethodValueFactory(ProtocolPluggableService protocolPluggableService) {
            super(ConnectionMethodInfo.class);
            this.protocolPluggableService = protocolPluggableService;
        }

        @Override
        public ConnectionMethodInfo fromStringValue(String stringValue) {
            if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
                return null;
            }
            Optional<ConnectionTypePluggableClass> connectionTypePluggableClass = this.protocolPluggableService.findConnectionTypePluggableClass(Long.valueOf(stringValue));
            if (!connectionTypePluggableClass.isPresent()) {
                return null;
            }
            return new ConnectionMethodInfo(connectionTypePluggableClass.get());
        }

        @Override
        public String toStringValue(ConnectionMethodInfo object) {
            return object.getId();
        }
    }

    static class ConnectionMethodInfo extends HasIdAndName {
        ConnectionTypePluggableClass ctpClass;

        ConnectionMethodInfo(ConnectionTypePluggableClass ctpClass) {
            this.ctpClass = ctpClass;
        }

        @Override
        public String getId() {
            return String.valueOf(ctpClass.getId());
        }

        @Override
        public String getName() {
            return ctpClass.getName();
        }

        public ConnectionTypePluggableClass getConnectionTypeClass() {
            return ctpClass;
        }
    }
}
