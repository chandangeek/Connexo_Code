package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.devices.CreateDeviceCommand;
import com.elster.jupiter.demo.impl.commands.upload.UploadAllCommand;
import com.elster.jupiter.demo.impl.commands.upload.ValidateStartDateCommand;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CreateDemoDataCommand {
    private final Provider<CreateCollectRemoteDataSetupCommand> createCollectRemoteDataSetupCommandProvider;
    private final Provider<CreateUserManagementCommand> createUserManagementCommandProvider;
    private final Provider<CreateApplicationServerCommand> createApplicationServerCommandProvider;
    private final Provider<CreateNtaConfigCommand> createNtaConfigCommandProvider;
    private final Provider<UploadAllCommand> uploadAllCommandProvider;
    private final Provider<CreateValidationSetupCommand> createValidationSetupCommandProvider;
    private final Provider<CreateDeviceCommand> createDeviceCommandProvider;
    private final Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider;
    private final Provider<ValidateStartDateCommand> validateStartDateCommandProvider;

    private String comServerName;
    private String host;
    private String startDate;

    @Inject
    public CreateDemoDataCommand(
            Provider<CreateCollectRemoteDataSetupCommand> createCollectRemoteDataSetupCommandProvider,
            Provider<CreateUserManagementCommand> createUserManagementCommandProvider,
            Provider<CreateApplicationServerCommand> createApplicationServerCommandProvider,
            Provider<CreateNtaConfigCommand> createNtaConfigCommandProvider,
            Provider<UploadAllCommand> uploadAllCommandProvider,
            Provider<CreateValidationSetupCommand> createValidationSetupCommandProvider,
            Provider<CreateDeviceCommand> createDeviceCommandProvider,
            Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider,
            Provider<ValidateStartDateCommand> validateStartDateCommandProvider) {
        this.createCollectRemoteDataSetupCommandProvider = createCollectRemoteDataSetupCommandProvider;
        this.createUserManagementCommandProvider = createUserManagementCommandProvider;
        this.createApplicationServerCommandProvider = createApplicationServerCommandProvider;
        this.createNtaConfigCommandProvider = createNtaConfigCommandProvider;
        this.uploadAllCommandProvider = uploadAllCommandProvider;
        this.createValidationSetupCommandProvider = createValidationSetupCommandProvider;
        this.createDeviceCommandProvider = createDeviceCommandProvider;
        this.createDeliverDataSetupCommandProvider = createDeliverDataSetupCommandProvider;
        this.validateStartDateCommandProvider = validateStartDateCommandProvider;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.parse(this.startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault());
        if (zonedDateTime.getDayOfMonth() != 1){
            throw new UnableToCreate("Please specify the first day of month as a start date");
        }
    }

    public void run(){
        validateStartDateCommand();
        createUserManagementCommand();
        createCollectRemoteDataSetupCommand();
        createValidationSetupCommand();
        createApplicationServerCommand();
        createNtaConfigCommand();
        createMockedDataDeviceCommand();
        uploadAllData();
        createDeliverDataSetupCommand();
    }

    private void validateStartDateCommand(){
        String[] resourceFiles = {
                "realisticChannelData - Interval.csv",
                "realisticChannelData - Daily.csv",
                "realisticChannelData - Monthly.csv",
                "realisticRegisterData.csv",
                "realisticChannelData - Interval - Validation.csv",
                "realisticChannelData - Daily - Validation.csv",
                "realisticRegisterData - Validation.csv"
        };
        for (String resourceFile : resourceFiles) {
            ValidateStartDateCommand command = this.validateStartDateCommandProvider.get();
            command.setStartDate(this.startDate);
            command.setSource(getResourceAsStream(resourceFile));
            command.run();
        }
    }

    private InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    private void createUserManagementCommand(){
        CreateUserManagementCommand command = this.createUserManagementCommandProvider.get();
        command.run();
    }

    private void createCollectRemoteDataSetupCommand(){
        CreateCollectRemoteDataSetupCommand command = this.createCollectRemoteDataSetupCommandProvider.get();
        command.setComServerName(this.comServerName);
        command.setHost(this.host);
        command.run();
    }

    private void createValidationSetupCommand(){
        CreateValidationSetupCommand command = this.createValidationSetupCommandProvider.get();
        command.run();
    }

    private void createApplicationServerCommand(){
        CreateApplicationServerCommand command = this.createApplicationServerCommandProvider.get();
        command.setName(this.comServerName);
        command.run();
    }

    private void createNtaConfigCommand(){
        CreateNtaConfigCommand command = this.createNtaConfigCommandProvider.get();
        command.run();
    }

    private void createMockedDataDeviceCommand(){
        CreateDeviceCommand command = this.createDeviceCommandProvider.get();
        command.setSerialNumber("093000020359");
        command.setMridPrefix(Constants.Device.MOCKED_REALISTIC_DEVICE);
        command.run();
    }

    private void uploadAllData(){
        UploadAllCommand command = this.uploadAllCommandProvider.get();
        command.setStartDate(this.startDate);
        command.run();
    }

    private void createDeliverDataSetupCommand(){
        CreateDeliverDataSetupCommand createDeliverDataSetupCommand = this.createDeliverDataSetupCommandProvider.get();
        createDeliverDataSetupCommand.run();
    }
}
