package com.elster.jupiter.search.users;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.users.properties.CreationDateSearchableProperty;
import com.elster.jupiter.search.users.properties.DescriptionSearchableProperty;
import com.elster.jupiter.search.users.properties.LanguageSearchableProperty;
import com.elster.jupiter.search.users.properties.LastSuccessfulLoginSearchableProperty;
import com.elster.jupiter.search.users.properties.LastUnsuccessfulLoginSearchableProperty;
import com.elster.jupiter.search.users.properties.ModificationDateSearchableProperty;
import com.elster.jupiter.search.users.properties.NameSearchableProperty;
import com.elster.jupiter.search.users.properties.RoleSearchableProperty;
import com.elster.jupiter.search.users.properties.StatusSearchableProperty;
import com.elster.jupiter.search.users.properties.UserDirectorySearchableProperty;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserInGroup;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.users.search.user", service = SearchDomain.class, immediate = true)
public class UserSearchDomain implements SearchDomain {

    private volatile PropertySpecService propertySpecService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;

    public UserSearchDomain() {
        super();
    }

    @Inject
    public UserSearchDomain(final PropertySpecService propertySpecService, final NlsService nlsService, final UserService userService) {
        super();
        setPropertySpecService(propertySpecService);
        setNlsService(nlsService);
        setUserService(userService);
    }

    @Reference
    public void setPropertySpecService(final PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UserService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getId() {
        return User.class.getName();
    }

    @Override
    public String displayName() {
        return thesaurus.getFormat(PropertyTranslationKeys.USER_DOMAIN).format();
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("COKO");
    }

    @Override
    public Class<?> getDomainClass() {
        return User.class;
    }

    @Override
    public List<SearchableProperty> getProperties() {
        ImmutableList.Builder<SearchableProperty> properties = ImmutableList.builder();

        properties.add(new NameSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new DescriptionSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new UserDirectorySearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new StatusSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new RoleSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new LanguageSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new CreationDateSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new ModificationDateSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new LastSuccessfulLoginSearchableProperty(userService, this, propertySpecService, thesaurus));
        properties.add(new LastUnsuccessfulLoginSearchableProperty(userService, this, propertySpecService, thesaurus));

        return properties.build();
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(final List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("Expecting no constrictions");
        } else {
            return this.getProperties();
        }
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(final Function<SearchableProperty, SearchablePropertyValue> mapper) {
        return getProperties()
                .stream()
                .map(mapper)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().isValid())
                .collect(Collectors.toList());
    }

    @Override
    public Finder<?> finderFor(final List<SearchablePropertyCondition> conditions) {
        return new UserFinder(this.toCondition(conditions));
    }

    private Condition toCondition(List<SearchablePropertyCondition> conditions) {
        return conditions
                .stream()
                .map(ConditionBuilder::new)
                .reduce(
                        Condition.TRUE,
                        (underConstruction, builder) -> underConstruction.and(builder.build()),
                        Condition::and);
    }

    private final class ConditionBuilder {

        private final SearchablePropertyCondition spec;
        private final SearchableUserProperty property;

        private ConditionBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableUserProperty) spec.getProperty();
        }

        private Condition build() {
            return this.property.toCondition(this.spec.getCondition());
        }

    }

    private final class UserFinder implements Finder<User> {

        private final Finder<User> finder;

        private UserFinder(Condition condition) {
            this.finder = DefaultFinder
                    .of(User.class, condition, userService.getDataModel(), UserInGroup.class)
                    .defaultSortColumn("authenticationName");
        }

        @Override
        public int count() {
            try (Connection connection = userService.getDataModel().getConnection(false)) {
                SqlBuilder countSqlBuilder = new SqlBuilder();
                countSqlBuilder.add(asFragment("count(*)"));
                try (PreparedStatement statement = countSqlBuilder.prepare(connection)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        resultSet.next();
                        return resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public Finder<User> paged(int start, int pageSize) {
            return this.finder.paged(start, pageSize);
        }

        @Override
        public Finder<User> sorted(String sortColumn, boolean ascending) {
            return this.finder.sorted(sortColumn, ascending);
        }

        @Override
        public Finder<User> sorted(Order order) {
            return finder.sorted(order);
        }

        @Override
        public List<User> find() {
            return this.finder.find();
        }

        @Override
        public Subquery asSubQuery(String... fieldNames) {
            return this.finder.asSubQuery(fieldNames);
        }

        @Override
        public SqlFragment asFragment(String... fieldNames) {
            return this.finder.asFragment(fieldNames);
        }
    }
}
