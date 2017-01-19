package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.dynamic.TemporalAmountValueFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelIntervalSearchableProperty extends AbstractSearchableDeviceProperty {

    static final TemporalAmountValueFactory TIME_DURATION_VALUE_FACTORY = new TemporalAmountValueFactory();
    static final String PROPERTY_NAME = "device.channel.interval";

    private final PropertySpecService propertySpecService;
    private final MasterDataService masterDataService;

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    @Inject
    public ChannelIntervalSearchableProperty(PropertySpecService propertySpecService, MasterDataService masterDataService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.masterDataService = masterDataService;
    }

    ChannelIntervalSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof TimeDurationWrapper;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return value.toString();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        Contains contains = (Contains) condition;
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN (");
        sqlBuilder.append("select DEVICECONFIGID from DTC_CHANNELSPEC where ");
        if (contains.getOperator() == ListOperator.NOT_IN) {
            sqlBuilder.append(" NOT ");
        }
        sqlBuilder.openBracket();
        sqlBuilder.append(contains.getCollection().stream()
                .map(TimeDurationWrapper.class::cast)
                .map(interval -> "interval = " + interval.getCount() + " AND intervalcode = " + interval.getUnitCode())
                .collect(Collectors.joining(" OR ")));
        sqlBuilder.closeBracket();
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
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        Stream<TimeDurationWrapper> defaultValues = DecoratedStream.decorate(this.masterDataService.findAllLoadProfileTypes().stream())
                .map(LoadProfileType::interval)
                .map(Temporals::toTimeDuration)
                .map(TimeDurationWrapper::new)
                .distinct(TimeDurationWrapper::getId);
        return this.propertySpecService
                .specForValuesOf(new TimeDurationWrapperValueFactory())
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(defaultValues.toArray(TimeDurationWrapper[]::new))
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
        return PropertyTranslationKeys.CHANNEL_INTERVAL;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    static class TimeDurationWrapper extends HasIdAndName {
        private TimeDuration timeDuration;

        TimeDurationWrapper(TimeDuration timeDuration) {
            this.timeDuration = timeDuration;
        }

        @Override
        public String getId() {
            return TIME_DURATION_VALUE_FACTORY.toStringValue(this.timeDuration);
        }

        @Override
        public String getName() {
            return this.timeDuration.toString();
        }

        public int getCount() {
            return this.timeDuration.getCount();
        }

        public int getUnitCode() {
            return this.timeDuration.getTimeUnitCode();
        }

        @Override
        public String toString() {
            return this.timeDuration.toString();
        }
    }

    class TimeDurationWrapperValueFactory extends SearchHelperValueFactory<TimeDurationWrapper> {
        private TimeDurationWrapperValueFactory() {
            super(TimeDurationWrapper.class);
        }

        @Override
        public TimeDurationWrapper fromStringValue(String stringValue) {
            return new TimeDurationWrapper(TIME_DURATION_VALUE_FACTORY.fromStringValue(stringValue));
        }

        @Override
        public String toStringValue(TimeDurationWrapper object) {
            return object.getId();
        }
    }

}