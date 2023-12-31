/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.mdm.app.install", service = {MdmAppInstaller.class}, property = "name=" + MdmAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class MdmAppInstaller {

    private final Logger logger = Logger.getLogger(MdmAppInstaller.class.getName());
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;
    private volatile ValidationService validationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile UsagePointService usagePointService;
    private volatile IssueService issueService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Insight", "DMA"), dataModel, Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 3), UpgraderV10_3.class)
                        .put(version(10, 4, 37), UpgraderV10_4_37.class)
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 7, 4), UpgraderV10_7_4.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .put(version(10, 9, 19), UpgraderV10_9_19.class)
                        .build());
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setUsagePointService(UsagePointService usagePointService) {
        this.usagePointService = usagePointService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    static class Installer implements FullInstaller {
        private final UserService userService;

        @Inject
        Installer(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            doTry(
                    "Create default roles for MDMAPP",
                    this::createDefaultRoles,
                    logger
            );
            doTry(
                    "Create default roles for MDMAPP",
                    this::assignPrivilegesToDefaultRoles,
                    logger
            );
        }

        private void createDefaultRoles() {
            userService.createGroup(MdmAppService.Roles.DATA_EXPERT.value(), MdmAppService.Roles.DATA_EXPERT.description());
            userService.createGroup(MdmAppService.Roles.DATA_OPERATOR.value(), MdmAppService.Roles.DATA_OPERATOR.description());
        }

        private void assignPrivilegesToDefaultRoles() {
            userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, getPrivilegesBatchExecutor());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_EXPERT.value(), MdmAppService.APPLICATION_KEY, getPrivilegesDataExpert());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_OPERATOR.value(), MdmAppService.APPLICATION_KEY, getPrivilegesDataOperator());
        }

        private String[] getPrivilegesBatchExecutor() {
            return MdmAppPrivileges.getApplicationAllPrivileges().stream().toArray(String[]::new);
        }

        private String[] getPrivilegesDataExpert() {
            return MdmAppPrivileges.getApplicationAllPrivileges().stream()
                    .filter(name -> !name.equals(com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES))
                    .toArray(String[]::new);
        }

        private String[] getPrivilegesDataOperator() {
            return new String[]{
                    //usage point
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_READINGTYPE,
                    //validation
                    com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                    com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
                    com.elster.jupiter.mdm.usagepoint.config.security.Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION,

                    //metrology configuration
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION,

                    //service category
                    com.elster.jupiter.metering.security.Privileges.Constants.VIEW_SERVICECATEGORY,

                    com.elster.jupiter.servicecall.security.Privileges.Constants.CHANGE_SERVICE_CALL_STATE,

                    //Export
                    com.elster.jupiter.export.security.Privileges.Constants.RUN_DATA_EXPORT_TASK,
                    com.elster.jupiter.export.security.Privileges.Constants.VIEW_DATA_EXPORT_TASK,
                    com.elster.jupiter.export.security.Privileges.Constants.VIEW_HISTORY,

                    //Relative periods
                    com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,

                    //Import services
                    com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES,
                    com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_HISTORY,
                    com.elster.jupiter.fileimport.security.Privileges.Constants.IMPORT_FILE,

                    // Usage point life cycle
                    com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW,
                    com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_1,
                    com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_2,
                    com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_3,
                    com.elster.jupiter.usagepoint.lifecycle.config.Privileges.Constants.EXECUTE_TRANSITION_4,

                    //Usage point groups
                    com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL,

                    //estimation
                    com.elster.jupiter.estimation.security.Privileges.Constants.RUN_ESTIMATION_TASK,
                    com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
                    com.elster.jupiter.estimation.security.Privileges.Constants.VIEW_ESTIMATION_TASK,
                    com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL,
                    com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_WITH_RULE,
                    com.elster.jupiter.estimation.security.Privileges.Constants.EDIT_WITH_ESTIMATOR,

                    //data quality kpi
                    com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION,
                    com.elster.jupiter.dataquality.security.Privileges.Constants.VIEW_DATA_QUALITY_RESULTS,

                    //Issues
                    com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE,
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE,

                    //issue configuration
                    com.elster.jupiter.issue.security.Privileges.Constants.ADMINISTRATE_CREATION_RULE,
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ASSIGNMENT_RULE,
                    com.elster.jupiter.issue.security.Privileges.Constants.VIEW_CREATION_RULE,

                    //WebServices
                    com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.VIEW_WEB_SERVICES,
                    com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.VIEW_HISTORY_WEB_SERVICES
            };
        }
    }
}
