/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.energyict.mdc.device.data.validation.ValidationOverviews;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceDataValidationService} interface.
 *
 * @author Dragos
 * @since 2015-07-21 (14:54)
 */
@Component(name = "com.energyict.mdc.device.data.validation", service = {DeviceDataValidationService.class}, property = "name=" + DeviceDataValidationService.COMPONENT_NAME, immediate = true)
public class DeviceDataValidationServiceImpl implements DeviceDataValidationService {

    private volatile DataModel validationDataModel;
    private volatile MeteringGroupsService meteringGroupsService;

    // For OSGi purposes
    public DeviceDataValidationServiceImpl() {
    }

    // For Testing purposes
    @Inject
    DeviceDataValidationServiceImpl(OrmService ormService, MeteringGroupsService meteringGroupsService) {
        this();
        this.setOrmService(ormService);
        this.setMeteringGroupsService(meteringGroupsService);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        // dependency order only
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
    public ValidationOverviewBuilder forAllGroups(List<EndDeviceGroup> deviceGroups) {
        return new ValidationOverviewBuilderImpl(deviceGroups);
    }

    private ValidationOverviews queryWith(ValidationOverviewSpecificationImpl specification) {
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

    private ValidationOverviews execute(PreparedStatement statement, ValidationOverviewSpecificationImpl specification) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return this.fetch(resultSet, specification);
        }
    }

    private ValidationOverviews fetch(ResultSet resultSet, ValidationOverviewSpecificationImpl specification) throws SQLException {
        ValidationOverviewsImpl overviews = new ValidationOverviewsImpl();
        while (resultSet.next()) {
            overviews.add(this.fetchOne(resultSet, specification));
        }
        return overviews;
    }

    private ValidationOverviewImpl fetchOne(ResultSet resultSet, ValidationOverviewSpecificationImpl specification) throws SQLException {
        return ValidationOverviewImpl.from(resultSet, specification);
    }

    private EndDeviceGroup findGroup(long id) {
        return this.meteringGroupsService.findEndDeviceGroup(id).get();
    }

    private class ValidationOverviewBuilderImpl implements ValidationOverviewBuilder {
        private final ValidationOverviewSpecificationImpl underConstruction;

        ValidationOverviewBuilderImpl(List<EndDeviceGroup> deviceGroups) {
            super();
            this.underConstruction = new ValidationOverviewSpecificationImpl(deviceGroups);
        }

        @Override
        public ValidationOverviewBuilder in(Range<Instant> range) {
            this.underConstruction.setRange(range);
            return this;
        }

        @Override
        public ValidationOverviewSuspectsSpecificationBuilder suspects() {
            return new ValidationOverviewSuspectsSpecificationBuilderImpl(this);
        }

        @Override
        public ValidationOverviewBuilder includeThresholdValidator() {
            this.underConstruction.includeThresholdValidator();
            return this;
        }

        @Override
        public ValidationOverviewBuilder includeMissingValuesValidator() {
            this.underConstruction.includeMissingValuesValidator();
            return this;
        }

        @Override
        public ValidationOverviewBuilder includeReadingQualitiesValidator() {
            this.underConstruction.includeReadingQualitiesValidator();
            return this;
        }

        @Override
        public ValidationOverviewBuilder includeRegisterIncreaseValidator() {
            this.underConstruction.includeRegisterIncreaseValidator();
            return this;
        }

        @Override
        public ValidationOverviewBuilder excludeAllValidators() {
            this.underConstruction.excludeAllValidators();
            return this;
        }

        @Override
        public ValidationOverviewBuilder includeAllValidators() {
            this.underConstruction.includeAllValidators();
            return this;
        }

        @Override
        public ValidationOverviews paged(int from, int to) {
            return this.underConstruction.paged(from, to);
        }

    }

    private class ValidationOverviewSuspectsSpecificationBuilderImpl implements ValidationOverviewSuspectsSpecificationBuilder {
        private final ValidationOverviewBuilderImpl continuation;

        ValidationOverviewSuspectsSpecificationBuilderImpl(ValidationOverviewBuilderImpl continuation) {
            this.continuation = continuation;
        }

        @Override
        public ValidationOverviewBuilder equalTo(long numberOfSuspects) {
            this.continuation.underConstruction.setNumberOfSuspects(numberOfSuspects);
            return this.continuation;
        }

        @Override
        public ValidationOverviewBuilder inRange(Range<Long> range) {
            this.continuation.underConstruction.setSuspectRange(range);
            return this.continuation;
        }
    }

    interface SuspectsRange {
        void appendHavingTo(SqlBuilder sqlBuilder, String expression);
    }

    private class IgnoreSuspectRange implements SuspectsRange {
        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" > 0");
        }
    }

    private class ExactMatch implements SuspectsRange {
        private final long match;

        private ExactMatch(long match) {
            this.match = match;
        }

        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" =");
            sqlBuilder.addLong(this.match);
        }
    }

    private class LongRange implements SuspectsRange {
        private final Range<Long> range;

        private LongRange(Range<Long> range) {
            this.range = range;
        }

        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" >");
            if (this.range.hasLowerBound() && this.range.lowerBoundType() == BoundType.CLOSED) {
                sqlBuilder.append("=");
            }
            sqlBuilder.addLong(this.range.hasLowerBound() ? this.range.lowerEndpoint() : Integer.MIN_VALUE);
            sqlBuilder.append("AND ");
            sqlBuilder.append(expression);
            sqlBuilder.append(" <");
            if (this.range.hasUpperBound() && this.range.upperBoundType() == BoundType.CLOSED) {
                sqlBuilder.append("=");
            }
            sqlBuilder.addLong(this.range.hasUpperBound() ? this.range.upperEndpoint() : Integer.MAX_VALUE);
        }
    }

    class ValidationOverviewSpecificationImpl {
        private final List<EndDeviceGroup> deviceGroups;
        private Set<ValidationOverviewSqlBuilder.KpiType> kpiTypes;
        private Range<Instant> range = Range.all();
        private SuspectsRange suspectsRange = new IgnoreSuspectRange();
        private int from;
        private int to;

        private ValidationOverviewSpecificationImpl(List<EndDeviceGroup> deviceGroups) {
            this.deviceGroups = deviceGroups;
            this.excludeAllValidators();
        }

        void setRange(Range<Instant> range) {
            this.range = range;
        }

        void includeThresholdValidator() {
            this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.THRESHOLD);
        }

        void includeMissingValuesValidator() {
            this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.MISSING_VALUES);
        }

        void includeReadingQualitiesValidator() {
            this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.READING_QUALITIES);
        }

        void includeRegisterIncreaseValidator() {
            this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.REGISTER_INCREASE);
        }

        void excludeAllValidators() {
            this.kpiTypes = EnumSet.noneOf(ValidationOverviewSqlBuilder.KpiType.class);
        }

        void includeAllValidators() {
            this.kpiTypes =
                    EnumSet.of(
                            ValidationOverviewSqlBuilder.KpiType.THRESHOLD,
                            ValidationOverviewSqlBuilder.KpiType.MISSING_VALUES,
                            ValidationOverviewSqlBuilder.KpiType.READING_QUALITIES,
                            ValidationOverviewSqlBuilder.KpiType.REGISTER_INCREASE);
        }

        void setNumberOfSuspects(long numberOfSuspects) {
            this.suspectsRange = new ExactMatch(numberOfSuspects);
        }

        void setSuspectRange(Range<Long> range) {
            this.suspectsRange = new LongRange(range);
        }

        ValidationOverviews paged(int from, int to) {
            this.from = from;
            this.to = to;
            return queryWith(this);
        }
    }

}