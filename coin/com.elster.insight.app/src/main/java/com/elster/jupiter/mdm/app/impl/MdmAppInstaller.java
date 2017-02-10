/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
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
    private volatile UsagePointDataModelService usagePointDataModelService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Insight","DMA"), dataModel, Installer.class, ImmutableMap.of(version(10, 3), UpgraderV10_3.class));
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
            userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, getPrivilegesDataExpert());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_EXPERT.value(), MdmAppService.APPLICATION_KEY, getPrivilegesDataExpert());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.DATA_OPERATOR.value(), MdmAppService.APPLICATION_KEY, getPrivilegesDataOperator());
        }

        private String[] getPrivilegesDataExpert() {
            return MdmAppPrivileges.getApplicationAllPrivileges().stream().toArray(String[]::new);
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
                    com.elster.jupiter.estimation.security.Privileges.Constants.ESTIMATE_MANUAL
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setUsagePointDataModelService(UsagePointDataModelService usagePointDataModelService) {
        this.usagePointDataModelService = usagePointDataModelService;
    }
}
