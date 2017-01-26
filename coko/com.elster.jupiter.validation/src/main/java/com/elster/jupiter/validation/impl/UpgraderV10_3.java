package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.Version.version;

@LiteralSql
public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private static final String METROLOGY_CONFIGURATION_PROPERTY = "metrologyConfigurations.metrologyConfiguration";
    private final DataModel dataModel;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringGroupsService meteringGroupsService;
    private final ValidationService validationService;
    private final SearchService searchService;

    @Inject
    public UpgraderV10_3(DataModel dataModel, MetrologyConfigurationService metrologyConfigurationService, MeteringGroupsService meteringGroupsService, ValidationService validationService, SearchService searchService) {
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringGroupsService = meteringGroupsService;
        this.validationService = validationService;
        this.searchService = searchService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);

        Map<MetrologyConfiguration, List<DataValidationTask>> validationTasks = new HashMap<>();

        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement preparedStatement = connection.prepareStatement("select VAL_DATAVALIDATIONTASK.ID, MTR_METROLOGY_CONTRACT.METROLOGY_CONFIG " +
                     " from VAL_DATAVALIDATIONTASK inner join MTR_METROLOGY_CONTRACT on VAL_DATAVALIDATIONTASK.METROLOGYCONTRACT=MTR_METROLOGY_CONTRACT.ID");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(resultSet
                        .getInt(2)).get();
                if (validationTasks.containsKey(metrologyConfiguration)) {
                    validationTasks.get(metrologyConfiguration)
                            .add(validationService.findValidationTask(resultSet.getInt(1)).get());
                } else {
                    validationTasks.put(metrologyConfiguration, new ArrayList<>(Collections.singletonList(validationService
                            .findValidationTask(resultSet.getInt(1))
                            .get())));
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        List<String> sql = new ArrayList<>();
        for (Map.Entry<MetrologyConfiguration, List<DataValidationTask>> entry : validationTasks.entrySet()) {
            String name = "All with " + entry.getKey().getName();
            SearchDomain usagePointSearchDomain = searchService.findDomain(UsagePoint.class.getName()).get();
            UsagePointGroup usagePointGroup = meteringGroupsService.createQueryUsagePointGroup(createSearchablePropertyValue(METROLOGY_CONFIGURATION_PROPERTY, Collections
                    .singletonList(String.valueOf(entry.getKey().getId()))))
                    .setName(name)
                    .setSearchDomain(usagePointSearchDomain)
                    .setQueryProviderName("com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider")
                    .setLabel("MDM")
                    .setMRID("MDM:" + name)
                    .create();
            for (DataValidationTask validationTask : entry.getValue()) {
                sql.add("UPDATE VAL_DATAVALIDATIONTASK SET USAGEPOINTGROUP = "
                        + usagePointGroup.getId()
                        + " WHERE ID = "
                        + validationTask.getId());
            }
        }
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private SearchablePropertyValue createSearchablePropertyValue(String searchableProperty, List<String> values) {
        return new SearchablePropertyValue(null, new SearchablePropertyValue.ValueBean(searchableProperty, SearchablePropertyOperator.EQUAL, values ));
    }
}
