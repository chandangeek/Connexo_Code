/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
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
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@UniqueUsagePointGroupAndPurpose(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE + "}")
public final class UsagePointDataQualityKpiImpl extends DataQualityKpiImpl implements UsagePointDataQualityKpi {

    static final String KPIMEMBERNAME_SUFFIX_SEPARATOR = ":";

    public enum Fields {

        USAGEPOINT_GROUP("usagePointGroup"),
        METROLOGY_PURPOSE("metrologyPurpose");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();

    @Inject
    public UsagePointDataQualityKpiImpl(DataModel dataModel, MeteringService meteringService, ValidationService validationService,
                                        EstimationService estimationService, MessageService messageService, TaskService taskService,
                                        KpiService kpiService, Clock clock) {
        super(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
    }

    UsagePointDataQualityKpiImpl init(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency) {
        this.usagePointGroup.set(usagePointGroup);
        this.metrologyPurpose.set(metrologyPurpose);
        super.setFrequency(calculationFrequency);
        return this;
    }

    UsagePointDataQualityKpiImpl init(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency, List<RecurrentTask> nextRecurrentTasks) {
        this.usagePointGroup.set(usagePointGroup);
        this.metrologyPurpose.set(metrologyPurpose);
        super.setFrequency(calculationFrequency);
        super.setNextRecurrentTasks(nextRecurrentTasks);
        return this;
    }

    @Override
    public UsagePointGroup getUsagePointGroup() {
        return usagePointGroup.orNull();
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return metrologyPurpose.orNull();
    }

    @Override
    KpiType getKpiType() {
        return KpiType.USAGE_POINT_DATA_QUALITY_KPI;
    }

    @Override
    String getRecurrentTaskName() {
        return getKpiType().recurrentTaskName(getUsagePointGroup().getName() + "/" + getMetrologyPurpose().getName());
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDM;
    }

    @Override
    public Map<Long, List<DataQualityKpiMember>> updateMembers(Range<Instant> interval) {
        if (getKpiMembers().isEmpty()) {
            return createDataQualityKpiMembers(interval);
        }
        Set<UsagePoint> usagePointsInGroup = usagePointsInGroup();
        Map<Long, List<DataQualityKpiMember>> dataQualityKpiMembersMap = usagePointIdsInKpiMembers();
        Set<Long> commonElements = intersection(
                usagePointsInGroup().stream().map(HasId::getId).collect(Collectors.toSet()),
                dataQualityKpiMembersMap.keySet());

        // create kpis for new usage points in group
        usagePointsInGroup.stream()
                .filter(usagePoint -> !commonElements.contains(usagePoint.getId()))
                .forEach(usagePoint -> createDataQualityKpiMemberIfPurposeActive(usagePoint, interval));

        // update existing kpis with the new kpi members
        // (in case new validators/estimators deployed or metrology configuration has been changed)
        dataQualityKpiMembersMap.entrySet().stream()
                .filter(entry -> commonElements.contains(entry.getKey()))
                .forEach(entry -> updateKpiMemberIfNeeded(entry.getKey(), interval, entry.getValue()));

        // remove kpis for usage points that disappeared from group
        Set<DataQualityKpiMember> obsoleteKpiMembers = dataQualityKpiMembersMap.entrySet().stream()
                .filter(entry -> !commonElements.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .peek(DataQualityKpiMember::remove)
                .collect(Collectors.toSet());
        this.removeAll(obsoleteKpiMembers);

        return usagePointIdsInKpiMembers();
    }

    private Map<Long, List<DataQualityKpiMember>> createDataQualityKpiMembers(Range<Instant> interval) {
        return getUsagePointGroup().getMembers(getClock().instant())
                .stream()
                .map(usagePoint -> Pair.of(usagePoint.getId(), createDataQualityKpiMemberIfPurposeActive(usagePoint, interval)))
                .filter(pair -> !pair.getLast().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    private List<DataQualityKpiMember> createDataQualityKpiMemberIfPurposeActive(UsagePoint usagePoint, Range<Instant> interval) {
        Set<ChannelsContainer> channelContainers = getChannelsContainersForPurpose(usagePoint, getMetrologyPurpose(), interval);
        if (channelContainers.isEmpty()) {
            return Collections.emptyList();
        }

        return channelContainers.stream().map(channelContainer -> createDataQualityKpiMember(usagePoint, channelContainer)).collect(Collectors.toList());

    }

    private DataQualityKpiMember createDataQualityKpiMember(UsagePoint usagePoint, ChannelsContainer container) {
        KpiBuilder kpiBuilder = getKpiService().newKpi();
        kpiBuilder.interval(getFrequency());
        kpiBuilder.timeZone(usagePoint.getZoneId());
        kpiBuilder.keepZeros(false);


        Set<String> actualKpiMemberNames = actualKpiMemberTypes().map(DataQualityKpiMemberType::getName).collect(Collectors.toSet());

        actualKpiMemberNames.forEach(member -> kpiBuilder.member().named(member).add());

        DataQualityKpiMemberImpl dataQualityKpiMember = DataQualityKpiMemberImpl.from(getDataModel(), this, kpiBuilder.create(), usagePoint, container);
        this.add(dataQualityKpiMember);
        return dataQualityKpiMember;
    }

    public static String kpiMemberNameSuffix(UsagePoint usagePoint, ChannelsContainer channelsContainer) {
        return usagePoint.getId() + KPIMEMBERNAME_SUFFIX_SEPARATOR + channelsContainer.getId();
    }

    private Map<Long, List<DataQualityKpiMember>> usagePointIdsInKpiMembers() {
        return getKpiMembers().stream().collect(Collectors.toMap(DataQualityKpiMember::getUsagePointId, Collections::singletonList, (list1, list2) -> {
            List<DataQualityKpiMember> result = new ArrayList<>(list1);
            result.addAll(list2);
            return result;
        }));
    }

    private Set<UsagePoint> usagePointsInGroup() {
        return getUsagePointGroup().getMembers(getClock().instant()).stream().collect(Collectors.toSet());
    }

    private Set<ChannelsContainer> getChannelsContainersForPurpose(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval) {
        return usagePoint.getEffectiveMetrologyConfigurations(interval)
                .stream()
                .map(effectiveMC -> getChannelsContainerForPurpose(effectiveMC, metrologyPurpose))
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
    }

    private Optional<ChannelsContainer> getChannelsContainerForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose purpose) {
        return effectiveMC
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(purpose))
                .findAny()
                .flatMap(effectiveMC::getChannelsContainer);
    }

    private void updateKpiMemberIfNeeded(Long usagePointId, Range<Instant> interval, List<DataQualityKpiMember> dataQualityKpiMembers) {
        UsagePoint usagePoint = getMeteringService().findUsagePointById(usagePointId).get();
        Set<ChannelsContainer> channelsContainersForPurpose = getChannelsContainersForPurpose(usagePoint, getMetrologyPurpose(), interval);
        channelsContainersForPurpose
                .stream()
                .map(container -> Pair.of(container, dataQualityKpiMembers.stream().filter(dataQualityKpiMember -> dataQualityKpiMember.getChannelContainer() == container.getId()).findFirst()))
                .forEach(optional -> {
                    if (optional.getLast().isPresent()) {
                        updateKpiMemberIfNeeded(optional.getLast().get());
                    } else {
                        createDataQualityKpiMember(usagePoint, optional.getFirst());
                    }
                });
    }

    private void updateKpiMemberIfNeeded(DataQualityKpiMember dataQualityKpiMember) {
        Kpi kpi = dataQualityKpiMember.getChildKpi();

        Set<String> existingKpiMemberNames = kpi.getMembers().stream().map(KpiMember::getName).collect(Collectors.toSet());
        Set<String> membersToCreate = actualKpiMemberTypes().map(DataQualityKpiMemberType::getName)
                .filter(not(existingKpiMemberNames::contains))
                .collect(Collectors.toSet());
        if (!membersToCreate.isEmpty()) {
            KpiUpdater kpiUpdater = kpi.startUpdate();
            membersToCreate.forEach(name -> kpiUpdater.member().named(name).add());
            kpiUpdater.update();
        }
    }
}
