package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reacts on DeviceConfiguration Change Events: additional test for datalogger and multi-element devices:
 * The new configuration cannot have less channels available than the number of already linked channels
 * Copyrights EnergyICT
 * Date: 4/04/2017
 * Time: 9:25
 */
@Component(name = "com.energyict.mdc.device.data.deactivate.deviceconfiguration.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceConfigurationChangeVetoEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/deviceconfiguration/VALIDATE_CHANGE";

    private volatile ServerTopologyService topologyService;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public DeviceConfigurationChangeVetoEventHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceConfigurationChangeVetoEventHandler(ServerTopologyService topologyService, NlsService nlsService) {
        this();
        this.setTopologyService(topologyService);
        this.setNlsService(nlsService);
    }

    @Reference
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN);
    }

    /**
     * Vetos the deletion of the {@link DeviceConfiguration}
     * by throwing an exception when there is at least
     * one Device created from that DeviceConfiguration.
     *
     * @param deviceConfiguration The ComTaskEnablement that is about to be deleted
     */
     void validateEnoughChannelsOnTargetConfiguration(Device device, DeviceConfiguration deviceConfiguration) {
        List<Optional<Channel>> linkedSlaveChannels;
        if (!device.getDeviceType().isDataloggerSlave() && !device.getDeviceType().isMultiElementSlave()) {
            linkedSlaveChannels = device.getChannels().stream().map(topologyService::getSlaveChannel).filter(Optional::isPresent).collect(Collectors.toList());
            if (linkedSlaveChannels.size() > deviceConfiguration.getChannelSpecs().size()) {
                // the number of available channels on the new device configuration must be at least equal to current linked channels
                throw DeviceConfigurationChangeException.toLessChannelsProvided(thesaurus, device, deviceConfiguration);
            }
            List<Channel> linkedDataLoggerSlaveChannels = linkedSlaveChannels.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter((c) -> c.getDevice().getDeviceType().isDataloggerSlave())
                    .collect(Collectors.toList());
            if (!linkedDataLoggerSlaveChannels.isEmpty()) {
                if (!deviceConfiguration.isDataloggerEnabled()) {
                    // As the device has linked (data logger slave) channels , the new device configuration must be data logger enabled
                    throw DeviceConfigurationChangeException.deviceConfigMustBeDataLoggerEnabled(thesaurus, device, deviceConfiguration);
                }
            }
            // Also 'Multi-element slave channels' ?
            if (linkedSlaveChannels.size() > linkedDataLoggerSlaveChannels.size()) {
                if (!deviceConfiguration.isMultiElementEnabled()) {
                    // As the device has linked (multi element slave) channels , the new device configuration must be multi-element enabled
                    throw DeviceConfigurationChangeException.deviceConfigMustBeMultiElementEnabled(thesaurus, device, deviceConfiguration);
                }
            }
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        if (localEvent.getSource().getClass().isAssignableFrom(Pair.class)) {
            Pair<Device, DeviceConfiguration> source = (Pair<Device, DeviceConfiguration>) localEvent.getSource();
            validateEnoughChannelsOnTargetConfiguration(source.getFirst(), source.getLast());
        }
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }
}
