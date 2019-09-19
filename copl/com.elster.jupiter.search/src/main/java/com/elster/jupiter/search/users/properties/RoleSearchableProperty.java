package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserInGroup;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleSearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "memberships";

    public RoleSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
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
                .specForValuesOf(new UserGroupFactory())
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_ROLES)
                .fromThesaurus(thesaurus)
                .addValues(userService.getGroups())
                .markExhaustive()
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
        return PropertyTranslationKeys.USER_ROLES.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(final Object value) {
        if (value instanceof Group) {
            final Group group = (Group) value;
            return group.getName();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public Condition toCondition(final Condition specification) {
        if (!(specification instanceof Contains)) {
            throw new IllegalArgumentException("Condition must be IN or NOT IN");
        }

        Contains contains = (Contains) specification;

        final Subquery subquery = userService.getDataModel()
                .query(UserInGroup.class, Group.class)
                .asSubquery(Where.where("group").in((List<?>) contains.getCollection()), "userid");

        return ListOperator.IN.contains(subquery, "id");
    }

    private class UserGroupFactory implements ValueFactory<Group> {

        @Override
        public Group fromStringValue(final String stringValue) {
            return userService.getGroup(stringValue).orElse(null);
        }

        @Override
        public String toStringValue(final Group object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<Group> getValueType() {
            return Group.class;
        }

        @Override
        public Group valueFromDatabase(final Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(final Group object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(final PreparedStatement statement, final int offset, final Group value) throws SQLException {

        }

        @Override
        public void bind(final SqlBuilder builder, final Group value) {

        }
    }
}
