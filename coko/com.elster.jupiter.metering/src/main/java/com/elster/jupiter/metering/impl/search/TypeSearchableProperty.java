package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;

import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
        return SearchableProperty.SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_TYPE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (!this.valueCompatibleForDisplay(value)) {
            throw new IllegalArgumentException("Value not compatible with domain");
        }
        return this.toDisplayAfterValidation(value);
    }

    private String toDisplayAfterValidation(Object value){
        UsagePointTypes usagePointTypes = (UsagePointTypes) value;
        return usagePointTypes.getValue();
    }

    private boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Enum;
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
       return null;
//               Arrays.asList(((Comparison) specification).getValues()).stream()
//                .findAny(result-> {
//                    if(((UsagePointTypes)result).getValue().equalsIgnoreCase("Measured SDP")){
//                        return Where.where("isVirtual")
//                                .isEqualTo(true).and(Where.where("isSdp").isEqualTo(true));
//                    }
//                    if(((UsagePointTypes)result).getValue().equalsIgnoreCase("Measured non-SDP")){
//                        return Where.where("isVirtual").isEqualTo(true).and(Where.where("isSdp").isEqualTo(false));
//                    }
//                    if(((UsagePointTypes)result).getValue().equalsIgnoreCase("Unmeasured SDP")){
//                        return Where.where("isVirtual").isEqualTo(false).and(Where.where("isSdp").isEqualTo(true));
//                    }
//                    else
//                    //if((((UsagePointTypes)result).getValue().equalsIgnoreCase("Unmeasured non-SDP"))){
//                        return Where.where("isVirtual").isEqualTo(false).and(Where.where("isSdp").isEqualTo(false));
//                });
    }

    private enum UsagePointTypes {
        MEASURED_SDP {
            @Override
            public String getValue() {
                return "Measured SDP";
            }
        },
        UNMEASURED_SDP {
            @Override
            public String getValue() {
                return "Unmeasured SDP";
            }
        },
        MEASURED_NON_SDP {
            @Override
            public String getValue() {
                return "Unmeasured non-SDP";
            }
        },
        UNMEASURED_NON_SDP {
            @Override
            public String getValue() {
                return "Unmeasured non-SDP";
            }
        };
        public abstract String getValue();
    }

    static class TypeValueFactory extends AbstractValueFactory<Enum> {

        @Override
        protected int getJdbcType() {
            return Types.VARCHAR;
        }

        @Override
        public Enum fromStringValue(String stringValue) {
            return Stream.of(UsagePointTypes.values())
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
