/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiMemberType;
import com.elster.jupiter.dataquality.impl.calc.KpiType;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.KpiUpdater;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.dataquality.impl.DataQualityKpiMember.KPIMEMBERNAME_SEPARATOR;
import static com.elster.jupiter.util.streams.Predicates.not;

@UniqueEndDeviceGroup(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_GROUP_MUST_BE_UNIQUE + "}")
public final class DeviceDataQualityKpiImpl extends DataQualityKpiImpl implements DeviceDataQualityKpi {

    public enum Fields {

        ENDDEVICE_GROUP("deviceGroup");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();

    @Inject
    public DeviceDataQualityKpiImpl(DataModel dataModel, MeteringService meteringService, ValidationService validationService,
                                    EstimationService estimationService, MessageService messageService, TaskService taskService,
                                    KpiService kpiService, Clock clock) {
        super(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
    }

    DeviceDataQualityKpiImpl init(EndDeviceGroup deviceGroup, TemporalAmount calculationFrequency) {
        this.deviceGroup.set(deviceGroup);
        super.setFrequency(calculationFrequency);
        return this;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return this.deviceGroup.orNull();
    }

    @Override
    KpiType getKpiType() {
        return KpiType.DEVICE_DATA_QUALITY_KPI;
    }

    @Override
    String getRecurrentTaskName() {
        return getKpiType().recurrentTaskName(getDeviceGroup().getName());
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDC;
    }

    @Override
    public Map<Long, DataQualityKpiMember> updateMembers(Range<Instant> interval) {
        if (getKpiMembers().isEmpty()) {
            return createDataQualityKpiMembers(getDeviceGroup());
        }
        Set<Long> deviceGroupDeviceIds = deviceIdsInGroup();
        Map<Long, DataQualityKpiMember> dataQualityKpiMembersMap = deviceIdsInKpiMembers();
        Set<Long> commonElements = intersection(deviceGroupDeviceIds, dataQualityKpiMembersMap.keySet());

        // create kpis for new devices in group
        deviceGroupDeviceIds.stream()
                .filter(not(commonElements::contains))
                .forEach(this::createDataQualityKpiMember);

        // update existing kpis with the new kpi members (in case new validators/estimators deployed)
        dataQualityKpiMembersMap.entrySet().stream()
                .filter(entry -> commonElements.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(this::updateKpiMemberIfNeeded);

        // remove kpis for devices that disappeared from group
        Set<DataQualityKpiMember> obsoleteKpiMembers = dataQualityKpiMembersMap.entrySet().stream()
                .filter(not(entry -> commonElements.contains(entry.getKey())))
                .map(Map.Entry::getValue)
                .peek(DataQualityKpiMember::remove)
                .collect(Collectors.toSet());
        getKpiMembers().removeAll(obsoleteKpiMembers);

        return deviceIdsInKpiMembers();
    }

    private Map<Long, DataQualityKpiMember> createDataQualityKpiMembers(EndDeviceGroup endDeviceGroup) {
        return endDeviceGroup.getMembers(getClock().instant()).stream()
                .map(Meter.class::cast)
                .collect(Collectors.toMap(
                        Meter::getId,
                        meter -> createDataQualityKpiMember(kpiMemberNameSuffix(meter), meter.getZoneId())));
    }

    private DataQualityKpiMemberImpl createDataQualityKpiMember(long endDeviceId) {
        Meter meter = getMeteringService().findMeterById(endDeviceId).get();
        return createDataQualityKpiMember(kpiMemberNameSuffix(meter), meter.getZoneId());
    }

    private DataQualityKpiMemberImpl createDataQualityKpiMember(String kpiMemberNameSuffix, ZoneId zoneId) {
        KpiBuilder kpiBuilder = getKpiService().newKpi();
        kpiBuilder.interval(getFrequency());
        kpiBuilder.timeZone(zoneId);

        actualKpiMemberTypes()
                .map(DataQualityKpiMemberType::getName)
                .map(member -> member + KPIMEMBERNAME_SEPARATOR + kpiMemberNameSuffix)
                .forEach(member -> kpiBuilder.member().named(member).add());

        DataQualityKpiMemberImpl dataQualityKpiMember = DataQualityKpiMemberImpl.from(getDataModel(), this, kpiBuilder.create());
        getKpiMembers().add(dataQualityKpiMember);
        return dataQualityKpiMember;
    }

    private Set<Long> deviceIdsInGroup() {
        return getDeviceGroup().getMembers(getClock().instant()).stream()
                .map(EndDevice::getId).collect(Collectors.toSet());
    }

    private Map<Long, DataQualityKpiMember> deviceIdsInKpiMembers() {
        return getKpiMembers().stream()
                .collect(Collectors.toMap(
                        member -> Long.parseLong(member.getTargetIdentifier()),
                        Function.identity()));
    }

    private void updateKpiMemberIfNeeded(DataQualityKpiMember dataQualityKpiMember) {
        Kpi kpi = dataQualityKpiMember.getChildKpi();
        Set<String> existingKpiMemberNames = kpi.getMembers().stream().map(KpiMember::getName).collect(Collectors.toSet());
        String identifier = dataQualityKpiMember.getTargetIdentifier();
        List<String> membersToCreate = actualKpiMemberTypes()
                .map(DataQualityKpiMemberType::getName)
                .map(member -> member + KPIMEMBERNAME_SEPARATOR + identifier)
                .filter(not(existingKpiMemberNames::contains))
                .collect(Collectors.toList());
        if (!membersToCreate.isEmpty()) {
            KpiUpdater kpiUpdater = kpi.startUpdate();
            membersToCreate.forEach(name -> kpiUpdater.member().named(name).add());
            kpiUpdater.update();
        }
    }

    public static String kpiMemberNameSuffix(Meter meter) {
        return "" + meter.getId();
    }

    @Override
    public void makeObsolete() {
        this.deviceGroup.setNull();
        super.makeObsolete();
    }
}
