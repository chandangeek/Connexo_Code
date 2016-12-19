package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.UsagePointStateTemporalImpl;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UsagePointFinder implements Finder<UsagePoint> {
    private DefaultFinder<UsagePoint> finder;
    private final ServerMeteringService meteringService;

    UsagePointFinder(ServerMeteringService meteringService, List<SearchablePropertyCondition> conditions) {
        this.meteringService = meteringService;
        this.finder = DefaultFinder
                .of(UsagePoint.class, toCondition(conditions), meteringService.getDataModel(),
                        EffectiveMetrologyConfigurationOnUsagePoint.class,
                        MetrologyConfiguration.class,
                        UsagePointDetail.class,
                        ServiceCategory.class,
                        UsagePointConnectionState.class,
                        MetrologyContract.class,
                        UsagePointStateTemporalImpl.class)
                .defaultSortColumn("name");
    }

    private Condition toCondition(List<SearchablePropertyCondition> conditions) {
        return conditions
                .stream()
                .reduce(
                        Condition.TRUE,
                        (underConstruction, condition) -> underConstruction.and(getConditionForSingleProperty(condition)),
                        Condition::and);
    }

    private Condition getConditionForSingleProperty(SearchablePropertyCondition condition) {
        return ((SearchableUsagePointProperty) condition.getProperty()).toCondition(condition.getCondition());
    }

    @Override
    public int count() {
        try (Connection connection = meteringService.getDataModel().getConnection(false)) {
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
    public Finder<UsagePoint> paged(int start, int pageSize) {
        finder = finder.paged(start, pageSize);
        return this;
    }

    @Override
    public Finder<UsagePoint> sorted(String sortColumn, boolean ascending) {
        finder = finder.sorted(sortColumn, ascending);
        return this;
    }

    @Override
    public List<UsagePoint> find() {
        QueryExecutor<UsagePoint> query = meteringService.getDataModel().query(
                UsagePoint.class, EffectiveMetrologyConfigurationOnUsagePoint.class, Location.class, LocationMember.class);
        Range<Integer> pageLimits = finder.getActualPageLimits();
        return Range.all().equals(pageLimits) ?
                query.select(ListOperator.IN.contains(this.finder.asSubQuery("id"), "id"), finder.getActualSortingColumns()) :
                query.select(ListOperator.IN.contains(this.finder.asSubQuery("id"), "id"), finder.getActualSortingColumns(),
                        true, new String[0], pageLimits.lowerEndpoint(), pageLimits.upperEndpoint());
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
