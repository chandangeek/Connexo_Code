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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConnectionStatusSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.connection.status";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ConnectionStatusSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    ConnectionStatusSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ConnectionStatusSearchWrapper;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return thesaurus.getFormat(((ConnectionStatusSearchWrapper) value).getContainer().translation()).format();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICE from DDC_CONNECTIONTASK where OBSOLETE_DATE IS NULL AND ");
        sqlBuilder.add(this.toSqlFragment("DDC_CONNECTIONTASK.STATUS", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        statement.setInt(bindPosition, ((ConnectionStatusSearchWrapper) value).getContainer().getStatus().ordinal());
    }

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        Stream<ConnectionStatusSearchWrapper> statusses = Arrays.stream(ConnectionStatusContainer.values()).map(ConnectionStatusSearchWrapper::new);
        return this.propertySpecService
                .specForValuesOf(new ConnectionStatusValueFactory())
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(statusses.toArray(ConnectionStatusSearchWrapper[]::new))
                .markExhaustive()
                .finish();
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
        return PropertyTranslationKeys.CONNECTION_STATUS;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    enum ConnectionStatusContainer {
        ACTIVE(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE, PropertyTranslationKeys.CONNECTION_TASK_STATUS_ACTIVE),
        INACTIVE(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE, PropertyTranslationKeys.CONNECTION_TASK_STATUS_INACTIVE),
        INCOMPLETE(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE, PropertyTranslationKeys.CONNECTION_TASK_STATUS_INCOMPLETE),
        ;

        private ConnectionTask.ConnectionTaskLifecycleStatus status;
        private TranslationKey translation;

        ConnectionStatusContainer(ConnectionTask.ConnectionTaskLifecycleStatus status, TranslationKey translation) {
            this.status = status;
            this.translation = translation;
        }

        public ConnectionTask.ConnectionTaskLifecycleStatus getStatus() {
            return status;
        }

        public TranslationKey translation() {
            return this.translation;
        }

    }

    private class ConnectionStatusValueFactory extends SearchHelperValueFactory<ConnectionStatusSearchWrapper> {
        ConnectionStatusValueFactory() {
            super(ConnectionStatusSearchWrapper.class);
        }

        @Override
        public ConnectionStatusSearchWrapper fromStringValue(String stringValue) {
            return Arrays.stream(ConnectionStatusContainer.values())
                    .filter(stk -> stk.name().equals(stringValue))
                    .map(ConnectionStatusSearchWrapper::new)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String toStringValue(ConnectionStatusSearchWrapper object) {
            return object.getId();
        }
    }

    static class ConnectionStatusSearchWrapper extends HasIdAndName {
        private final ConnectionStatusContainer container;

        ConnectionStatusSearchWrapper(ConnectionStatusContainer container) {
            this.container = container;
        }

        @Override
        public String getId() {
            return container.name();
        }

        @Override
        public String getName() {
            return container.translation().getKey();
        }

        public ConnectionStatusContainer getContainer() {
            return this.container;
        }
    }
}
