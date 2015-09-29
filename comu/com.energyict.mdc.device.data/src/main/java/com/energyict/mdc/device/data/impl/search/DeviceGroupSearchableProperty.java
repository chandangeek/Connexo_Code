package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeviceGroupSearchableProperty  extends AbstractSearchableDeviceProperty {

        public static final String PROPERTY_NAME = DeviceFields.DEVICEGROUP.fieldName();
        private DeviceSearchDomain domain;
        private final MeteringGroupsService meteringGroupsService;
        private final PropertySpecService propertySpecService;
        private final Thesaurus thesaurus;

        @Inject
        public DeviceGroupSearchableProperty(MeteringGroupsService meteringGroupsService, PropertySpecService propertySpecService, Thesaurus thesaurus/*, DataModel dataModel*/) {
            super();
            this.meteringGroupsService = meteringGroupsService;
            this.propertySpecService = propertySpecService;
            this.thesaurus = thesaurus;
        }

    DeviceGroupSearchableProperty init(DeviceSearchDomain domain) {
            this.domain = domain;
            return this;
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
            return Optional.empty();
        }

        @Override
        public Visibility getVisibility() {
            return Visibility.STICKY;
        }

        @Override
        public SelectionMode getSelectionMode() {
            return SelectionMode.MULTI;
        }

        @Override
        public String getDisplayName() {
            return this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_GROUP).format();
        }

        @Override
        public PropertySpec getSpecification() {
            return this.propertySpecService.referencePropertySpec(
                    PROPERTY_NAME,
                    false,
                    FactoryIds.DEVICE_GROUP,
                    this.meteringGroupsService.findEndDeviceGroups());
        }

        @Override
        public List<SearchableProperty> getConstraints() {
            return Collections.emptyList();
        }

        @Override
        public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
            // Nothing to refresh
        }

        @Override
        public void appendJoinClauses(JoinClauseBuilder builder) {
            builder
                    .addEndDevice()
                    .addDeviceGroup();
        }

        @Override
        public SqlFragment toSqlFragment(Condition condition, Instant now) {
            // TODO: handling of dynamic device groups
            return this.toSqlFragment("edg.group_id", condition, now);
        }

        @Override
        protected boolean valueCompatibleForDisplay(Object value) {
            return value instanceof EndDeviceGroup;
        }

        @Override
        protected String toDisplayAfterValidation(Object value) {
            return ((EndDeviceGroup) value).getName();
        }
    }
