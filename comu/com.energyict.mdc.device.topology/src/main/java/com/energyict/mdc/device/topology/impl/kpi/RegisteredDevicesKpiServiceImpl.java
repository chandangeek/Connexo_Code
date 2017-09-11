/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
import com.energyict.mdc.device.topology.kpi.Privileges;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;


import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegisteredDevicesKpiServiceImpl implements RegisteredDevicesKpiService, TranslationKeyProvider {

    private DataModel dataModel;

    @Inject
    public RegisteredDevicesKpiServiceImpl(ServerTopologyService topologyService) {
        this.dataModel = topologyService.dataModel();
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
        return DefaultFinder.of(RegisteredDevicesKpi.class, this.dataModel, EndDeviceGroup.class).defaultSortColumn(RegisteredDevicesKpiImpl.Fields.END_DEVICE_GROUP.fieldName()+".name");
    }

    @Override
    public Optional<RegisteredDevicesKpi> findRegisteredDevicesKpi(long id) {
        return this.dataModel.mapper(RegisteredDevicesKpi.class).getOptional(id);

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
    public String getComponentName() {
        return TopologyService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
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
        public RegisteredDevicesKpi save() {
            underConstruction.save();
            return underConstruction;
        }
    }
}
