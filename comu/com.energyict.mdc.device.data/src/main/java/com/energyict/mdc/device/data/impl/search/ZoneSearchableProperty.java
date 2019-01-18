package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
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
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.dynamic.PropertySpecService;


import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ZoneSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.zoneName";
    private final MeteringZoneService meteringZoneService;
    private final PropertySpecService propertySpecService;
    private SearchableProperty zoneTypeProperty;
    private DeviceSearchDomain domain;
    SearchablePropertyGroup group;
    private List<Zone>  zones = new ArrayList<Zone>();

    @Inject
    public ZoneSearchableProperty(MeteringZoneService meteringZoneService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.meteringZoneService = meteringZoneService;
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Zone;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((Zone) value).getName() + " (" + ((Zone) value).getZoneType().getName() + ") ";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    ZoneSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group, SearchableProperty zoneTypeProperty) {
        this.domain = domain;
        this.group = group;
        this.zoneTypeProperty = zoneTypeProperty;
        return this;
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {

        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (");
        builder.append("select enddevice " +
                "from MTZ_ZONETOENDDEVICE " +
                "where ");
        builder.add(this.toSqlFragment("zone", condition, now));;
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
        return false;
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

        return propertySpecService
                .referenceSpec(Zone.class)
                .named(this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(zones.toArray(new Zone[zones.size()]))
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
        return PropertyTranslationKeys.ZONE_NAME;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Arrays.asList(this.zoneTypeProperty);
    }


    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // We have at most one constraint
        if (constrictions.size() != 1) {
            throw new IllegalArgumentException("Expecting exactly 1 constriction, i.e. the constraint on the device type");
        }
        this.refreshWithConstriction(constrictions.get(0));
    }

    private void refreshWithConstriction(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(ZoneTypeSearchableProperty.PROPERTY_NAME)) {
            this.refreshWithConstrictionValues(constriction.getConstrainingValues());
        }
        else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }


    private void refreshWithConstrictionValues(List<Object> list) {
        this.validateAllParentsAreZoneTypes(list);
        zones = meteringZoneService.
                getZones("MDC", meteringZoneService.newZoneFilter().
                        setZoneTypes(list.stream()
                                .map(ZoneType.class::cast)
                                .map(zoneType -> zoneType.getId())
                                .collect(Collectors.toList())))
                .stream().collect(Collectors.toList());
    }

    private void validateAllParentsAreZoneTypes(List<Object> list) {
        Optional<Object> anyNonDeviceType =
                list.stream()
                        .filter(Predicates.not(ZoneType.class::isInstance))
                        .findAny();
        if (anyNonDeviceType.isPresent()) {
            throw new IllegalArgumentException("Constricting values are expected to be of type " + ZoneType.class.getName());
        }
    }

}
