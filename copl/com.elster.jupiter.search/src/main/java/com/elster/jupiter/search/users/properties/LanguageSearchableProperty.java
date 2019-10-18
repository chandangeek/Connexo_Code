package com.elster.jupiter.search.users.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.users.PropertyTranslationKeys;
import com.elster.jupiter.search.users.UserSearchDomain;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class LanguageSearchableProperty extends AbstractUserSearchableProperty {

    private static final String PROPERTY_NAME = "languageTag";

    public LanguageSearchableProperty(final UserService userService, final UserSearchDomain userSearchDomain, final PropertySpecService propertySpecService, final Thesaurus thesaurus) {
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
                .specForValuesOf(new LanguageFactory())
                .named(PROPERTY_NAME, PropertyTranslationKeys.USER_LANGUAGE)
                .fromThesaurus(thesaurus)
                .addValues(userService.getUserPreferencesService().getSupportedLocales())
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
        return PropertyTranslationKeys.USER_LANGUAGE.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(final Object value) {
        if (value instanceof Locale) {
            final Locale locale = (Locale) value;
            return locale.toLanguageTag();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public Condition toCondition(final Condition specification) {
        if (!(specification instanceof Contains)) {
            throw new IllegalArgumentException("Condition must be IN or NOT IN");
        }

        Contains contains = (Contains) specification;
        final List<String> locales = contains.getCollection().stream().map(this::fromLocale).collect(Collectors.toList());

        return ListOperator.IN.contains(PROPERTY_NAME, locales);
    }

    private String fromLocale(final Object object) {
        return ((Locale) object).toLanguageTag();
    }

    public static class LanguageFactory implements ValueFactory<Locale> {

        @Override
        public Locale fromStringValue(final String stringValue) {
            return Locale.forLanguageTag(stringValue);
        }

        @Override
        public String toStringValue(final Locale object) {
            return object.toLanguageTag();
        }

        @Override
        public Class<Locale> getValueType() {
            return Locale.class;
        }

        @Override
        public Locale valueFromDatabase(final Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(final Locale object) {
            return toStringValue(object);
        }

        @Override
        public void bind(final PreparedStatement statement, final int offset, final Locale value) throws SQLException {

        }

        @Override
        public void bind(final SqlBuilder builder, final Locale value) {

        }
    }

}
