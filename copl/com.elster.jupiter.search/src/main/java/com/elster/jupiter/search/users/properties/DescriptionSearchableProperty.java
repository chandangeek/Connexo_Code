package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.search.users.UserSearchDomain;

import java.util.Optional;

public class DescriptionSearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "description";

    public DescriptionSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
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
                .stringSpec()
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_DESCRIPTION)
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
        return PropertyTranslationKeys.USER_DESCRIPTION.getDisplayName(thesaurus);
    }
}
