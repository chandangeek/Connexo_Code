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
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConnectionDirectionSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.connection.direction";

    private final PropertySpecService propertySpecService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ConnectionDirectionSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    ConnectionDirectionSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ConnectionTaskDirectionWrapper;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return this.getThesaurus().getFormat(((ConnectionTaskDirectionWrapper) value).getContainer().getTranslationKey()).format();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN (" +
                "select device " +
                "from ddc_connectiontask " +
                "left join dtc_partialconnectiontask on dtc_partialconnectiontask.id = ddc_connectiontask.partialconnectiontask " +
                "where ddc_connectiontask.obsolete_date is null and ");
        sqlBuilder.add(this.toSqlFragment("ddc_connectiontask.discriminator", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
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
        Stream<ConnectionTaskDirectionWrapper> stream =
                Arrays.stream(ConnectionTaskDirectionContainer.values())
                .map(ConnectionTaskDirectionWrapper::new);
        return this.propertySpecService
                .specForValuesOf(new ConnectionTaskDirectionValueFactory())
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(stream.toArray(ConnectionTaskDirectionWrapper[]::new))
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
        return PropertyTranslationKeys.CONNECTION_DIRECTION;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // no refresh
    }

    private enum ConnectionTaskDirectionContainer {
        INBOUND(PropertyTranslationKeys.CONNECTION_TASK_DIRECTION_INBOUND, ConnectionTaskImpl.INBOUND_DISCRIMINATOR),
        OUTBOUND(PropertyTranslationKeys.CONNECTION_TASK_DIRECTION_OUTBOUND, ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR),
        ;

        private TranslationKey translationKey;
        private String code;

        ConnectionTaskDirectionContainer(TranslationKey translationKey, String code) {
            this.translationKey = translationKey;
            this.code = code;
        }

        public TranslationKey getTranslationKey() {
            return this.translationKey;
        }

        public String getCode() {
            return this.code;
        }
    }

    private class ConnectionTaskDirectionValueFactory extends SearchHelperValueFactory<ConnectionTaskDirectionWrapper> {
        private ConnectionTaskDirectionValueFactory() {
            super(ConnectionTaskDirectionWrapper.class);
        }

        @Override
        public ConnectionTaskDirectionWrapper fromStringValue(String stringValue) {
            return Arrays.stream(ConnectionTaskDirectionContainer.values())
                    .filter(dc -> dc.code.equals(stringValue))
                    .map(ConnectionTaskDirectionWrapper::new)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String toStringValue(ConnectionTaskDirectionWrapper object) {
            return object.getId();
        }
    }
    static final class ConnectionTaskDirectionWrapper extends HasIdAndName {
        private ConnectionTaskDirectionContainer container;

        ConnectionTaskDirectionWrapper(ConnectionTaskDirectionContainer container) {
            this.container = container;
        }

        @Override
        public String getId() {
            return this.container.getCode();
        }

        @Override
        public String getName() {
            return this.container.getTranslationKey().getDefaultFormat();
        }

        public ConnectionTaskDirectionContainer getContainer() {
            return container;
        }
    }
}

