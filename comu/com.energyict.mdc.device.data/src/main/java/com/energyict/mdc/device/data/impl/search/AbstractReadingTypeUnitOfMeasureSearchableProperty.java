package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractReadingTypeUnitOfMeasureSearchableProperty<T> extends AbstractSearchableDeviceProperty {

    private final Class<T> implClass;
    private final MeteringService meteringService;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    public AbstractReadingTypeUnitOfMeasureSearchableProperty(Class<T> clazz, MeteringService meteringService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.implClass = clazz;
        this.meteringService = meteringService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    T init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this.implClass.cast(this);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof UnitOfMeasureInfo;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((UnitOfMeasureInfo) value).getName();
    }

    @Override
    public final void appendJoinClauses(JoinClauseBuilder builder) {
        // nothing to join to device table
    }

    /**
     * @return join clause for MDS_MEASUREMENTTYPE
     */
    public abstract String getMeasurementTypeJoinSql();


    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        Contains contains = (Contains) condition;
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID");
        if (contains.getOperator() == ListOperator.NOT_IN) {
            sqlBuilder.append(" NOT");
        }
        sqlBuilder.append(" IN (");
        sqlBuilder.append("select DEVICECONFIGID " +
                "from ");
        sqlBuilder.append(getMeasurementTypeJoinSql());
        sqlBuilder.append(" where ");
        sqlBuilder.append(contains.getCollection().stream()
                .map(UnitOfMeasureInfo.class::cast)
                .map(unit -> {
                    StringBuilder builder = new StringBuilder(' ');
                    builder.append("MDS_MEASUREMENTTYPE.readingtype like '%.%.%.%.%.%.%.%.%.%.%.%.%.%.%.");
                    builder.append(unit.getMetricMultiplier().getId());
                    builder.append(".");
                    builder.append(unit.getReadingTypeUnit().getId());
                    builder.append(".%' ");
                    return builder.toString();
                })
                .collect(Collectors.joining(" OR ")));
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
        return Optional.of(group);
    }

    @Override
    public abstract String getName();

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.stringReferencePropertySpec(
                getName(),
                false,
                new UnitOfMeasureFinder(),
                getPossibleValues()
        );
    }

    private UnitOfMeasureInfo[] getPossibleValues() {
        return this.meteringService.getAvailableNonEquidistantReadingTypes().stream()
                .map(rt -> new UnitOfMeasureInfo(rt.getMultiplier(), rt.getUnit()))
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .toArray(UnitOfMeasureInfo[]::new);
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.READING_TYPE_UNIT_OF_MEASURE).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    static class UnitOfMeasureFinder implements CanFindByStringKey<UnitOfMeasureInfo> {

        @Override
        public Optional<UnitOfMeasureInfo> find(String key) {
            String[] parts = key.split(":");
            if (parts.length != 2) {
                return Optional.empty();
            }
            try {
                MetricMultiplier metricMultiplier = MetricMultiplier.get(Integer.parseInt(parts[0]));
                ReadingTypeUnit readingTypeUnit = ReadingTypeUnit.get(Integer.parseInt(parts[1]));
                return Optional.of(new UnitOfMeasureInfo(metricMultiplier, readingTypeUnit));
            } catch (NumberFormatException | IllegalEnumValueException e) {
                return Optional.empty();
            }
        }

        @Override
        public Class<UnitOfMeasureInfo> valueDomain() {
            return UnitOfMeasureInfo.class;
        }
    }

    static class UnitOfMeasureInfo extends HasIdAndName {

        private MetricMultiplier metricMultiplier;
        private ReadingTypeUnit readingTypeUnit;

        public UnitOfMeasureInfo(MetricMultiplier metricMultiplier, ReadingTypeUnit readingTypeUnit) {
            this.metricMultiplier = metricMultiplier;
            this.readingTypeUnit = readingTypeUnit;
        }

        @Override
        public String getId() {
            return metricMultiplier.getId() + ":" + readingTypeUnit.getId();
        }

        @Override
        public String getName() {
            return metricMultiplier.getSymbol() + readingTypeUnit.getSymbol();
        }

        public MetricMultiplier getMetricMultiplier() {
            return metricMultiplier;
        }

        public ReadingTypeUnit getReadingTypeUnit() {
            return readingTypeUnit;
        }
    }
}
