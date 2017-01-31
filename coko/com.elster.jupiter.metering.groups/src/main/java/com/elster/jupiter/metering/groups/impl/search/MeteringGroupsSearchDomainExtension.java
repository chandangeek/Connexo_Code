/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl.search;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsServiceImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.impl.search.MeteringGroupsSearchDomainExtension", service = SearchDomainExtension.class, immediate = true)
public class MeteringGroupsSearchDomainExtension implements SearchDomainExtension {

    private volatile MeteringGroupsServiceImpl meteringGroupsService;
    private volatile MeteringService meteringService;

    @SuppressWarnings("unused")
    public MeteringGroupsSearchDomainExtension() {
    }

    @Inject
    public MeteringGroupsSearchDomainExtension(MeteringGroupsService meteringGroupsService, MeteringService meteringService) {
        this();
        setMeteringGroupsService(meteringGroupsService);
        setMeteringService(meteringService);
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = (MeteringGroupsServiceImpl) meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.getDomainClass().isAssignableFrom(UsagePoint.class);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        DataModel injector = this.meteringGroupsService.getDataModel();
        return Collections.singletonList(injector.getInstance(UsagePointGroupSearchableProperty.class));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        return getProperties();
    }

    @Override
    public SqlFragment asFragment(List<SearchablePropertyCondition> conditions) {
        Condition condition = conditions.stream()
                .filter(c -> UsagePointGroupSearchableProperty.PROPERTY_NAME.equals(c.getProperty().getName()))
                .map(c -> UsagePointGroupSearchableProperty.toCondition(c.getCondition()))
                .findAny()
                .orElseThrow(
                        () -> new IllegalStateException("Missing searchable property condition for property: " + UsagePointGroupSearchableProperty.PROPERTY_NAME)
                );
        return this.meteringService.getUsagePointQuery().asSubquery(condition, "id").toFragment();
    }
}
