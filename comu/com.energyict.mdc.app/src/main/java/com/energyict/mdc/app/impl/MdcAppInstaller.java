package com.energyict.mdc.app.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.energyict.mdc.app.MdcAppService;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.monitor.app.MdcMonitorAppService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.app.install", service = {MdcAppInstaller.class}, property = "name=" + MdcAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class MdcAppInstaller {

    private final Logger logger = Logger.getLogger(MdcAppInstaller.class.getName());
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    // depencs on these to be able to assign their privileges
    private volatile AppService appService;
    private volatile IssueService issueService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile SchedulingService schedulingService;
    private volatile ValidationService validationService;
    private volatile YellowfinService yellowfinService;
    private volatile BpmService bpmService;
    private volatile DataExportService dataExportService;
    private volatile FileImportService fileImportService;
    private volatile FirmwareService firmwareService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MdcMonitorAppService mdcMonitorAppService;
    private volatile CommandRuleService commandRuleService;


    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", "MDA"), dataModel, Installer.class, ImmutableMap.of(version(10, 2), UpgraderV10_2.class));
    }

    public static class Installer implements FullInstaller {

        private final UserService userService;

        @Inject
        public Installer(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            doTry(
                    "Create default roles for MDA",
                    this::createDefaultRoles,
                    logger
            );
            doTry(
                    "Assign privileges to roles for MDA",
                    this::assignPrivilegesToDefaultRoles,
                    logger
            );
        }

        public void createDefaultRoles() {
            userService.createGroup(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.Roles.METER_EXPERT.description());
            userService.createGroup(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.Roles.METER_OPERATOR.description());
            userService.createGroup(MdcAppService.Roles.REPORT_VIEWER.value(), MdcAppService.Roles.REPORT_VIEWER.description());
        }

        public void assignPrivilegesToDefaultRoles() {
            String[] privilegesMeterExpert = getPrivilegesMeterExpert();

            userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_OPERATOR.value(), MdcAppService.APPLICATION_KEY, getPrivilegesMeterOperator());
            userService.grantGroupWithPrivilege(MdcAppService.Roles.METER_EXPERT.value(), MdcAppService.APPLICATION_KEY, privilegesMeterExpert);
            userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdcAppService.APPLICATION_KEY, privilegesMeterExpert);
            userService.grantGroupWithPrivilege(MdcAppService.Roles.REPORT_VIEWER.value(), MdcAppService.APPLICATION_KEY, getPrivilegesReportViewer());
        }

        private String[] getPrivilegesMeterExpert() {
            return MdcAppPrivileges.getApplicationPrivileges()
                    .stream()
                    .filter(p -> !p.equals(com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS))
                    .filter(p -> !p.equals(com.energyict.mdc.device.command.security.Privileges.Constants.APPROVE_COMMAND_LIMITATION_RULE))
                    .toArray(String[]::new);
        }


        private String[] getPrivilegesReportViewer() {
            return new String[]{
                    com.elster.jupiter.yellowfin.security.Privileges.Constants.VIEW_REPORTS
            };
        }

        private String[] getPrivilegesMeterOperator() {
            return new String[]{
                    //Assets inventory
                    com.energyict.mdc.device.data.security.Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT,
                    com.energyict.mdc.device.data.security.Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT,

                    //Business processes
                    com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_BPM,

                    //Communication
                    com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
                    com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_STATUS_COMMUNICATION_INFRASTRUCTURE,

                    //Data collection KPI
                    com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DATA_COLLECTION_KPI,

                    //Device communications
                    com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,

                    //Device data
                    com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,

                    //Device groups
                    com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL,

                    //CommandRules
                    com.energyict.mdc.device.command.security.Privileges.Constants.VIEW_COMMAND_LIMITATION_RULE,

                    //Device life cycle
                    com.energyict.mdc.device.lifecycle.config.Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE,

                    //Device master data
                    com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_MASTER_DATA,

                    //Device types
                    com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE,

                    //Devices
                    com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE,
                    com.energyict.mdc.device.data.security.Privileges.Constants.REMOVE_DEVICE,
                    com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
                    com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE,
                    com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS,

                    //Estimation
                    com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                    com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                    com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                    com.elster.jupiter.estimation.security.Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,

                    //Export
                    com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                    com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                    com.elster.jupiter.export.security.Privileges.Constants.VIEW_HISTORY,

                    //Firmware campaigns
                    com.energyict.mdc.firmware.security.Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN,

                    //Import
                    com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                    com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_HISTORY,

                    //Issues
                    com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE,

                    //Issues configuration
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ASSIGNMENT_RULE,
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE,

                    //Relative periods
                    com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                    //Usage points
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,

                    //User tasks
                    com.elster.jupiter.bpm.security.Privileges.Constants.EXECUTE_TASK,
                    com.elster.jupiter.bpm.security.Privileges.Constants.VIEW_TASK,
                    com.elster.jupiter.bpm.security.Privileges.Constants.ASSIGN_TASK,

                    //Validation
                    com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                    com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                    com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,

                    //Shared communication schedule
                    com.energyict.mdc.scheduling.security.Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE,

                    //service category
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                    //metrology configuration
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,

                    //Service calls
                    com.elster.jupiter.servicecall.security.Privileges.Constants.CHANGE_SERVICE_CALL_STATE
            };
        }
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }


    @Reference
    public void setBpmService(BpmService appService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
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
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMdcMonitorAppService(MdcMonitorAppService mdcMonitorAppService) {
        this.mdcMonitorAppService = mdcMonitorAppService;
    }

    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

}