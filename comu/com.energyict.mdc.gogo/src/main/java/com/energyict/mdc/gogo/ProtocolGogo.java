package com.energyict.mdc.gogo;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.gogo.ProtocolGogo", service = ProtocolGogo.class,
        property = {"osgi.command.scope=mdc.protocol",
                "osgi.command.function=help",
                "osgi.command.function=cleanUpProtocol"},
        immediate = true)
@SuppressWarnings("unused")
public class ProtocolGogo {

    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile TransactionService transactionService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService){
        this.userService = userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService){
        this.threadPrincipalService = threadPrincipalService;
    }

    public void help() {
        System.out.println("cleanUpProtocol <communicationProtocolName>");
        System.out.println("    Removes the communicationProtocol, including all existing:");
        System.out.println("        - Devices");
        System.out.println("        - DeviceConfigurations");
        System.out.println("        - DeviceTypes");
        System.out.println("    ... so make sure you know what you are doing!");
    }

    public void cleanUpProtocol(String communicationProtocol) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClassByName = protocolPluggableService.findDeviceProtocolPluggableClassByName(communicationProtocol.toUpperCase());
        if (deviceProtocolPluggableClassByName.isPresent()) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceProtocolPluggableClassByName.get();
            CleanupTransaction cleanupTransaction = new CleanupTransaction(communicationProtocol, deviceProtocolPluggableClass);
            try {
                boolean proceed = cleanupTransaction.getConfirmationFromUser();
                if (proceed) {
                    this.transactionService.execute(cleanupTransaction);
                }
                else {
                    System.out.println("I'm glad you changed your mind, will not delete anything ...");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Could not find a protocol pluggable class with the name " + communicationProtocol);
        }
    }

    private class CleanupTransaction extends VoidTransaction {
        private final String communicationProtocol;
        private final DeviceProtocolPluggableClass deviceProtocolPluggableClass;
        private final List<DeviceType> deviceTypesWithDeviceProtocol;
        private final List<DeviceConfiguration> deviceConfigurations;
        private final List<Device> devices;

        private CleanupTransaction(String communicationProtocol, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            super();
            this.communicationProtocol = communicationProtocol;
            this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
            this.deviceTypesWithDeviceProtocol = deviceConfigurationService.findDeviceTypesWithDeviceProtocol(deviceProtocolPluggableClass);
            this.deviceConfigurations =
                    this.deviceTypesWithDeviceProtocol
                            .stream()
                            .flatMap(deviceType -> deviceType.getConfigurations().stream())
                            .collect(Collectors.toList());
            this.devices =
                    this.deviceConfigurations
                            .stream()
                            .flatMap(this::findDevicesByConfiguration)
                            .collect(Collectors.toList());
        }

        private Stream<Device> findDevicesByConfiguration(DeviceConfiguration deviceConfiguration) {
            return deviceService.findDevicesByDeviceConfiguration(deviceConfiguration).stream();
        }

        boolean getConfirmationFromUser() throws IOException {
            System.out.println("Are you really really sure you want to delete:");
            System.out.println("    - " + this.deviceTypesWithDeviceProtocol.size() + " deviceTypes");
            System.out.println("    - " + this.deviceConfigurations.size() + " deviceConfigurations");
            System.out.println("    - " + this.devices.size() + " devices");
            System.out.println("    - the '" + this.communicationProtocol + "' protocol");
            System.out.println("??? (Y/n)");
            return this.analyseResponse(this.readInput());
        }

        private String readInput() throws IOException {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        }

        private boolean analyseResponse(String response) {
            String[] ok = {"", "y", "yes"};
            return Stream.of(ok).filter(option -> option.equalsIgnoreCase(response)).findFirst().isPresent();
        }

        @Override
        protected void doPerform() {
            boolean success = this.setupThreadPrinciple();
            if (success) {
                System.out.println("Here we go ... (this can take a while)");
                System.out.print("Deleting devices");
                this.devices.stream().forEach(this::delete);
                this.logDone();
                System.out.print("Deactivating device configurations");
                this.deviceConfigurations.stream().forEach(this::deactivate);
                this.logDone();
                System.out.print("Deleting device types");
                this.deviceTypesWithDeviceProtocol.stream().forEach(this::delete);
                this.logDone();
                System.out.print("Deleting the protocol...");
                this.deviceProtocolPluggableClass.delete();
                System.out.println("Completely finished");
                System.out.println("Please note that at the first restart your protocol will be recreated by the 'out of the box' activation process for protocol classes!");
            } else {
                System.out.println("Could not set a proper user, action will not be executed, nothing will be deleted!");
            }
        }

        private boolean setupThreadPrinciple() {
            Optional<User> admin = userService.findUser("admin");
            if (admin.isPresent()) {
                threadPrincipalService.set(admin.get());
                return true;
            } else {
                System.out.println("Not possible to set the user, action will fail");
                return false;
            }
        }

        private void logDone() {
            System.out.println(" Done!");
        }

        private void delete(Device device) {
            device.delete();
            System.out.print(".");
        }

        private void deactivate(DeviceConfiguration configuration) {
            configuration.deactivate();
            System.out.print(".");
        }

        private void delete(DeviceType deviceType) {
            deviceType.delete();
            System.out.print(".");
        }
    }

}