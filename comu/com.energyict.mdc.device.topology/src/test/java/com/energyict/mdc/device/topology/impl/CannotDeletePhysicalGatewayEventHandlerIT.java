/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.topology.StillGatewayException;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link CannotDeletePhysicalGatewayEventHandler} compnent.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:43)
 */
public class CannotDeletePhysicalGatewayEventHandlerIT extends PersistenceIntegrationTest {

    @Before
    public void addEventHandlers() {
        ServerTopologyService topologyService = inMemoryPersistence.getTopologyService();
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new CannotDeletePhysicalGatewayEventHandler(topologyService,  mock(Thesaurus.class))));
    }

    @Test(expected = StillGatewayException.class)
    @Transactional
    public void cannotDeleteBecauseStillUsedAsPhysicalGatewayTest() {
        Device physicalMaster = this.createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = getDeviceService().newDevice(deviceConfiguration, "Origin1", MRID, Instant.now());
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);

        try {
            // Business method
            physicalMaster.delete();
        }
        catch (StillGatewayException e) {
            // Asserts
            assertThat(e.getMessageSeed().equals(MessageSeeds.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY));
            throw e;
        }
    }

    @Test
    @Transactional
    public void deletePhysicalMasterAfterDeletingSlaveTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device = getDeviceService().newDevice(deviceConfiguration, "Origin", MRID, Instant.now());
        device.save();
        this.getTopologyService().setPhysicalGateway(device, physicalMaster);

        device.delete();

        Device reloadedMaster = getReloadedDevice(physicalMaster);
        long masterId = reloadedMaster.getId();
        physicalMaster.delete();

        assertThat(getDeviceService().findDeviceById(masterId).isPresent()).isFalse();
    }

    private ServerDeviceService getDeviceService() {
        return inMemoryPersistence.getDeviceService();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
    }

    private class SubscriberForTopicHandler implements Subscriber {
        private final TopicHandler topicHandler;

        private SubscriberForTopicHandler(TopicHandler topicHandler) {
            super();
            this.topicHandler = topicHandler;
        }

        @Override
        public void handle(Object notification, Object... notificationDetails) {
            LocalEvent event = (LocalEvent) notification;
            if (event.getType().getTopic().equals(this.topicHandler.getTopicMatcher())) {
                this.topicHandler.handle(event);
            }
        }

        @Override
        public Class<?>[] getClasses() {
            return new Class<?>[]{LocalEvent.class};
        }

    }

}