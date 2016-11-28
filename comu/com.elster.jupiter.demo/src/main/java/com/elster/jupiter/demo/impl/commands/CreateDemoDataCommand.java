package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.commands.upload.ValidateStartDateCommand;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;
import javax.inject.Provider;

public class CreateDemoDataCommand {
    private final Provider<CreateCollectRemoteDataSetupCommand> createCollectRemoteDataSetupCommandProvider;
    private final Provider<CreateUserManagementCommand> createUserManagementCommandProvider;
    private final Provider<CreateApplicationServerCommand> createApplicationServerCommandProvider;
    private final Provider<CreateNtaConfigCommand> createNtaConfigCommandProvider;
    private final Provider<CreateValidationSetupCommand> createValidationSetupCommandProvider;
    private final Provider<CreateEstimationSetupCommand> createEstimationSetupCommandProvider;
    private final Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider;
    private final Provider<ValidateStartDateCommand> validateStartDateCommandProvider;
    private final Provider<CreateDemoUserCommand> createDemoUserCommandProvider;
    private final Provider<SetupFirmwareManagementCommand> setupFirmwareManagementCommandProvider;
    private final Provider<CreateImportersCommand> createImportersCommandProvider;
    private final Provider<CreateDataLoggerSetupCommand> createDataLoggerSetupCommandProvider;

    private String comServerName;
    private String host;
    private Integer devicesPerType = null;
    private SpatialCoordinates geoCoordinates;
    private boolean skipFirmwareManagementData;

    @Inject
    public CreateDemoDataCommand(
            Provider<CreateCollectRemoteDataSetupCommand> createCollectRemoteDataSetupCommandProvider,
            Provider<CreateUserManagementCommand> createUserManagementCommandProvider,
            Provider<CreateApplicationServerCommand> createApplicationServerCommandProvider,
            Provider<CreateNtaConfigCommand> createNtaConfigCommandProvider,
            Provider<CreateValidationSetupCommand> createValidationSetupCommandProvider,
            Provider<CreateEstimationSetupCommand> createEstimationSetupCommandProvider,
            Provider<CreateDeliverDataSetupCommand> createDeliverDataSetupCommandProvider,
            Provider<ValidateStartDateCommand> validateStartDateCommandProvider,
            Provider<CreateDemoUserCommand> createDemoUserCommandProvider,
            Provider<SetupFirmwareManagementCommand> setupFirmwareManagementCommandProvider,
            Provider<CreateDataLoggerSetupCommand> createDataLoggerSetupCommandProvider,
            Provider<CreateImportersCommand> createImportersCommandProvider) {
        this.createCollectRemoteDataSetupCommandProvider = createCollectRemoteDataSetupCommandProvider;
        this.createUserManagementCommandProvider = createUserManagementCommandProvider;
        this.createApplicationServerCommandProvider = createApplicationServerCommandProvider;
        this.createNtaConfigCommandProvider = createNtaConfigCommandProvider;
        this.createValidationSetupCommandProvider = createValidationSetupCommandProvider;
        this.createEstimationSetupCommandProvider = createEstimationSetupCommandProvider;
        this.createDeliverDataSetupCommandProvider = createDeliverDataSetupCommandProvider;
        this.validateStartDateCommandProvider = validateStartDateCommandProvider;
        this.createDemoUserCommandProvider = createDemoUserCommandProvider;
        this.setupFirmwareManagementCommandProvider = setupFirmwareManagementCommandProvider;
        this.createImportersCommandProvider = createImportersCommandProvider;
        this.createDataLoggerSetupCommandProvider = createDataLoggerSetupCommandProvider;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setGeoCoordinates(SpatialCoordinates geoCoordinates) {
        this.geoCoordinates = geoCoordinates;
    }

    public void setSkipFirmwareManagementData(boolean skipFirmwareData) {
        this.skipFirmwareManagementData = skipFirmwareData;
    }

    public void setDevicesPerType(Integer devicesPerType) {
        this.devicesPerType = devicesPerType;
    }

    public void run() {
        createUserManagementCommand();
        createDemoUserCommand("DemoUser1", "DemoUser2", "DemoUser3", "DemoUser4", "DemoUser5");
        createApplicationServerCommand();
        createCollectRemoteDataSetupCommand();
        setupFirmwareManagementCommand();
        createImportersCommand();
        createValidationSetupCommand();
        createEstimationSetupCommand();
        createNtaConfigCommand();
        createDeliverDataSetupCommand();
        createDataLoggerSetupCommand();
        System.out.println("Command completed successfully");
    }

    private void createUserManagementCommand() {
        CreateUserManagementCommand command = this.createUserManagementCommandProvider.get();
        command.runInTransaction();
    }

    private void createDemoUserCommand(String... usernames) {
        CreateDemoUserCommand command = this.createDemoUserCommandProvider.get();
        for (String name : usernames) {
            command.setUserName(name);
            command.runInTransaction();
        }
    }

    private void createCollectRemoteDataSetupCommand() {
        CreateCollectRemoteDataSetupCommand command = this.createCollectRemoteDataSetupCommandProvider.get();
        command.setComServerName(this.comServerName);
        command.setHost(this.host);
        command.setDevicesPerType(this.devicesPerType);
        command.run();
    }

    private void createValidationSetupCommand() {
        CreateValidationSetupCommand command = this.createValidationSetupCommandProvider.get();
        command.runInTransaction();
    }

    private void createEstimationSetupCommand() {
        CreateEstimationSetupCommand command = this.createEstimationSetupCommandProvider.get();
        command.runInTransaction();
    }

    private void createApplicationServerCommand() {
        CreateApplicationServerCommand command = this.createApplicationServerCommandProvider.get();
        command.setName(this.comServerName);
        command.runInTransaction();
    }

    private void createNtaConfigCommand() {
        CreateNtaConfigCommand command = this.createNtaConfigCommandProvider.get();
        command.runInTransaction();
    }

    private void createDeliverDataSetupCommand() {
        CreateDeliverDataSetupCommand createDeliverDataSetupCommand = this.createDeliverDataSetupCommandProvider.get();
        createDeliverDataSetupCommand.runInTransaction();
    }

    private void setupFirmwareManagementCommand() {
        if (!skipFirmwareManagementData) {
            SetupFirmwareManagementCommand setupFirmwareManagementCommand = this.setupFirmwareManagementCommandProvider.get();
            setupFirmwareManagementCommand.runInTransaction();
        }
    }

    private void createImportersCommand() {
        CreateImportersCommand importersCommand = this.createImportersCommandProvider.get();
        importersCommand.setAppServerName(this.comServerName);
        importersCommand.runInTransaction();
    }

    private void createDataLoggerSetupCommand() {
        CreateDataLoggerSetupCommand createDataLoggerSetupCommand = this.createDataLoggerSetupCommandProvider.get();
        createDataLoggerSetupCommand.setDataLoggerMrid("DL099000000000");
        createDataLoggerSetupCommand.setDataLoggerSerial("099000000000");
        createDataLoggerSetupCommand.setNumberOfSlaves(1);
        createDataLoggerSetupCommand.runInTransaction();
    }
}
