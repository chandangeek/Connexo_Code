/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.gogo;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides useful gogo commands that will support device related operations
 * that are NOT yet provided by the GUI.
 * <p>
 * <ul>
 * <li>enableOutboundCommunication
 * <ul>
 * <li>will add all outbound connections that are enabled on the configuration</li>
 * <li>will schedule all communication tasks that are enabled on the configuration</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-06 (13:44)
 */
@Component(name = "com.energyict.mdc.device.data.gogo", service = DeviceDataGoGoCommands.class,
        property = {"osgi.command.scope=" + DeviceDataServices.COMPONENT_NAME,
                "osgi.command.function=sendCalendarMessage",
                "osgi.command.function=enableOutboundCommunication",
                "osgi.command.function=devices",
                "osgi.command.function=comTaskExecution",
                "osgi.command.function=readMeter"
        }, immediate = true)
@SuppressWarnings("unused")
public class DeviceDataGoGoCommands {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile CalendarService calendarService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DeviceService deviceService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile Clock clock;
    private volatile MultiSenseHeadEndInterface headEndInterface;
    private volatile MeteringService meteringService;

    private enum ScheduleFrequency {
        DAILY {
            @Override
            public void enableOutboundCommunication(TransactionService transactionService, DeviceService deviceService, String scheduleOption, List<Device> devices) {
                new EnableDailyCommunicationTransaction(transactionService, devices).execute();
            }
        },

        NONE {
            @Override
            public void enableOutboundCommunication(TransactionService transactionService, DeviceService deviceService, String scheduleOption, List<Device> devices) {
                // This enum value represents no scheduling frequency so we will not enable anything on the devices
            }
        };

        public static ScheduleFrequency fromString(String name) {
            try {
                return ScheduleFrequency.valueOf(name);
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }

        public abstract void enableOutboundCommunication(TransactionService transactionService, DeviceService deviceService, String scheduleOption, List<Device> devices);

    }

    @SuppressWarnings("unused")
    public void enableOutboundCommunication() {
        System.out.println("enableOutboundCommunication [DAILY | NONE] <schedule options> <device names>+ (i.e. at least one device name)");
    }

