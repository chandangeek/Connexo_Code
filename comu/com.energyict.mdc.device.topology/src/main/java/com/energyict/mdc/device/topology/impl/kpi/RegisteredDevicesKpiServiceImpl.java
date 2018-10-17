/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiFrequency;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiScore;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class RegisteredDevicesKpiServiceImpl implements RegisteredDevicesKpiService {

    private DataModel dataModel;
    private TopologyService topologyService;
    private Clock clock;

    @Inject
    public RegisteredDevicesKpiServiceImpl(ServerTopologyService topologyService, Clock clock) {
        this.topologyService = topologyService;
        this.dataModel = topologyService.dataModel();
        this.clock = clock;
    }

    @Override
    public RegisteredDevicesKpiBuilder newRegisteredDevicesKpi(EndDeviceGroup endDeviceGroup) {
        return new RegisteredDevicesKpiBuilderImpl(endDeviceGroup);
    }

    @Override
    public List<RegisteredDevicesKpi> findAllRegisteredDevicesKpis() {
        return this.dataModel.mapper(RegisteredDevicesKpi.class).find();
    }

    @Override
    public Finder<RegisteredDevicesKpi> registeredDevicesKpiFinder() {
        return DefaultFinder.of(RegisteredDevicesKpi.class, this.dataModel, EndDeviceGroup.class).defaultSortColumn(RegisteredDevicesKpiImpl.Fields.END_DEVICE_GROUP.fieldName() + ".name");
    }

    @Override
    public Optional<RegisteredDevicesKpi> findRegisteredDevicesKpi(long id) {
        return this.dataModel.mapper(RegisteredDevicesKpi.class).getOptional(id);
    }

    @Override
    public Optional<RegisteredDevicesKpi> findRegisteredDevicesKpiByRecurrentTask(long id) {
        Condition condition = where("kpiTask.id").isEqualTo(id);
        return this.dataModel.query(RegisteredDevicesKpi.class, RecurrentTask.class).select(condition).stream().findFirst();

    }

    @Override
    public Optional<RegisteredDevicesKpi> findAndLockRegisteredDevicesKpiByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(RegisteredDevicesKpi.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<RegisteredDevicesKpi> findRegisteredDevicesKpi(EndDeviceGroup group) {
        return this.dataModel.mapper(RegisteredDevicesKpi.class).getUnique(RegisteredDevicesKpiImpl.Fields.END_DEVICE_GROUP
                .fieldName(), group);
    }

    @Override
    public List<RegisteredDevicesKpiScore> getScores(Device gateway, Range<Instant> interval, RegisteredDevicesKpiFrequency frequency) {
        ScheduleExpression expression = getTemporalExpression(frequency.getFrequency());
        Optional<ZonedDateTime> zonedDateTime = expression.nextOccurrence(ZonedDateTime.ofInstant(interval.lowerEndpoint().minus(frequency.getFrequency()), clock.getZone()));
        ZonedDateTime startTime = zonedDateTime.get();
        List<PhysicalGatewayReference> gatewayReferences = topologyService.getPhysicalGatewayReferencesFor(gateway, interval);
        Map<Instant, Integer> countPerInstant = getInstantsMap(interval, expression, startTime);
        gatewayReferences
                .forEach(physicalGatewayReference -> belongsToInstants(countPerInstant, physicalGatewayReference.getRange()));
        return countPerInstant.entrySet()
                .stream()
                .map(registered -> new RegisteredDevicesKpiScoreImpl(registered.getKey(), BigDecimal.valueOf(registered.getValue())))
                .collect(Collectors.toList());

    }

    private Map<Instant, Integer> getInstantsMap(Range<Instant> interval, ScheduleExpression expression, ZonedDateTime startTime) {
        Map<Instant, Integer> numberPerInstant = new HashMap<>();
        while (!startTime.toInstant().isAfter(interval.upperEndpoint())) {
            numberPerInstant.put(startTime.toInstant(), 0);
            startTime = expression.nextOccurrence(startTime).get();
        }
        return numberPerInstant;
    }

    private void belongsToInstants(Map<Instant, Integer> countPerInstant, Range<Instant> range) {
        countPerInstant.keySet().stream()
                .filter(range::contains)
                .forEach(instant -> {
                    int count = countPerInstant.get(instant);
                    countPerInstant.put(instant, count + 1);
                });
    }

    private ScheduleExpression getTemporalExpression(TemporalAmount frequency) {

        if (frequency instanceof Duration) {
            Duration duration = (Duration) frequency;
            return new TemporalExpression(new TimeDurationFactory.TimeDurationFromDurationFactory().from(duration));
        } else {
            Period period = (Period) frequency;
            return new TemporalExpression(new TimeDurationFactory.TimeDurationFromPeriodFactory().from(period));
        }
    }

    private class RegisteredDevicesKpiBuilderImpl implements RegisteredDevicesKpiBuilder {
        private RegisteredDevicesKpiImpl underConstruction;

        public RegisteredDevicesKpiBuilderImpl(EndDeviceGroup deviceGroup) {
            underConstruction = dataModel.getInstance(RegisteredDevicesKpiImpl.class).initialize(deviceGroup);
        }

        @Override
        public RegisteredDevicesKpiBuilder frequency(TemporalAmount temporalAmount) {
            underConstruction.setFrequency(temporalAmount);
            return this;
        }

        @Override
        public RegisteredDevicesKpiBuilder target(long target) {
            underConstruction.setTarget(target);
            return this;
        }

        @Override
        public RegisteredDevicesKpiBuilder setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks) {
            underConstruction.setNextRecurrentTasks(nextRecurrentTasks);
            return this;
        }

        @Override
        public RegisteredDevicesKpi save() {
            underConstruction.save();
            return underConstruction;
        }
    }
}
