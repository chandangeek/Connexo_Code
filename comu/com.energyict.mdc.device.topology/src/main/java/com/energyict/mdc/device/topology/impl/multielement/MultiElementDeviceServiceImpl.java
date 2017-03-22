package com.energyict.mdc.device.topology.impl.multielement;

/**
 * Copyrights EnergyICT
 * Date: 15/03/2017
 * Time: 16:02
 */

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_3SimpleUpgrader;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.impl.Installer;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayReferenceImpl;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
import com.energyict.mdc.device.topology.impl.utils.ChannelDataTransferor;
import com.energyict.mdc.device.topology.impl.utils.MeteringChannelProvider;
import com.energyict.mdc.device.topology.impl.utils.Utils;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceReference;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link MultiElementDeviceService} interface.
 */
@Component(name = "com.energyict.mdc.device.multi.element", service = {MultiElementDeviceService.class, MessageSeedProvider.class}, property = "name=" + MultiElementDeviceService.COMPONENT_NAME)
public class MultiElementDeviceServiceImpl implements MultiElementDeviceService, MessageSeedProvider {

    private volatile ServerTopologyService topologyService;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;

    private MeteringChannelProvider meteringChannelProvider;

    // For OSGi framework only
    @SuppressWarnings("unused")
    public MultiElementDeviceServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public MultiElementDeviceServiceImpl(ServerTopologyService topologyService, UpgradeService upgradeService, NlsService nlsService) {
        setTopologyService(topologyService);
        setUpgradeService(upgradeService);
        setNlsService(nlsService);
      //  activate();
    }

    @Activate
    public void activate() {
        upgradeService.register(InstallIdentifier.identifier("MultiSense", MultiElementDeviceService.COMPONENT_NAME), getDataModel(), Installer.class, V10_3SimpleUpgrader.V10_3_UPGRADER);
    }

