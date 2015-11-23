package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChannelReadingTypeNameSearchableProperty extends AbstractReadingTypeNameSearchableProperty<ChannelReadingTypeNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.reading.type.name";

    @Inject
    public ChannelReadingTypeNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelReadingTypeNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addChannelReadingType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("ch_rt.aliasname", condition, now);
    }
}
