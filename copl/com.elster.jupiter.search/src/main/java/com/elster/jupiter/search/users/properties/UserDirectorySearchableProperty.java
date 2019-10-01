package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class UserDirectorySearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "userDirectory";

    public UserDirectorySearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
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
                .specForValuesOf(new UserDirectoriesFactory())
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_DIRECTORY)
                .fromThesaurus(thesaurus)
                .addValues(userService.getUserDirectories())
                .markExhaustive()
                .finish();
    }

    @Override
    public String toDisplay(final Object value) {
        if (value instanceof UserDirectory) {
            final UserDirectory userDirectory = (UserDirectory) value;
            return userDirectory.getDomain();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
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
        return PropertyTranslationKeys.USER_DIRECTORY.getDisplayName(thesaurus);
    }

    private class UserDirectoriesFactory implements ValueFactory<UserDirectory> {

        @Override
        public UserDirectory fromStringValue(final String stringValue) {
            return userService.findUserDirectory(stringValue).orElse(userService.findDefaultUserDirectory());
        }

        @Override
        public String toStringValue(final UserDirectory object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<UserDirectory> getValueType() {
            return UserDirectory.class;
        }

        @Override
        public UserDirectory valueFromDatabase(final Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(final UserDirectory object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(final PreparedStatement statement, final int offset, final UserDirectory value) throws SQLException {

        }

        @Override
        public void bind(final SqlBuilder builder, final UserDirectory value) {

        }
    }
}