    private DataModel getDataModel() {
        return topologyService.dataModel();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MultiElementDeviceService.class).toInstance(MultiElementDeviceServiceImpl.this);
            }
        };
    }

    @Reference
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MultiElementDeviceService.COMPONENT_NAME, Layer.DOMAIN);
        this.meteringChannelProvider = new MeteringChannelProvider(thesaurus);
    }

    @Override
    public Layer getLayer() {
        return null;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return null;
    }

    @Override
    public void addSlave(Device slave, Device multiElementDevice, Instant linkingDate, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap) {
        Instant linkDate = Utils.generalizeLinkingDate(linkingDate);
        Optional<PhysicalGatewayReference> existingGatewayReference = topologyService.getPhysicalGatewayReference(slave, linkDate);
        if (existingGatewayReference.isPresent()) {
            throw MultiElementDeviceLinkException.slaveWasAlreadyLinkedToOtherDatalogger(thesaurus, slave, existingGatewayReference.get().getGateway(), linkDate);
        }
        validateUniqueKeyConstraintForMultiElementDeviceReference(multiElementDevice, linkDate, slave);

        List<MeterActivation> dataLoggerMeterActivations = multiElementDevice.getMeterActivations(Range.atLeast(linkDate));
        Collections.reverse(dataLoggerMeterActivations);
        List<MeterActivation> slaveMeterActivations = slave.getMeterActivations(Range.atLeast(linkDate));
        Collections.reverse(slaveMeterActivations);

        createNecessaryMultiElementReferences(slave, multiElementDevice, slaveDataLoggerChannelMap, slaveDataLoggerRegisterMap, linkDate, dataLoggerMeterActivations, slaveMeterActivations);
    }

    private void createNecessaryMultiElementReferences(Device slave, Device dataLogger, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap, Instant linkDate, List<MeterActivation> dataLoggerMeterActivations, List<MeterActivation> slaveMeterActivations) {
        Instant start = linkDate;
        Instant end = null;
        for (MeterActivation slaveMeterActivation : slaveMeterActivations) {
            List<MeterActivation> overLappingDataLoggerMeterActivations = Utils.getOverLappingDataLoggerMeterActivations(slaveMeterActivation, dataLoggerMeterActivations);
            for (MeterActivation dataLoggerMeterActivation : overLappingDataLoggerMeterActivations) {
                findOrCreateNewMultiElementDeviceReference(slave, dataLogger, slaveDataLoggerChannelMap, slaveDataLoggerRegisterMap, start, slaveMeterActivation, dataLoggerMeterActivation);

                if (slaveMeterActivation.getEnd() != null) {
                    end = slaveMeterActivation.getEnd();
                }
                if (dataLoggerMeterActivation.getEnd() != null) {
                    if (end == null || end.isAfter(dataLoggerMeterActivation.getEnd())) {
                        end = dataLoggerMeterActivation.getEnd();
                    }
                }
                if (end != null) {
                    start = end;
                    removeSlave(slave, end);
                    end = null;
                }
            }
        }
    }

    private MultiElementDeviceReference findOrCreateNewMultiElementDeviceReference(Device slave, Device dataLogger, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap, Instant start, MeterActivation slaveMeterActivation, MeterActivation dataLoggerMeterActivation) {
        Optional<MultiElementDeviceReference> existingMultiElementDeviceReference = findMultiElementDeviceReference(slave, start);
        if (!existingMultiElementDeviceReference.isPresent()) {
            final MultiElementDeviceReferenceImpl multiElementDeviceReference = this.newMultiElementReference(slave, dataLogger, start);
            slaveDataLoggerChannelMap.forEach((slaveChannel, dataLoggerChannel) -> this.addChannelDataLoggerUsage(multiElementDeviceReference, slaveChannel, dataLoggerChannel, dataLoggerMeterActivation, slaveMeterActivation));
            slaveDataLoggerRegisterMap.forEach((slaveRegister, dataLoggerRegister) -> this.addRegisterDataLoggerUsage(multiElementDeviceReference, slaveRegister, dataLoggerRegister, dataLoggerMeterActivation, slaveMeterActivation));
            Save.CREATE.validate(getDataModel(), multiElementDeviceReference);
            ChannelDataTransferor dataTransferor = new ChannelDataTransferor();
            multiElementDeviceReference.getChannelUsages().stream().forEach(dataTransferor::transferChannelDataToSlave);
            getDataModel().persist(multiElementDeviceReference);
            return multiElementDeviceReference;
        } else {
            return existingMultiElementDeviceReference.get();
        }
    }

    private void addChannelDataLoggerUsage(MultiElementDeviceReferenceImpl multiElementDeviceReference, Channel slave, Channel dataLogger, MeterActivation dataLoggerMeterActivation, MeterActivation slaveMeterActivation) {
        com.elster.jupiter.metering.Channel channelForSlave = meteringChannelProvider.getMeteringChannel(slave, slaveMeterActivation);
        com.elster.jupiter.metering.Channel channelForDataLogger = meteringChannelProvider.getMeteringChannel(dataLogger, dataLoggerMeterActivation);
        multiElementDeviceReference.addChannelUsage(channelForSlave, channelForDataLogger);
    }

    private void addRegisterDataLoggerUsage(MultiElementDeviceReferenceImpl multiElementDeviceReference, Register slave, Register dataLogger, MeterActivation dataLoggerMeterActivation, MeterActivation slaveMeterActivation) {
        com.elster.jupiter.metering.Channel channelForSlave = meteringChannelProvider.getMeteringChannel(slave, slaveMeterActivation);
        com.elster.jupiter.metering.Channel channelForDataLogger = meteringChannelProvider.getMeteringChannel(dataLogger, dataLoggerMeterActivation);
        multiElementDeviceReference.addChannelUsage(channelForSlave, channelForDataLogger);
    }

    public Optional<MultiElementDeviceReference> findMultiElementDeviceReference(Device multiElementSlaveDevice, Instant effective) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(multiElementSlaveDevice).and(where("interval").isEffective(effective));
        return getDataModel().mapper(MultiElementDeviceReference.class)
                .select(condition)
                .stream()
                .findAny(); // the business logic of the effectivity requires that there is only one object effective at a given time
    }

    @Override
    public void removeSlave(Device slave, Instant when) {
        Instant unlinkTimeStamp = Utils.generalizeLinkingDate(when);
        List<PhysicalGatewayReference> physicalGatewayReferences = topologyService.getPhysicalGateWayReferencesFrom(slave, when);
        if (!physicalGatewayReferences.isEmpty()) {
            topologyService.terminateTemporal(physicalGatewayReferences.get(0), unlinkTimeStamp);
            topologyService.slaveTopologyChanged(slave, Optional.empty());
            if (physicalGatewayReferences.size() > 1) {
                for (int i = 1; i < physicalGatewayReferences.size(); i++) {
                    PhysicalGatewayReference gatewayReference = physicalGatewayReferences.get(i);
                    topologyService.terminateTemporal(gatewayReference, gatewayReference.getRange().lowerEndpoint());
                }
            }
        } else {
            throw MultiElementDeviceLinkException.slaveWasNotLinkedAt(thesaurus, slave, unlinkTimeStamp);
        }
    }

    @Override
    public Optional<Device> getMultiElementDevice(Device slave, Instant when) {
        return null;
    }

    @Override
    public List<Device> findMultiElementSlaves(Device multiElementDevice) {
        Condition condition = this.getDevicesInTopologyCondition(multiElementDevice);
        List<MultiElementDeviceReferenceImpl> multiElementDeviceReferences = getDataModel().mapper(MultiElementDeviceReferenceImpl.class).select(condition);
        return this.findUniqueReferencingDevices(new ArrayList<>(multiElementDeviceReferences));
    }

    private Condition getDevicesInTopologyCondition(Device device) {
        return where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(device).and(where("interval").isEffective());
    }

    private List<Device> findUniqueReferencingDevices(List<PhysicalGatewayReference> gatewayReferences) {
        Map<Long, Device> devicesById = new HashMap<>();
        for (PhysicalGatewayReference reference : gatewayReferences) {
            devicesById.put(reference.getOrigin().getId(), reference.getOrigin());
        }
        return new ArrayList<>(devicesById.values());
    }

    @Override
    public Finder<? extends MultiElementDeviceReference> findAllEffectiveMultiElementSlaveDevices() {
        return DefaultFinder.of(MultiElementDeviceReferenceImpl.class, where("interval").isEffective(), getDataModel());
    }

    @Override
    public List<Pair<Channel, Range<Instant>>> getMultiElementSlaveChannelTimeLine(Channel channel, Range<Instant> range){
       return topologyService.getDataLoggerChannelTimeLine(channel, range);
    }

    @Override
    public List<Pair<Register, Range<Instant>>> getMultiElementSlaveRegisterTimeLine(Register register, Range<Instant> intervalReg){
       return topologyService.getDataLoggerRegisterTimeLine(register, intervalReg);
    }

    private void validateUniqueKeyConstraintForMultiElementDeviceReference(Device multiElementDevice, Instant linkingDate, Device slave) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(multiElementDevice)
                .and(where("interval.start").in(Range.closed(linkingDate.toEpochMilli(), linkingDate.toEpochMilli())));
        Optional<MultiElementDeviceReference> duplicateReference = topologyService.dataModel().mapper(MultiElementDeviceReference.class).select(condition).stream().collect(Collectors.toList())
                .stream().filter(reference -> reference.getOrigin().getName().equals(slave.getName()) && reference.getRange().lowerEndpoint().equals(linkingDate)).findAny();
        if (duplicateReference.isPresent()) {
            throw MultiElementDeviceLinkException.slaveWasPreviouslyLinkedAtSameTimeStamp(thesaurus, slave, multiElementDevice, linkingDate);
        }
    }

    private MultiElementDeviceReferenceImpl newMultiElementReference(Device slave, Device gateway, Instant start) {
        return getDataModel().getInstance(MultiElementDeviceReferenceImpl.class).createFor(slave, gateway, Interval.of(Range.atLeast(start)));
    }
}
