package com.energyict.mdc.gogo;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Optional;

/**
 * Provides gogo commands that support the device life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-08 (17:29)
 */
@Component(name = "com.energyict.mdc.gogo.DeviceLifeCycleCommands", service = DeviceLifeCycleCommands.class,
        property = {"osgi.command.scope=mdc.device.lifecycle",
                "osgi.command.function=help",
                "osgi.command.function=triggerEvent",
                "osgi.command.function=triggerAction",
                "osgi.command.function=currentState",
                "osgi.command.function=historicalState"},
        immediate = true)
@SuppressWarnings("unused")
public class DeviceLifeCycleCommands {

    private volatile Clock clock;
    private volatile DeviceService deviceService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile FiniteStateMachineService finiteStateMachineService;

    public DeviceLifeCycleCommands() {
        super();
    }

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
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
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

    @SuppressWarnings("unused")
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
        this.executeTransaction(() -> {
            deviceLifeCycleService.triggerEvent(eventType, device);
            return null;
        });
    }

    @SuppressWarnings("unused")
    public void triggerAction(String symbolicEventName, String mRID) {
        Optional<StateTransitionEventType> eventType = this.finiteStateMachineService.findStateTransitionEventTypeBySymbol(symbolicEventName);
        if (eventType.isPresent()) {
            this.triggerAction(eventType.get(), mRID);
        }
        else {
            System.out.println("State transition event type " + symbolicEventName + " does not exist");
        }
    }

    private void triggerAction(StateTransitionEventType eventType, String mRID) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            this.triggerAction(eventType,  device.get());
        }
        else {
            System.out.println("Device with mRID " + mRID + " does not exist");
        }
    }

    private void triggerAction(StateTransitionEventType eventType, Device device) {
        this.executeTransaction(() -> {
            Optional<ExecutableAction> executableAction = this.deviceLifeCycleService.getExecutableActions(device, eventType);
            if (executableAction.isPresent()) {
                executableAction.get().execute(Collections.emptyList());
            }
            else {
                System.out.println("Current state of device with mRID " + device.getmRID() + " does not support the event type");
            }
            return null;
        });
    }

    @SuppressWarnings("unused")
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
        String now = this.dateTimeFormatter().format(this.clock.instant().atZone(ZoneId.of("UTC")));
        System.out.println("State of device (at: " + now + "): " + device.getState().getName());
    }

    @SuppressWarnings("unused")
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
            System.out.println("Invalid date time format, was expecting yyyy-MM-dd HH:mm:ss but got '" + when + "'");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void help() {
        System.out.println("triggerEvent <event type> <device mRID>");
        System.out.println("triggerAction <event type> <device mRID>");
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