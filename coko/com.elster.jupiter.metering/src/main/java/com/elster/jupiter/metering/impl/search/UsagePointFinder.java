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
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsagePointFinder implements Finder<UsagePoint> {
    private Finder<UsagePoint> finder;
    private List<Order> orders = new ArrayList<>();
    private final ServerMeteringService meteringService;

    UsagePointFinder(ServerMeteringService meteringService, List<SearchablePropertyCondition> conditions) {
        this.meteringService = meteringService;
        this.finder = DefaultFinder
                .of(UsagePoint.class, toCondition(conditions), meteringService.getDataModel(),
                            EffectiveMetrologyConfigurationOnUsagePoint.class, MetrologyConfiguration.class, UsagePointDetail.class, ServiceCategory.class, UsagePointConnectionState.class)
                .defaultSortColumn("mRID");
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
        this.finder = this.finder.paged(start, pageSize);
        return this;
    }

    @Override
    public Finder<UsagePoint> sorted(String sortColumn, boolean ascending) {
        this.finder = this.finder.sorted(sortColumn, ascending);
        orders.add(ascending ? Order.ascending(sortColumn) : Order.descending(sortColumn));
        return this;
    }

    @Override
    public List<UsagePoint> find() {
        QueryExecutor<UsagePoint> query = meteringService.getDataModel().query(UsagePoint.class, EffectiveMetrologyConfigurationOnUsagePoint.class, Location.class, LocationMember.class);
        return query.select(ListOperator.IN.contains(this.finder.asSubQuery("id"), "id"), orders.toArray(new Order[orders.size()]));

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
