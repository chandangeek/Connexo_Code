/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.search.SearchableDeviceProperty;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SapAttributesDeviceSearchDomainExtension implements SearchDomainExtension {

    private final Clock clock;
    private final List<SearchableProperty> searchableProperties;

    @Inject
    public SapAttributesDeviceSearchDomainExtension(DataModel dataModel, Clock clock, Thesaurus thesaurus) {
        this.clock = clock;
        searchableProperties = new ArrayList<>();
        SapAttributesSearchablePropertyGroup searchablePropertyGroup = new SapAttributesSearchablePropertyGroup(thesaurus);
        searchableProperties.add(dataModel.getInstance(DeviceIdentifierSearchableProperty.class).init(searchablePropertyGroup));
        searchableProperties.add(dataModel.getInstance(DeviceLocationSearchableProperty.class).init(searchablePropertyGroup));
        searchableProperties.add(dataModel.getInstance(LogicalRegisterNumberSearchableProperty.class).init(searchablePropertyGroup));
        searchableProperties.add(dataModel.getInstance(PointOfDeliverySearchableProperty.class).init(searchablePropertyGroup));
        searchableProperties.add(dataModel.getInstance(ProfileIdSearchableProperty.class).init(searchablePropertyGroup));
        searchableProperties.add(dataModel.getInstance(RegisteredSearchableProperty.class).init(searchablePropertyGroup));
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.getDomainClass().isAssignableFrom(Device.class);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return searchableProperties;
    }

    @Override
    public SqlFragment asFragment(List<SearchablePropertyCondition> conditions) {
        Instant now = clock.instant();
        SqlBuilder builder = new SqlBuilder();
        Holder<String> holder = HolderBuilder.first("").andThen(" INTERSECT ");
        conditions.forEach(condition -> {
            Optional<SearchableProperty> property = getProperties().stream()
                    .filter(prop -> prop.getName().equals(condition.getProperty().getName())).findAny();
            if (property.isPresent()) {
                builder.append(holder.get());
                builder.add(((SearchableDeviceProperty)property.get()).toSqlFragment(condition.getCondition(), now));
            }
        });
        return builder;
    }

}
