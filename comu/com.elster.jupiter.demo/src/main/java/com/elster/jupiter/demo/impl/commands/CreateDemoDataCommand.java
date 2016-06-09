package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.devices.CreateDeviceCommand;
import com.elster.jupiter.demo.impl.commands.upload.UploadAllCommand;
import com.elster.jupiter.demo.impl.commands.upload.ValidateStartDateCommand;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.util.geo.SpatialCoordinates;

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
    private final Provider<CreateEstimationSetupCommand> createEstimationSetupCommandProvider;
    private final Provider<CreateDeviceCommand> createDeviceCommandProvider;
    private final Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider;
    private final Provider<ValidateStartDateCommand> validateStartDateCommandProvider;
    private final Provider<CreateDemoUserCommand> createDemoUserCommandProvider;
    private final Provider<SetupFirmwareManagementCommand> setupFirmwareManagementCommandProvider;
    private final Provider<CreateImportersCommand> createImportersCommandProvider;

    private String comServerName;
    private String host;
    private String startDate;
    private Integer devicesPerType = null;
    private Location location;
    private SpatialCoordinates geoCoordinates;
    private boolean skipFirmwareMamanagementData;

    @Inject
    public CreateDemoDataCommand(
            Provider<CreateCollectRemoteDataSetupCommand> createCollectRemoteDataSetupCommandProvider,
            Provider<CreateUserManagementCommand> createUserManagementCommandProvider,
            Provider<CreateApplicationServerCommand> createApplicationServerCommandProvider,
            Provider<CreateNtaConfigCommand> createNtaConfigCommandProvider,
            Provider<UploadAllCommand> uploadAllCommandProvider,
            Provider<CreateValidationSetupCommand> createValidationSetupCommandProvider,
            Provider<CreateEstimationSetupCommand> createEstimationSetupCommandProvider,
            Provider<CreateDeviceCommand> createDeviceCommandProvider,
            Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider,
            Provider<ValidateStartDateCommand> validateStartDateCommandProvider,
            Provider<CreateDemoUserCommand> createDemoUserCommandProvider,
            Provider<SetupFirmwareManagementCommand> setupFirmwareManagementCommandProvider,
            Provider<CreateImportersCommand> createImportersCommandProvider) {
        this.createCollectRemoteDataSetupCommandProvider = createCollectRemoteDataSetupCommandProvider;
        this.createUserManagementCommandProvider = createUserManagementCommandProvider;
        this.createApplicationServerCommandProvider = createApplicationServerCommandProvider;
        this.createNtaConfigCommandProvider = createNtaConfigCommandProvider;
        this.uploadAllCommandProvider = uploadAllCommandProvider;
        this.createValidationSetupCommandProvider = createValidationSetupCommandProvider;
        this.createEstimationSetupCommandProvider = createEstimationSetupCommandProvider;
        this.createDeviceCommandProvider = createDeviceCommandProvider;
        this.createDeliverDataSetupCommandProvider = createDeliverDataSetupCommandProvider;
        this.validateStartDateCommandProvider = validateStartDateCommandProvider;
        this.createDemoUserCommandProvider = createDemoUserCommandProvider;
        this.setupFirmwareManagementCommandProvider = setupFirmwareManagementCommandProvider;
        this.createImportersCommandProvider = createImportersCommandProvider;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    public void setGeoCoordinates(SpatialCoordinates geoCoordinates){
       this.geoCoordinates = geoCoordinates;
    }

    public void setSkipFirmwareManagementData(boolean skipFirmwareData) {
        this.skipFirmwareMamanagementData = skipFirmwareData;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.parse(this.startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault());
        if (zonedDateTime.getDayOfMonth() != 1){
            throw new UnableToCreate("Please specify the first day of month as a start date");
        }
    }

    public void setDevicesPerType(Integer devicesPerType) {
        this.devicesPerType = devicesPerType;
    }

    public void run(){
        validateStartDateCommand();
        createUserManagementCommand();
        createDemoUserCommand("DemoUser1", "DemoUser2", "DemoUser3", "DemoUser4", "DemoUser5");
        createApplicationServerCommand();
        createCollectRemoteDataSetupCommand();
        setupFirmwareManagementCommand();
        createImportersCommand();
        createValidationSetupCommand();
        createEstimationSetupCommand();
        createNtaConfigCommand();
        createMockedDataDeviceCommand();
        createDeliverDataSetupCommand();

        uploadAllData();
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

    private void createDemoUserCommand(String... usernames){
        CreateDemoUserCommand command = this.createDemoUserCommandProvider.get();
        for (String name: usernames){
            command.setUserName(name);
            command.run();
        }
    }

    private void createCollectRemoteDataSetupCommand(){
        CreateCollectRemoteDataSetupCommand command = this.createCollectRemoteDataSetupCommandProvider.get();
        command.setComServerName(this.comServerName);
        command.setHost(this.host);
        command.setDevicesPerType(this.devicesPerType);
        command.setLocation(location);
        command.setSpatialCoordinates(geoCoordinates);
        command.run();
    }

    private void createValidationSetupCommand(){
        CreateValidationSetupCommand command = this.createValidationSetupCommandProvider.get();
        command.run();
    }

    private void createEstimationSetupCommand(){
        CreateEstimationSetupCommand command = this.createEstimationSetupCommandProvider.get();
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

    private void setupFirmwareManagementCommand(){
        if (!skipFirmwareMamanagementData) {
            SetupFirmwareManagementCommand setupFirmwareManagementCommand = this.setupFirmwareManagementCommandProvider.get();
            setupFirmwareManagementCommand.run();
        }
    }

    private void createImportersCommand(){
        CreateImportersCommand importersCommand = this.createImportersCommandProvider.get();
        importersCommand.setAppServerName(this.comServerName);
        importersCommand.run();
    }
}
