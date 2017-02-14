/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.validation.DataQualityOverviews;
import com.energyict.mdc.device.data.validation.DeviceDataQualityService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component(
        name = "com.energyict.mdc.device.data.quality",
        service = {DeviceDataQualityService.class},
        property = "name=" + DeviceDataQualityService.COMPONENT_NAME,
        immediate = true
)
public class DeviceDataQualityServiceImpl implements DeviceDataQualityService {

    private volatile DataModel validationDataModel;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;

    // For OSGi purposes
    public DeviceDataQualityServiceImpl() {
    }

    // For Testing purposes
    @Inject
    DeviceDataQualityServiceImpl(OrmService ormService, MeteringGroupsService meteringGroupsService) {
        this();
        this.setOrmService(ormService);
        this.setMeteringGroupsService(meteringGroupsService);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.validationDataModel = ormService.getDataModel(ValidationService.COMPONENTNAME).orElseThrow(IllegalStateException::new);
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public DataQualityOverviewBuilder forAllDevices() {
        return new DataQualityOverviewBuilderImpl();
    }

    private DataQualityOverviews queryWith(ValidationOverviewSpecificationImpl specification) {
        ValidationOverviewSqlBuilder sqlBuilder =
                new ValidationOverviewSqlBuilder(
                        specification.deviceGroups,
                        specification.range,
                        specification.kpiTypes,
                        specification.suspectsRange,
                        specification.from, specification.to);
        try (Connection connection = this.validationDataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            return this.execute(statement, specification);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private DataQualityOverviews execute(PreparedStatement statement, ValidationOverviewSpecificationImpl specification) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return this.fetch(resultSet, specification);
        }
    }

    private DataQualityOverviews fetch(ResultSet resultSet, ValidationOverviewSpecificationImpl specification) throws SQLException {
        DataQualityOverviewsImpl overviews = new DataQualityOverviewsImpl();
        while (resultSet.next()) {
            overviews.add(this.fetchOne(resultSet, specification));
        }
        return overviews;
    }

    private DataQualityOverviewImpl fetchOne(ResultSet resultSet, ValidationOverviewSpecificationImpl specification) throws SQLException {
        return DataQualityOverviewImpl.from(resultSet);
    }

    private EndDeviceGroup findGroup(long id) {
        return this.meteringGroupsService.findEndDeviceGroup(id).get();
    }
}