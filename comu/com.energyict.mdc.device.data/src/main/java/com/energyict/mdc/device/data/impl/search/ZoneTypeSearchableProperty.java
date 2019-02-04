package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;


import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ZoneTypeSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.zoneType";
    private final MeteringZoneService meteringZoneService;
    private final PropertySpecService propertySpecService;
    private SearchablePropertyGroup group;
    private DeviceSearchDomain domain;

    @Inject
    public ZoneTypeSearchableProperty(MeteringZoneService meteringZoneService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.meteringZoneService = meteringZoneService;
        this.propertySpecService = propertySpecService;
    }

    ZoneTypeSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ZoneType;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ZoneType)value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {

        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".id IN (");
        builder.append("select enddevice " +
                "from MTZ_ZONETOENDDEVICE " +
                "where ZONE in (select ID from MTZ_ZONE where ");
        builder.add(this.toSqlFragment("zonetype", condition, now));
        builder.closeBracket();
        builder.closeBracket();
        return builder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        if (value instanceof ConnectionMethodSearchableProperty.ConnectionMethodInfo) {
            statement.setObject(bindPosition, ((ConnectionMethodSearchableProperty.ConnectionMethodInfo) value).getId());
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
        return Optional.of(group);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public PropertySpec getSpecification() {
        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes("MDC").stream().collect(Collectors.toList());
        return propertySpecService
                .referenceSpec(ZoneType.class)
                .named(this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(zoneTypes.toArray(new ZoneType[zoneTypes.size()]))
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
        return PropertyTranslationKeys.ZONE_TYPE;
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