    @SuppressWarnings("unused")
    public void enableOutboundCommunication(String scheduleFrequency, String scheduleOption, String... deviceNames) {
        try {
            ScheduleFrequency.fromString(scheduleFrequency).enableOutboundCommunication(this.transactionService, this.deviceService, scheduleOption, this.findDevices(deviceNames));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void devices() {
        try {
            System.out.println(deviceService.findAllDevices(Condition.TRUE).stream()
                    .map(this::toString)
                    .collect(Collectors.joining("\n")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String toString(Device device) {
        return device.getId() + " " + device.getName();
    }

    private List<Device> findDevices(String... deviceNames) {
        List<Device> devices = new ArrayList<>(deviceNames.length);
        for (String deviceName : deviceNames) {
            this.addDeviceIfExists(deviceName, devices);
        }
        return devices;
    }

    private void addDeviceIfExists(String deviceName, List<Device> devices) {
        Optional<Device> device = this.deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            devices.add(device.get());
        } else {
            System.out.println("Device with name '" + deviceName + "' does not exist and has been ignored");
        }
    }

    @SuppressWarnings("unused")
    public void sendCalendarMessage() {
        System.out.println("sendCalendarMessage <device id> <calendar id> <release date: yyyy-MM-DD>");
    }

    @SuppressWarnings("unused")
    public void sendCalendarMessage(long deviceId, long calendarId, String releaseDateString) {
        runInTransaction(() -> {
            Device device = this.findDeviceOrThrowException(deviceId);
            Calendar calendar = this.findCalendarOrThrowException(calendarId);
            Instant releaseDate = this.instantFromString(releaseDateString);
            DeviceMessageId deviceMessageId = DeviceMessageId.ACTIVITY_CALENDER_SEND;
            DeviceMessageSpec deviceMessageSpec = this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()).get();
            //Find the message attribute of type 'String' without any possible values. This is the 'activityCalendarName' attribute.
            PropertySpec activityCalendarNamePropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(isStringPropertySpecWithoutPossibleValues())
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("DeviceMessageSpec '" + deviceMessageSpec.getName() + "' (ID: '" + deviceMessageId.dbValue() + "') must have a string attribute for the activity calendar name"));

            //Find the message attribute of type 'reference' (to a DeviceMessageFile or Calendar). This is the 'activityCalendar' attribute.
            PropertySpec activityCalendarPropertySpec = deviceMessageSpec
                    .getPropertySpecs()
                    .stream()
                    .filter(isCalendarOrMessageFileSpec())
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("DeviceMessageSpec '" + deviceMessageSpec.getName() + "' (ID: '" + deviceMessageId.dbValue() + "') must have a reference attribute for the activity calendar"));

            DeviceMessage message =
                    device
                            .newDeviceMessage(deviceMessageId)
                            .setReleaseDate(releaseDate)
                            .addProperty(
                                    activityCalendarNamePropertySpec.getName(),
                                    calendar.getName())
                            .addProperty(
                                    activityCalendarPropertySpec.getName(),
                                    calendar)
                            .add();
            System.out.println("message created with id " + message.getId());
        });
    }

    private Predicate<PropertySpec> isStringPropertySpecWithoutPossibleValues() {
        return propertySpec -> propertySpec.getValueFactory().getValueType().equals(String.class) && (propertySpec.getPossibleValues() == null || propertySpec.getPossibleValues().getAllValues().isEmpty());
    }

    private Predicate<PropertySpec> isCalendarOrMessageFileSpec() {
        return propertySpec -> (propertySpec.getValueFactory().isReference());
    }

    private Device findDeviceOrThrowException(long deviceId) {
        return this.deviceService
                .findDeviceById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with id " + deviceId + " does not exist"));
    }

    private Calendar findCalendarOrThrowException(long id) {
        return this.calendarService
                .findCalendar(id)
                .orElseThrow(() -> new IllegalArgumentException("Calendar with id " + id + " does not exist"));
    }

    private Instant instantFromString(String aString) {
        try {
            return LocalDate.parse(aString, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            System.out.printf("%s cannot be parsed from format " + DateTimeFormatter.ISO_LOCAL_DATE.toString(), aString);
            throw e;
        }
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setHeadEndInterface(MultiSenseHeadEndInterface headEndInterface) {
        this.headEndInterface = headEndInterface;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    private static class EnableDailyCommunicationTransaction {
        private final TransactionService transactionService;
        private final List<Device> devices;

        private EnableDailyCommunicationTransaction(TransactionService transactionService, List<Device> devices) {
            super();
            this.transactionService = transactionService;
            this.devices = devices;
        }

        private void execute() {
            for (Device device : this.devices) {
                this.execute(device);
            }
        }

        private void execute(final Device device) {
            this.transactionService.execute(() -> {
                addScheduledConnectionTasks(device);
                List<ComTaskExecution> comTaskExecutions = addComTaskExecutions(device);
                device.save();
                if (comTaskExecutions.isEmpty()) {
                    System.out.printf("No communication tasks were scheduled for device " + device.getName() + " because not tasks were enabled on the device configuration: " + device
                            .getDeviceConfiguration()
                            .getName());
                }
                return null;
            });
        }

        private ScheduledConnectionTask addScheduledConnectionTasks(Device device) {
            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            ScheduledConnectionTask defaultConnectionTask = null;
            List<PartialScheduledConnectionTask> partialOutboundConnectionTasks = deviceConfiguration.getPartialOutboundConnectionTasks();
            for (PartialScheduledConnectionTask partialOutboundConnectionTask : partialOutboundConnectionTasks) {
                ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialOutboundConnectionTask).add();
                if (partialOutboundConnectionTask.isDefault()) {
                    defaultConnectionTask = scheduledConnectionTask;
                }
            }
            return defaultConnectionTask;
        }

        private List<ComTaskExecution> addComTaskExecutions(Device device) {
            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            if (deviceConfiguration.getProtocolDialectConfigurationPropertiesList().isEmpty()) {
                System.out.println("No communication task scheduled for device " + device.getName() + "because no protocol dialect was configured in the device configuration: " + device
                        .getDeviceConfiguration()
                        .getName());
                return Collections.emptyList();
            } else {
                ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
                List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
                for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
                    comTaskExecutions.add(device.newAdHocComTaskExecution(comTaskEnablement).add());
                }
                return comTaskExecutions;
            }
        }

    }

    public void comTaskExecution(long id) {
        String message = communicationTaskService.findComTaskExecution(id)
                .map(this::description)
                .orElse("No such ComTaskExecution");
        System.out.println(message);
    }

    private String description(ComTaskExecution comTaskExecution) {
        return "Status : " + comTaskExecution.getStatus();
    }

    public void readMeter() {
        System.out.println("usage                     :    readMeter <meter_name> <reading_type_MRID_list> [<date_for_read>]");
        System.out.println("meter_name                :    Must be string. If has numeric format, just enclose the name in quotes.");
        System.out.println("reading_type_MRID_list    :    A comma-separated list of MRIDs, like 0.0.7.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0,0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0");
        System.out.println("date_for_read             :    Optional date/time to schedule meter read to, formatted yyyyMMddHHmmss");
    }

    public void readMeter(String name, String readingTypes) {
        runInTransaction(() -> readMeter(name, readingTypes, clock.instant()));
    }

    public void readMeter(String name, String readingTypes, String timestamp) {
        Instant instant = LocalDateTime.from(DATE_TIME_FORMAT.parse(timestamp)).atZone(ZoneId.systemDefault()).toInstant();
        runInTransaction(() -> readMeter(name, readingTypes, instant));
    }

    private void runInTransaction(Runnable runnable) {
        threadPrincipalService.set(() -> "DeviceDataGoGoCommands");
        try (TransactionContext context = transactionService.getContext()) {
            runnable.run();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private void readMeter(String name, String readingTypesString, Instant timestamp) {
        Meter meter = meteringService.findMeterByName(name)
                .orElseThrow(() -> new NoSuchElementException("Meter with name '" + name + "' does not exist."));
        List<String> mrids = Arrays.stream(readingTypesString.split(",")).distinct().collect(Collectors.toList());
        List<ReadingType> readingTypes = meteringService.findReadingTypes(mrids);
        if (readingTypes.size() < mrids.size()) {
            Set<String> found = readingTypes.stream()
                    .map(ReadingType::getMRID)
                    .collect(Collectors.toSet());
            String unfound = mrids.stream()
                    .filter(mrid -> !found.contains(mrid))
                    .collect(Collectors.joining(","));
            throw new NoSuchElementException("Following reading types are not found in system: " + unfound);
        }
        headEndInterface.scheduleMeterRead(meter, readingTypes, timestamp);
        System.out.println("Meter read is scheduled.");
    }
}
