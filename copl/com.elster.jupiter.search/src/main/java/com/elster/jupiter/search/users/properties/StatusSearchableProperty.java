package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Where;

import java.util.ArrayList;
import java.util.Optional;

public class StatusSearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "status";

    public StatusSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
        super(userService, userSearchDomain, propertySpecService, thesaurus);
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
    public PropertySpec getSpecification() {
        return propertySpecService
                .specForValuesOf(new BooleanFactory())
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_STATUS)
                .fromThesaurus(thesaurus)
                .finish();
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
        return PropertyTranslationKeys.USER_STATUS.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(final Object value) {
        if (value != null) {
            if ((Boolean) value) {
                return "Active";
            } else {
                return "Inactive";
            }
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public Condition toCondition(final Condition specification) {
        return Where.where(PROPERTY_NAME).isEqualTo(parseSpecification(specification));
    }

    private Boolean parseSpecification(final Condition specification) {
        return (Boolean) ((ArrayList) ((Contains) specification).getCollection()).get(0);
    }

}
