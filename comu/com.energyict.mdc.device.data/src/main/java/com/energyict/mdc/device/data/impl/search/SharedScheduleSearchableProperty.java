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
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SharedScheduleSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.shared.schedule";

    private DeviceSearchDomain domain;
    private final SchedulingService schedulingService;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public SharedScheduleSearchableProperty(SchedulingService schedulingService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.schedulingService = schedulingService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    SharedScheduleSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ComSchedule;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ComSchedule) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addComSchedule();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment("csh.id", condition, now));
        sqlBuilder.append(" AND cte.obsolete_date is null ");
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public PropertySpec getSpecification() {
        return propertySpecService.referencePropertySpec(
                PROPERTY_NAME,
                false,
                FactoryIds.COMSCHEDULE,
                this.schedulingService.findAllSchedules());
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.SHARED_SCHEDULE).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }
}
