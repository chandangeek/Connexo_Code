/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;

import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StandardDataSelectorInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final OrmService ormService;

    @Inject
    public StandardDataSelectorInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, OrmService ormService) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.ormService = ormService;
    }

    public StandardDataSelectorInfo asInfo(MeterReadingSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.deviceGroup = getEndDeviceGroup(selector);
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
        populateFromExportStrategy(selector.getStrategy(), info);
        return info;
    }

    public StandardDataSelectorInfo asInfo(UsagePointReadingSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.usagePointGroup = new IdWithNameInfo(selector.getUsagePointGroup().getId(), selector.getUsagePointGroup()
                .getName());
        selector.getMetrologyPurpose()
                .ifPresent(purpose -> info.purpose = new LongIdWithNameInfo(purpose.getId(), purpose.getName()));
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.readingTypes = selector.getReadingTypes()
                .stream()
                .map(readingTypeInfoFactory::from)
                .collect(Collectors.toCollection(ArrayList::new));
        populateFromExportStrategy(selector.getStrategy(), info);
        return info;
    }

    private void populateFromExportStrategy(DataExportStrategy strategy, StandardDataSelectorInfo info) {
        info.exportContinuousData = strategy.isExportContinuousData();
        info.exportComplete = strategy.getMissingDataOption();
        info.exportUpdate = strategy.isExportUpdate();
        strategy.getUpdatePeriod()
                .ifPresent(relativePeriod -> info.updatePeriod = RelativePeriodInfo.withCategories(relativePeriod));
        strategy.getUpdateWindow()
                .ifPresent(relativePeriod -> info.updateWindow = RelativePeriodInfo.withCategories(relativePeriod));
        info.validatedDataOption = strategy.getValidatedDataOption();
    }

    public StandardDataSelectorInfo asInfo(EventSelectorConfig selector) {
        StandardDataSelectorInfo info = new StandardDataSelectorInfo();
        info.id = selector.getId();
        info.exportComplete = MissingDataOption.NOT_APPLICABLE;
        info.deviceGroup = new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
        info.exportPeriod = RelativePeriodInfo.withCategories(selector.getExportPeriod());
        info.exportContinuousData = selector.getStrategy().isExportContinuousData();
        info.eventTypeCodes = selector.getEventTypeFilters()
                .stream()
                .map(EndDeviceEventTypeFilter::getCode)
                .map(EventTypeInfo::of)
                .collect(Collectors.toList());
        return info;
    }

    private IdWithNameInfo getEndDeviceGroup(MeterReadingSelectorConfig selector) {
        if (selector.getEndDeviceGroup() != null) {
            return new IdWithNameInfo(selector.getEndDeviceGroup().getId(), selector.getEndDeviceGroup().getName());
        } else {

            long endDeviceGroupId = selector.getEndDeviceGroupId();
            String endDeviceGroupName = "";

            SqlBuilder sqlBuilder = new SqlBuilder(
                    "SELECT * FROM (SELECT * FROM MTG_ED_GROUP_JRNL WHERE ID=");
            sqlBuilder.addLong(endDeviceGroupId);
            sqlBuilder.append(" ORDER BY MODTIME DESC) WHERE ROWNUM = 1");

            try (Connection connection = this.ormService.getDataModel("MTG").get().getConnection(true)) {
                try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            endDeviceGroupName = resultSet.getString(2);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }

            return new IdWithNameInfo(endDeviceGroupId, endDeviceGroupName);
        }
    }
}



