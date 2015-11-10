package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;
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
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChannelIntervalSearchableProperty extends AbstractSearchableDeviceProperty {

    static final TimeDurationValueFactory TIME_DURATION_VALUE_FACTORY = new TimeDurationValueFactory();
    static final String PROPERTY_NAME = "device.channel.interval";

    private final PropertySpecService propertySpecService;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    @Inject
    public ChannelIntervalSearchableProperty(PropertySpecService propertySpecService, MasterDataService masterDataService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
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
                .map(interval -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("interval = ");
                    builder.append(interval.getCount());
                    builder.append(" AND intervalcode = ");
                    builder.append(interval.getUnitCode());
                    return builder.toString();
                })
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
        List<TimeDurationWrapper> defaultValues = DecoratedStream.decorate(this.masterDataService.findAllLoadProfileTypes().stream())
                .map(LoadProfileType::getInterval)
                .map(TimeDurationWrapper::new)
                .distinct(TimeDurationWrapper::getId)
                .collect(Collectors.toList());
        return this.propertySpecService.stringReferencePropertySpec(
                PROPERTY_NAME,
                false,
                new TimeDurationFinder(),
                defaultValues.toArray(new TimeDurationWrapper[defaultValues.size()]));
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.CHANNEL_INTERVAL).format();
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

        public TimeDurationWrapper(TimeDuration timeDuration) {
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

        public int getCount(){
            return this.timeDuration.getCount();
        }

        public int getUnitCode(){
            return this.timeDuration.getTimeUnitCode();
        }

        @Override
        public String toString() {
            return this.timeDuration.toString();
        }
    }

    static class TimeDurationFinder implements CanFindByStringKey<TimeDurationWrapper> {

        @Override
        public Optional<TimeDurationWrapper> find(String key) {
            return Optional.of(new TimeDurationWrapper(TIME_DURATION_VALUE_FACTORY.fromStringValue(key)));
        }

        @Override
        public Class<TimeDurationWrapper> valueDomain() {
            return TimeDurationWrapper.class;
        }
    }
}
