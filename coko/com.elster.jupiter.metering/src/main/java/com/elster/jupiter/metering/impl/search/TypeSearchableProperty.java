package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Where;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TypeSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELDNAME = "type";

    public TypeSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public SearchableProperty.Visibility getVisibility() {
        return SearchableProperty.Visibility.REMOVABLE;
    }

    @Override
    public SearchableProperty.SelectionMode getSelectionMode() {
        return SearchableProperty.SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_TYPE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof UsagePointTypes) {
            return ((UsagePointTypes) value).getDisplayName(thesaurus);
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return propertySpecService
                .specForValuesOf(new TypeValueFactory())
                .named(FIELDNAME, PropertyTranslationKeys.USAGEPOINT_TYPE)
                .fromThesaurus(this.thesaurus)
                .addValues(UsagePointTypes.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("No constraint to refresh");
        }
    }

    @Override
    public Condition toCondition(Condition specification) {
        return (((Contains) specification).getCollection()).stream()
                .map(UsagePointTypes.class::cast).map(result -> {
                    switch (result) {
                        case MEASURED_SDP:
                            return Where.where("isVirtual").isEqualTo(true)
                                    .and(Where.where("isSdp").isEqualTo(true));
                        case UNMEASURED_SDP:
                            return Where.where("isVirtual").isEqualTo(false)
                                    .and(Where.where("isSdp").isEqualTo(true));
                        case MEASURED_NON_SDP:
                            return Where.where("isVirtual").isEqualTo(true)
                                    .and(Where.where("isSdp").isEqualTo(false));
                        case UNMEASURED_NON_SDP:
                            return Where.where("isVirtual").isEqualTo(false)
                                    .and(Where.where("isSdp").isEqualTo(false));
                        default:
                            throw new IllegalArgumentException("");
                    }
                }).findAny().orElseThrow(IllegalArgumentException::new);
    }

    public static TranslationKey[] getTranslationKeys() {
        return UsagePointTypes.values();
    }

    public enum UsagePointTypes implements TranslationKey {
        MEASURED_SDP("usagePoint.type.measuredSDP", "Measured SDP"),
        UNMEASURED_SDP("usagePoint.type.unmeasuredSDP", "Unmeasured SDP"),
        MEASURED_NON_SDP("usagePoint.type.measuredNonSDP", "Measured non-SDP"),
        UNMEASURED_NON_SDP("usagePoint.type.unmeasuredNonSDP", "Unmeasured non-SDP");

        private String key, value;

        private UsagePointTypes(String translationKey, String defaultValue) {
            key = translationKey;
            value = defaultValue;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return value;
        }

        String getDisplayName(Thesaurus thesaurus) {
            return thesaurus.getString(key, value);
        }
    }

    private static class TypeValueFactory extends AbstractValueFactory<Enum> {

        @Override
        protected int getJdbcType() {
            return Types.VARCHAR;
        }

        @Override
        public Enum fromStringValue(String stringValue) {
            return Arrays.stream(UsagePointTypes.values())
                    .filter(enumValue -> stringValue.equalsIgnoreCase(enumValue.name()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }

        @Override
        public String toStringValue(Enum object) {
            return String.valueOf(object);
        }

        @Override
        public Class<Enum> getValueType() {
            return Enum.class;
        }

        @Override
        public Enum valueFromDatabase(Object object) {
            return (Enum) object;
        }

        @Override
        public Object valueToDatabase(Enum object) {
            return object;
        }
    }
}
