package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConnectionCommunicationPortPoolSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.connection.portpool";

    private final PropertySpecService propertySpecService;
    private final EngineConfigurationService engineConfigurationService;
    private final Thesaurus thesaurus;
    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ConnectionCommunicationPortPoolSearchableProperty(PropertySpecService propertySpecService, EngineConfigurationService engineConfigurationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.engineConfigurationService = engineConfigurationService;
        this.thesaurus = thesaurus;
    }

    ConnectionCommunicationPortPoolSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ComPortPool;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ComPortPool) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append("dev.ID in (" + "select device from ddc_connectiontask left join dtc_partialconnectiontask" +
                " on dtc_partialconnectiontask.id=ddc_connectiontask.partialconnectiontask  where ddc_connectiontask.obsolete_date" +
                " is null and ");
        sqlBuilder.add(this.toSqlFragment("dtc_partialconnectiontask.comportpool", condition, now));
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
        return this.propertySpecService.referencePropertySpec(
                PROPERTY_NAME,
                false,
                FactoryIds.CONNECTION_TASK,
                engineConfigurationService.findAllComPortPools()
        );
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
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.CONNECTION_PORTPOOL).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // no refresh
    }
}
