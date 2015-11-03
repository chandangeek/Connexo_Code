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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
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
    private final Thesaurus thesaurus;

    private DeviceSearchDomain domain;
    private SearchableProperty parent;
    private List<ProtocolDialect> protocolDialects = Collections.emptyList();

    @Inject
    public ProtocolDialectSearchableProperty(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.thesaurus = thesaurus;
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
        return ((ProtocolDialect) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addDeviceType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.DEVICE_TYPE + ".DEVICEPROTOCOLPLUGGABLEID", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    /**
     * workaround because ProtocolDialect can not implement HasId and it is not a simple Java class,
     * {@link AbstractSearchableDeviceProperty.ProxyAwareSqlFragment#bindSingleValue(PreparedStatement, Object, int)},
     * as a result we unable to bind instances of this class into prepared statement
     */
    @Override
    public void visitContains(Contains contains) {
        super.visitContains(contains.getOperator().contains(contains.getFieldName(), contains.getCollection()
                .stream()
                .map(obj -> ((ProtocolDialect) obj).getPluggableClass().getId())
                .collect(Collectors.toList())));
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
        return this.propertySpecService.stringReferencePropertySpec(
                PROPERTY_NAME,
                false,
                new ProtocolDialectFinder(this.protocolPluggableService),
                this.protocolDialects.toArray(new ProtocolDialect[this.protocolDialects.size()]));
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.PROTOCOL_DIALECT).format();
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
                .flatMap(deviceType -> getProtocolDialectsOnDeviceType(deviceType))
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
        DeviceProtocolPluggableClass pluggableClass = deviceType.getDeviceProtocolPluggableClass();
        return pluggableClass.getDeviceProtocol().getDeviceProtocolDialects().stream()
                .map(protocolDialect -> new ProtocolDialect(pluggableClass, protocolDialect));
    }

    public static final class ProtocolDialectFinder implements CanFindByStringKey<ProtocolDialect> {
        private final ProtocolPluggableService protocolPluggableService;

        public ProtocolDialectFinder(ProtocolPluggableService protocolPluggableService) {
            this.protocolPluggableService = protocolPluggableService;
        }

        @Override
        public Optional<ProtocolDialect> find(String key) {
            if (key != null) {
                String[] keyParts = key.split(ProtocolDialect.KEY_DELIMITER);
                if (keyParts.length == 2
                        && !Checks.is(keyParts[0]).emptyOrOnlyWhiteSpace()
                        && !Checks.is(keyParts[1]).emptyOrOnlyWhiteSpace()) {
                    Optional<DeviceProtocolPluggableClass> pluggableClassOptional = this.protocolPluggableService.findDeviceProtocolPluggableClass(Long.valueOf(keyParts[0]));
                    if (pluggableClassOptional.isPresent()) {
                        return pluggableClassOptional.get().getDeviceProtocol().getDeviceProtocolDialects()
                                .stream()
                                .filter(protocolDialect -> protocolDialect.getDeviceProtocolDialectName().equals(keyParts[1]))
                                .findFirst()
                                .map(protocolDialect -> new ProtocolDialect(pluggableClassOptional.get(), protocolDialect));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public Class<ProtocolDialect> valueDomain() {
            return ProtocolDialect.class;
        }
    }

    static final class ProtocolDialect extends HasIdAndName {
        static final String KEY_DELIMITER = ";";

        private final DeviceProtocolPluggableClass pluggableClass;
        private final DeviceProtocolDialect protocolDialect;

        private String id;

        public ProtocolDialect(DeviceProtocolPluggableClass pluggableClass, DeviceProtocolDialect protocolDialect) {
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
            return getProtocolDialect().getDisplayName();
        }

        public DeviceProtocolPluggableClass getPluggableClass() {
            return this.pluggableClass;
        }

        public DeviceProtocolDialect getProtocolDialect() {
            return this.protocolDialect;
        }
    }
}
