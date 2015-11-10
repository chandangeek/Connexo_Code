package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChannelLastValueSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.channel.last.value";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup propertyGroup;

    @Inject
    public ChannelLastValueSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    ChannelLastValueSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup parent) {
        this.domain = domain;
        this.propertyGroup = parent;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return false;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return null;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    /**
     * <code>
     * select MTR_METERACTIVATION.METERID from MTR_CHANNEL
     * right join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID AND MTR_METERACTIVATION.STARTTIME > {now} AND MTR_METERACTIVATION.ENDTIME < {now}
     * left join IDS_TIMESERIES on MTR_CHANNEL.TIMESERIESID = IDS_TIMESERIES.ID
     * where IDS_TIMESERIES.LASTTIME = {criteria};
     * </code>
     */
    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".id IN (");
        builder.append("select MTR_METERACTIVATION.METERID from MTR_CHANNEL " +
                "right join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID AND MTR_METERACTIVATION.STARTTIME < ");
        builder.addLong(now.toEpochMilli());
        builder.append(" AND MTR_METERACTIVATION.ENDTIME > ");
        builder.addLong(now.toEpochMilli());
        builder.append(" left join IDS_TIMESERIES on MTR_CHANNEL.TIMESERIESID = IDS_TIMESERIES.ID " +
                "where ");
        builder.add(toSqlFragment("IDS_TIMESERIES.LASTTIME", condition, now));
        builder.closeBracket();
        return builder;
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
        return Optional.of(this.propertyGroup);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService.basicPropertySpec(
                PROPERTY_NAME,
                false,
                new InstantFactory()
        );
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.CHANNEL_LAST_VALUE).format();
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
