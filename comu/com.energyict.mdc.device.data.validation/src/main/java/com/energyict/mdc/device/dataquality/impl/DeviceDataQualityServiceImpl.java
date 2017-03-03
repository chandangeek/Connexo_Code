/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component(
        name = "com.energyict.mdc.device.dataquality",
        service = {DeviceDataQualityService.class},
        property = "name=" + DeviceDataQualityService.COMPONENT_NAME,
        immediate = true
)
public class DeviceDataQualityServiceImpl implements DeviceDataQualityService {

    private volatile OrmService ormService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile DataModel dataModel;

    // For OSGi purposes
    public DeviceDataQualityServiceImpl() {
    }

    // For Testing purposes
    @Inject
    DeviceDataQualityServiceImpl(OrmService ormService, ValidationService validationService, EstimationService estimationService) {
        this();
        this.setOrmService(ormService);
        this.setValidationService(validationService);
        this.setEstimationService(estimationService);
        this.activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
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
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        // just a dependency
    }

    @Activate
    public void activate() {
        this.dataModel = ormService.getDataModel(DataQualityKpiService.COMPONENTNAME).orElseThrow(IllegalStateException::new);
    }

    @Override
    public DataQualityOverviewBuilder forAllDevices() {
        return new DataQualityOverviewBuilderImpl(this, this.validationService, this.estimationService);
    }

    DataQualityOverviews queryWith(DataQualityOverviewSpecificationImpl specification) {
        DataQualityOverviewSqlBuilder sqlBuilder = new DataQualityOverviewSqlBuilder(specification);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            return this.execute(statement, specification);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private DataQualityOverviews execute(PreparedStatement statement, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return this.fetch(resultSet, specification);
        }
    }

    private DataQualityOverviews fetch(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        DataQualityOverviewsImpl overviews = new DataQualityOverviewsImpl();
        while (resultSet.next()) {
            overviews.add(this.fetchOne(resultSet, specification));
        }
        return overviews;
    }

    private DataQualityOverviewImpl fetchOne(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        return DataQualityOverviewImpl.from(resultSet, specification);
    }
}