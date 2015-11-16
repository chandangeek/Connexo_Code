package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComTaskScheduleTypeSearchableProperty extends AbstractSearchableDeviceProperty {
    static final ScheduleTypeFinder SCHEDULE_TYPE_FINDER = new ScheduleTypeFinder();
    static final String PROPERTY_NAME = "device.comtask.schedule.type";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ComTaskScheduleTypeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    ComTaskScheduleTypeSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ScheduleType;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        String name = ((ScheduleType) value).getName();
        return thesaurus.getString(PROPERTY_NAME+ "." + name, name);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN (");
        sqlBuilder.append("select DEVICE from DDC_COMTASKEXEC where OBSOLETE_DATE IS NULL AND ( ");
        sqlBuilder.append(((Contains) condition).getCollection()
                .stream()
                .map(ScheduleType.class::cast)
                .map(ScheduleType::getScheduleTypeKey)
                .map(ScheduleTypeKey::getSQLStatement)
                .collect(Collectors.joining(" OR "))
        );
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, Object value, int bindPosition) throws SQLException {
        statement.setString(bindPosition, toDisplayAfterValidation(value));
    }

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
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
        return this.propertySpecService.stringReferencePropertySpec(
                PROPERTY_NAME,
                false,
                SCHEDULE_TYPE_FINDER,
                Arrays.stream(ScheduleTypeKey.values())
                        .map(ScheduleType::new)
                        .collect(Collectors.toList())
                        .toArray(new ScheduleType[ScheduleTypeKey.values().length])
        );
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.COMTASK_SCHEDULE_TYPE).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    enum ScheduleTypeKey {
        ON_REQUEST("ScheduleTypeKey.ON_REQUEST") {
            @Override
            public String getSQLStatement() {
                return "DISCRIMINATOR = 1 AND NEXTEXECUTIONSPECS IS NULL";
            }
        },
        INDIVIDUAL("ScheduleTypeKey.INDIVIDUAL") {
            @Override
            public String getSQLStatement() {
                return "DISCRIMINATOR = 1 AND NEXTEXECUTIONSPECS IS NOT NULL";
            }
        },
        SHARED("ScheduleTypeKey.SHARED") {
            @Override
            public String getSQLStatement() {
                return "DISCRIMINATOR = 0";
            }
        };

        private String key;

        ScheduleTypeKey(String key) {
            this.key = key;
        }

        public String translation() {
            return this.key;
        }

        public abstract String getSQLStatement();
    }

    static class ScheduleTypeFinder implements CanFindByStringKey<ScheduleType> {
        @Override
        public Optional<ScheduleType> find(String key) {
            return Arrays.stream(ScheduleTypeKey.values())
                    .filter(stk -> stk.name().equals(key))
                    .map(ScheduleType::new)
                    .findFirst();
        }

        @Override
        public Class<ScheduleType> valueDomain() {
            return ScheduleType.class;
        }
    }

    static class ScheduleType extends HasIdAndName {
        private final ScheduleTypeKey scheduleTypeKey;

        ScheduleType(ScheduleTypeKey scheduleTypeKey) {
            this.scheduleTypeKey = scheduleTypeKey;
        }

        @Override
        public String getId() {
            return scheduleTypeKey.name();
        }

        @Override
        public String getName() {
            return scheduleTypeKey.translation();
        }

        public ScheduleTypeKey getScheduleTypeKey() {
            return scheduleTypeKey;
        }
    }
}
