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
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConnectionDirectionSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.connection.direction";

    private final PropertySpecService propertySpecService;

    private final Thesaurus thesaurus;
    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;
    private ConnectionTaskDirection[] connectionTasksDirections = new ConnectionTaskDirection[2];

    @Inject
    public ConnectionDirectionSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    ConnectionDirectionSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        this.connectionTasksDirections[0] = new ConnectionTaskDirection(ConnectionTask.Type.OUTBOUND);
        this.connectionTasksDirections[1] = new ConnectionTaskDirection(ConnectionTask.Type.INBOUND);
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ConnectionTaskDirection;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ConnectionTaskDirection) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append("dev.ID in (" + "select device from ddc_connectiontask left join dtc_partialconnectiontask" +
                " on dtc_partialconnectiontask.id=ddc_connectiontask.partialconnectiontask  where ddc_connectiontask.obsolete_date" +
                " is null and ");
        sqlBuilder.add(this.toSqlFragment("ddc_connectiontask.discriminator", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
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
                new DirectionFinder(),
                connectionTasksDirections);
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.CONNECTION_DIRECTION).format();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // no refresh
    }

    public static final class DirectionFinder implements CanFindByStringKey<ConnectionTaskDirection> {


        public DirectionFinder() {
        }

        @Override
        public Optional<ConnectionTaskDirection> find(String key) {
            if (key != null) {
                int position = Integer.parseInt(key);
                if (position >=0 && position < ConnectionTask.Type.values().length){
                    return Optional.of(new ConnectionTaskDirection(ConnectionTask.Type.values()[position]));
                }
            }
            return Optional.empty();
        }

        @Override
        public Class<ConnectionTaskDirection> valueDomain() {
            return ConnectionTaskDirection.class;
        }
    }

    static final class ConnectionTaskDirection extends HasIdAndName {

        private long id;
        private String displayValue;

        public ConnectionTaskDirection(ConnectionTask.Type type) {
            id = type.ordinal();
            displayValue = type.name();

        }


        @Override
        public Long getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.displayValue;
        }
    }
}

