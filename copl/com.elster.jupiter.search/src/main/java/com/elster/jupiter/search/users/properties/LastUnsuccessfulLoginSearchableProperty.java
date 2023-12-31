package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.users.UserService;

import java.time.Instant;
import java.util.Optional;

public class LastUnsuccessfulLoginSearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "lastUnSuccessfulLogin";

    public LastUnsuccessfulLoginSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
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
                .specForValuesOf(new InstantFactory())
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_LAST_UNSUCCESSFUL_LOGIN)
                .fromThesaurus(thesaurus)
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USER_LAST_UNSUCCESSFUL_LOGIN.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof Instant) {
            return value.toString();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }
}
