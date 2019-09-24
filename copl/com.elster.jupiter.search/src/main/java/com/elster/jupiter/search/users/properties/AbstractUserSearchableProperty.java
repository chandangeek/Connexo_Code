package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.users.SearchableUserProperty;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;

public abstract class AbstractUserSearchableProperty implements SearchableUserProperty {

    protected final UserService userService;
    protected final UserSearchDomain userSearchDomain;
    protected final PropertySpecService propertySpecService;
    protected final Thesaurus thesaurus;

    public AbstractUserSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
        this.userService = userService;
        this.userSearchDomain = userSearchDomain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public SearchDomain getDomain() {
        return userSearchDomain;
    }

    @Override
    public String toDisplay(Object value) {
        if (!this.valueCompatibleForDisplay(value)) {
            throw new IllegalArgumentException("Value not compatible with domain");
        }
        return String.valueOf(value);
    }

    private boolean valueCompatibleForDisplay(Object value) {
        return value instanceof String;
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
        return specification;
    }

}
