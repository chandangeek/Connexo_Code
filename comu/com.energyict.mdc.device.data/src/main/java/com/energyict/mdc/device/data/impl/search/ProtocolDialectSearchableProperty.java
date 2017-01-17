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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtocolDialectSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.protocol.dialect";

    private final PropertySpecService propertySpecService;
    private final ProtocolPluggableService protocolPluggableService;

    private DeviceSearchDomain domain;
    private SearchableProperty parent;
    private List<ProtocolDialect> protocolDialects = Collections.emptyList();

    @Inject
    public ProtocolDialectSearchableProperty(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
    }

    ProtocolDialectSearchableProperty init(DeviceSearchDomain domain, SearchableProperty parent) {
        this.domain = domain;
        this.parent = parent;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ProtocolDialect;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        ProtocolDialect protocolDialect = (ProtocolDialect) value;
        return protocolDialect.getName() + " (" + protocolDialect.getPluggableClass().getName() + ")";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addDeviceType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.DEVICE_TYPE + ".DEVICEPROTOCOLPLUGGABLEID", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        statement.setLong(bindPosition, ((ProtocolDialect) value).getPluggableClass().getId());
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
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new ProtocolDialectValueFactory())
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(this.protocolDialects.toArray(new ProtocolDialect[this.protocolDialects.size()]))
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
        return PropertyTranslationKeys.PROTOCOL_DIALECT;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.parent);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (constrictions.size() != 1) {
            throw new IllegalArgumentException("Expecting a constraint on the device type");
        }
        this.refreshWithConstrictions(constrictions.get(0));
    }

    private void refreshWithConstrictions(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            this.refreshWithConstrictionValues(constriction.getConstrainingValues());
        } else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }

    private void refreshWithConstrictionValues(List<Object> deviceTypes) {
        this.validateObjectsType(deviceTypes);
        this.protocolDialects = deviceTypes.stream()
                .map(DeviceType.class::cast)
                .flatMap(this::getProtocolDialectsOnDeviceType)
                .sorted((pd1, pd2) -> pd1.getName().compareToIgnoreCase(pd2.getName()))
                .collect(Collectors.toList());
    }

    private void validateObjectsType(List<Object> objectsForValidation) {
        objectsForValidation.stream()
                .filter(Predicates.not(DeviceType.class::isInstance))
                .findAny()
                .ifPresent(badObj -> {
                    throw new IllegalArgumentException("Parents are expected to be of type " + DeviceType.class.getName());
                });
    }

    private Stream<ProtocolDialect> getProtocolDialectsOnDeviceType(DeviceType deviceType) {
        return deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol()
                        .getDeviceProtocolDialects()
                        .stream()
                        .map(protocolDialect -> new ProtocolDialect(deviceProtocolPluggableClass, protocolDialect)))
                .orElse(Stream.empty());
    }

    private class ProtocolDialectValueFactory extends SearchHelperValueFactory<ProtocolDialect> {
        private ProtocolDialectValueFactory() {
            super(ProtocolDialect.class);
        }

        @Override
        public ProtocolDialect fromStringValue(String stringValue) {
            if (stringValue != null) {
                String[] keyParts = stringValue.split(ProtocolDialect.KEY_DELIMITER);
                if (keyParts.length == 2
                        && !Checks.is(keyParts[0]).emptyOrOnlyWhiteSpace()
                        && !Checks.is(keyParts[1]).emptyOrOnlyWhiteSpace()) {
                    Optional<DeviceProtocolPluggableClass> pluggableClassOptional = protocolPluggableService.findDeviceProtocolPluggableClass(Long.valueOf(keyParts[0]));
                    if (pluggableClassOptional.isPresent()) {
                        return pluggableClassOptional.get().getDeviceProtocol().getDeviceProtocolDialects()
                                .stream()
                                .filter(protocolDialect -> protocolDialect.getDeviceProtocolDialectName().equals(keyParts[1]))
                                .findFirst()
                                .map(protocolDialect -> new ProtocolDialect(pluggableClassOptional.get(), protocolDialect))
                                .orElse(null);
                    }
                }
            }
            return null;
        }

        @Override
        public String toStringValue(ProtocolDialect object) {
            return object.getId();
        }
    }

    static final class ProtocolDialect extends HasIdAndName {
        static final String KEY_DELIMITER = ";";

        private final DeviceProtocolPluggableClass pluggableClass;
        private final DeviceProtocolDialect protocolDialect;

        private String id;

        ProtocolDialect(DeviceProtocolPluggableClass pluggableClass, DeviceProtocolDialect protocolDialect) {
            this.pluggableClass = pluggableClass;
            this.protocolDialect = protocolDialect;
        }

        public static String getCompositeKey(DeviceProtocolPluggableClass pluggableClass, DeviceProtocolDialect protocolDialect) {
            return pluggableClass.getId() + KEY_DELIMITER + protocolDialect.getDeviceProtocolDialectName();
        }

        @Override
        public String getId() {
            if (this.id == null) {
                this.id = getCompositeKey(getPluggableClass(), getProtocolDialect());
            }
            return this.id;
        }

        @Override
        public String getName() {
            return getProtocolDialect().getDeviceProtocolDialectDisplayName();
        }

        public DeviceProtocolPluggableClass getPluggableClass() {
            return this.pluggableClass;
        }

        public DeviceProtocolDialect getProtocolDialect() {
            return this.protocolDialect;
        }
    }
}
