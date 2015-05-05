package com.energyict.mdc.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
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

        deviceProtocolPluggableClassByName.map(deviceProtocolPluggableClass -> {
            List<DeviceType> deviceTypesWithDeviceProtocol = deviceConfigurationService.findDeviceTypesWithDeviceProtocol(deviceProtocolPluggableClass);
            List<DeviceConfiguration> deviceConfigurations = deviceTypesWithDeviceProtocol.stream().flatMap(deviceType -> deviceType.getConfigurations().stream()).collect(Collectors.toList());
            List<Device> devices = deviceConfigurations.stream().flatMap(deviceConfiguration -> this.deviceService.findDevicesByDeviceConfiguration(deviceConfiguration).stream()).collect(Collectors.toList());

            System.out.println("Are you really really sure you want to delete:");
            System.out.println("    - " + deviceTypesWithDeviceProtocol.size() + " deviceTypes");
            System.out.println("    - " + deviceConfigurations.size() + " deviceConfigurations");
            System.out.println("    - " + devices.size() + " devices");
            System.out.println("    - the '" + communicationProtocol + "' protocol");
            System.out.println("??? (Y/n)");
            try {
                String response = readInput();
                boolean proceed = analyseResponse(response);
                if (proceed) {
                    transactionService.execute(new VoidTransaction() {
                                                   @Override
                                                   protected void doPerform() {
                                                       boolean success = setupThreadPrinciple();
                                                       if(success){
                                                           System.out.println("Here we go ... (this can take a while)");
                                                           System.out.print("Deleting devices ");
                                                           devices.stream().forEach(device -> {
                                                               device.delete();
                                                               System.out.print(".");
                                                           });
                                                           System.out.println(" Done!");
                                                           System.out.print("Deleting deviceconfigurations ");
                                                           deviceConfigurations.stream().forEach(deviceConfiguration -> {
                                                               deviceConfiguration.deactivate();
                                                               System.out.print(".");
                                                           });
                                                           System.out.println(" Done!");
                                                           System.out.print("Deleting devicetypes ");
                                                           deviceTypesWithDeviceProtocol.stream().forEach(deviceType -> {
                                                               deviceType.delete();
                                                               System.out.print(".");
                                                           });
                                                           System.out.println(" Done!");
                                                           System.out.print("Deleting the protocol ... ");
                                                           deviceProtocolPluggableClass.delete();
                                                           System.out.println("Completely finished");
                                                           System.out.println("Please note that at the first restart your protocol will be auto-loaded again!");
                                                       } else {
                                                           System.out.println("Could not set a proper user, action will not be executed, nothing will be deleted!");
                                                       }
                                                   }
                                               }
                    );

                } else {
                    System.out.println("I'm glad you changed your mind, will not delete anything ...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }).
                orElseGet(() -> {
                    System.out.println("Could not find a protocol pluggable class with the name " + communicationProtocol);
                    return true;
                });
    }

    private boolean setupThreadPrinciple() {
        Optional<User> admin = this.userService.findUser("admin");
        if(admin.isPresent()){
            this.threadPrincipalService.set(admin.get());
            return true;
        } else {
            System.out.println("Not possible to set the user, action will fail");
            return false;
        }
    }

    private boolean analyseResponse(String response) {
        String[] ok = {"", "y", "yes"};
        return Stream.of(ok).filter(option -> option.equalsIgnoreCase(response)).findFirst().isPresent();
    }

    private String readInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}
