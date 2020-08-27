/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceMessageSearchServiceImplIT extends PersistenceIntegrationTest {

    private static final String DEVICE_NAME = "MyUniqueName";
    private static final String SERIAL_NUMBER = "MyUniqueSerialNumber";
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private Device device1;
    private Device device2;
    private Device device3;
    private Device device4;
    private EnumeratedEndDeviceGroup deviceGroup1;
    private EnumeratedEndDeviceGroup deviceGroup2;
    private EnumeratedEndDeviceGroup deviceGroup3;
    private EnumeratedEndDeviceGroup deviceGroup4;

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("CommandlyTestProtocol", CommandlyTestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    @Before
    public void setUp() throws Exception {
        device1 = createSimpleDeviceWithName("dev1");
        device2 = createSimpleDeviceWithName("dev2");
        device3 = createSimpleDeviceWithName("dev3");
        device4 = createSimpleDeviceWithName("dev4");
        AmrSystem amrSystem = inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(() -> new IllegalStateException("ARM not found"));

        deviceGroup1 = createDeviceGroup(device1, amrSystem, "group1");
        deviceGroup2 = createDeviceGroup(device2, amrSystem, "group2");
        deviceGroup3 = createDeviceGroup(device3, amrSystem, "group3");
        deviceGroup4 = createDeviceGroup(device4, amrSystem, "group4");
    }

    @After
    // MultiplierType is a cached object - make sure the cache is cleared after each test
    public void clearCache() {
        inMemoryPersistence.getDataModel().getInstance(OrmService.class).invalidateCache("MTR", "MTR_MULTIPLIERTYPE");
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithName(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, SERIAL_NUMBER, name, start);
    }

    private EnumeratedEndDeviceGroup createDeviceGroup(Device device, AmrSystem amrSystem, String name) {
        return inMemoryPersistence
                .getMeteringGroupsService()
                .createEnumeratedEndDeviceGroup(amrSystem.findMeter("" + device.getId()).get())
                .at(inMemoryPersistence.getClock().instant())
                .setName(name)
                .create();
    }


    @Test
    @Transactional
    public void successfulCreateTest() {
        Device device = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(device).isNotNull();
        assertThat(device.getId()).isGreaterThan(0L);
        assertThat(device.getName()).isEqualTo(DEVICE_NAME);
        assertThat(device.getSerialNumber()).isEqualTo(SERIAL_NUMBER);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByDeviceGroups() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.CONTACTOR_ARM).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();

        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<EndDeviceGroup> getDeviceGroups() {
                return Arrays.asList(deviceGroup1, deviceGroup4);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(deviceMessage1.getDeviceMessageId(), deviceMessage4.getDeviceMessageId());
        assertThat(deviceMessageIds).doesNotContain(deviceMessage2.getDeviceMessageId(), deviceMessage3.getDeviceMessageId());
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByCommandCategories() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageCategory> getMessageCategories() {
                return Collections.singletonList(DeviceMessageTestCategories.CONTACTOR);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesBy_CommandCategories_And_DeviceCommands() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageCategory> getMessageCategories() {
                return Collections.singletonList(DeviceMessageTestCategories.CONTACTOR);
            }

            @Override
            public Collection<DeviceMessageId> getDeviceMessages() {
                return Collections.singletonList(DeviceMessageId.CONTACTOR_CLOSE);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(1);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_CLOSE);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesBy_MultipleCommandCategories_And_DeviceCommands() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageCategory> getMessageCategories() {
                return Arrays.asList(DeviceMessageTestCategories.CONTACTOR, DeviceMessageTestCategories.ALARMS);
            }

            @Override
            public Collection<DeviceMessageId> getDeviceMessages() {
                return Collections.singletonList(DeviceMessageId.CONTACTOR_CLOSE);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(3);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByDeviceMessageStatusMatching() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageStatus> getStatuses() {
                return Collections.singletonList(DeviceMessageStatus.WAITING);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).isEmpty();
    }

    @Test
    @Transactional
    @Ignore("Fails with DeviceMessages size = 3 instead of 4. Can only recreate when running the entire test suite")
    public void selectDeviceMessagesByDeviceMessageStatusMismatching() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageStatus> getStatuses() {
                return Collections.singletonList(DeviceMessageStatus.PENDING);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(4);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByFilter_ReleaseDate_start_and_end() throws Exception {

        Instant releaseDate1 = inMemoryPersistence.getClock().instant();
        Instant releaseDate2 = releaseDate1.plusSeconds(3600);
        Instant releaseDate3 = releaseDate2.plusSeconds(3600);
        Instant releaseDate4 = releaseDate3.plusSeconds(3600);
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(releaseDate1).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(releaseDate2).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(releaseDate3).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(releaseDate4).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Optional<Instant> getReleaseDateStart() {
                return Optional.of(releaseDate1.minusSeconds(100));
            }

            @Override
            public Optional<Instant> getReleaseDateEnd() {
                return Optional.of(releaseDate2.plusSeconds(100));
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByFilter_ReleaseDate_start_only() throws Exception {

        Instant releaseDate1 = inMemoryPersistence.getClock().instant();
        Instant releaseDate2 = releaseDate1.plusSeconds(3600);
        Instant releaseDate3 = releaseDate2.plusSeconds(3600);
        Instant releaseDate4 = releaseDate3.plusSeconds(3600);
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(releaseDate1).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(releaseDate2).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(releaseDate3).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(releaseDate4).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Optional<Instant> getReleaseDateStart() {
                return Optional.of(releaseDate2.plusSeconds(100));
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByFilter_SentDate_start_and_end() throws Exception {

        Instant now = inMemoryPersistence.getClock().instant();
        Instant sentDate1 = inMemoryPersistence.getClock().instant();
        Instant sentDate2 = sentDate1.plusSeconds(3600);
        Instant sentDate3 = sentDate2.plusSeconds(3600);
        Instant sentDate4 = sentDate3.plusSeconds(3600);

        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(now).add();
        deviceMessage1.setSentDate(sentDate1);
        deviceMessage1.save();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(now).add();
        deviceMessage2.setSentDate(sentDate2);
        deviceMessage2.save();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(now).add();
        deviceMessage3.setSentDate(sentDate3);
        deviceMessage3.save();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(now).add();
        deviceMessage4.setSentDate(sentDate4);
        deviceMessage4.save();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Optional<Instant> getSentDateStart() {
                return Optional.of(sentDate1.plusSeconds(1));
            }
            @Override
            public Optional<Instant> getSentDateEnd() {
                return Optional.of(sentDate3);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS);
    }

    @Test
    @Transactional
    public void selectDeviceMessagesByCombinedFilter_CommandCategories_and_DeviceGroup() throws Exception {
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl() {
            @Override
            public Collection<DeviceMessageCategory> getMessageCategories() {
                return Collections.singletonList(DeviceMessageTestCategories.CONTACTOR);
            }

            @Override
            public Collection<EndDeviceGroup> getDeviceGroups() {
                return Collections.singletonList(deviceGroup4);
            }
        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(1);
        List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                .map(DeviceMessage::getDeviceMessageId)
                .collect(Collectors.toList());
        assertThat(deviceMessageIds).containsOnly(DeviceMessageId.CONTACTOR_OPEN);
    }

    private class DeviceMessageQueryFilterImpl implements DeviceMessageQueryFilter {
        @Override
        public Collection<EndDeviceGroup> getDeviceGroups() {
            return Collections.emptyList();
        }

        @Override
        public Collection<DeviceMessageCategory> getMessageCategories() {
            return Collections.emptyList();
        }

        @Override
        public Collection<DeviceMessageId> getDeviceMessages() {
            return Collections.emptyList();
        }

        @Override
        public Collection<DeviceMessageStatus> getStatuses() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Instant> getReleaseDateStart() {
            return Optional.empty();
        }

        @Override
        public Optional<Instant> getReleaseDateEnd() {
            return Optional.empty();
        }

        @Override
        public Optional<Instant> getSentDateStart() {
            return Optional.empty();
        }

        @Override
        public Optional<Instant> getSentDateEnd() {
            return Optional.empty();
        }

        @Override
        public Optional<Instant> getCreationDateStart() {
            return Optional.empty();
        }

        @Override
        public Optional<Instant> getCreationDateEnd() {
            return Optional.empty();
        }
    }

    private enum DeviceMessageTestCategories implements DeviceMessageCategory {

        CONTACTOR {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Stream.of(DeviceMessageId.CONTACTOR_OPEN,
                        DeviceMessageId.CONTACTOR_CLOSE,
                        DeviceMessageId.CONTACTOR_ARM,
                        DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE)
                        .map(id->new DeviceMessageTestSpec(id.name(), id, CONTACTOR))
                        .collect(Collectors.toList());
            }
        },
        ALARMS {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Stream.of(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS,
                        DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS,
                        DeviceMessageId.ALARM_CONFIGURATION_WRITE_ALARM_FILTER)
                        .map(id->new DeviceMessageTestSpec(id.name(), id, ALARMS))
                        .collect(Collectors.toList());
            }
        };

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getDescription() {
            return name();
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        private class DeviceMessageTestSpec implements DeviceMessageSpec {


            private final DeviceMessageId deviceMessageId;
            private final DeviceMessageCategory deviceMessageCategory;
            private final String name;

            DeviceMessageTestSpec(String name, DeviceMessageId deviceMessageId, DeviceMessageCategory deviceMessageCategory) {
                this.deviceMessageId = deviceMessageId;
                this.deviceMessageCategory = deviceMessageCategory;
                this.name = name;
            }

            @Override
            public DeviceMessageCategory getCategory() {
                return this.deviceMessageCategory;
            }

            @Override
            public String getName() {
                return this.name;
            }

            @Override
            public DeviceMessageId getId() {
                return this.deviceMessageId;
            }

            @Override
            public List<PropertySpec> getPropertySpecs() {
                return Collections.emptyList();
            }

            @Override
            public Optional<PropertySpec> getPropertySpec(String name) {
                return Optional.empty();
            }
        }

    }



}
