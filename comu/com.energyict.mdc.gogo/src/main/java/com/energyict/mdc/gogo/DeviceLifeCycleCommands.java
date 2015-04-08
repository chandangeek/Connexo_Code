package com.energyict.mdc.gogo;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 3/17/15
 * Time: 2:23 PM
 */
@Component(name = "com.energyict.mdc.gogo.DeviceLifeCycleCommands", service = DeviceLifeCycleCommands.class,
        property = {"osgi.command.scope=mdc.device.lifecycle",
                "osgi.command.function=help",
                "osgi.command.function=triggerEvent",
                "osgi.command.function=currentState",
                "osgi.command.function=historicalState"},
        immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleCommands {

    private volatile Clock clock;
    private volatile DeviceService deviceService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile FiniteStateMachineService finiteStateMachineService;

    @Reference
    @SuppressWarnings("unused")
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    public void triggerEvent(String symbolicEventName, String mRID) {
        Optional<StateTransitionEventType> eventType = this.finiteStateMachineService.findStateTransitionEventTypeBySymbol(symbolicEventName);
        if (eventType.isPresent()) {
            this.triggerEvent(eventType.get(), mRID);
        }
        else {
            System.out.println("State transition event type " + symbolicEventName + " does not exist");
        }
    }

    private void triggerEvent(StateTransitionEventType eventType, String mRID) {
        if (eventType instanceof CustomStateTransitionEventType) {
            this.triggerEvent((CustomStateTransitionEventType) eventType, mRID);
        }
        else {
            System.out.println("Only custom state transition event types are supported as I don't know how to publish standard ones");
        }
    }

    private void triggerEvent(CustomStateTransitionEventType eventType, String mRID) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            this.triggerEvent(eventType,  device.get());
        }
        else {
            System.out.println("Device with mRID " + mRID + " does not exist");
        }
    }

    private void triggerEvent(CustomStateTransitionEventType eventType, Device device) {
        eventType
            .newInstance(
                device.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine(),
                String.valueOf(device.getmRID()),
                device.getState().getName(),
                Collections.emptyMap())
            .publish();
    }

    public void currentState(String mRID) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            this.currentState(device.get());
        }
        else {
            System.out.println("Device with mRID " + mRID + " does not exist");
        }
    }

    private void currentState(Device device) {
        String now = this.dateTimeFormatter().format(this.clock.instant());
        System.out.println("State of device (at: " + now + "): " + device.getState().getName());
    }

    public void historicalState(String mRID, String when) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            this.historicalState(device.get(), when);
        }
        else {
            System.out.println("Device with mRID " + mRID + " does not exist");
        }
    }

    private void historicalState(Device device, String when) {
        try {
            Instant historical = Instant.from(this.dateTimeFormatter().parse(when));
            Optional<State> state = device.getState(historical);
            if (state.isPresent()) {
                System.out.println("State of device (at: " + when + "): " + state.get().getName());
            }
            else {
                System.out.println("Device did not exist yet at the specified timestamp: " + when);
            }
        }
        catch (DateTimeParseException e) {
            System.out.println("Invalid date time format, was expecting yyyy-MM-dd HH:mm:ss");
        }
    }

    public void help() {
        System.out.println("triggerEvent <event type> <device mRID>");
        System.out.println("     where <event type> is one of:");
        System.out.println("       #commissioned");
        System.out.println("       #activated");
        System.out.println("       #deactivated");
        System.out.println("       #decommissioned");
        System.out.println("       #deleted");
        System.out.println("       #recycled");
        System.out.println("       #revoked");
        System.out.println("currentState <device mRID>");
        System.out.println("historicalState <device mRID> <yyyy-MM-dd HH:mm:ss>");
    }

    private DateTimeFormatter dateTimeFormatter() {
        return DefaultDateTimeFormatters.shortDate().withLongTime().build();
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            return transactionService.execute(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            clearPrincipal();
        }
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return this.userService.findUser("admin").get();
    }

}