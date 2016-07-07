package com.elster.jupiter.mdm.app.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.app.MdmAppService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.mdm.app.install", service = {MdmAppInstaller.class}, property = "name=" + MdmAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class MdmAppInstaller {

    private final Logger logger = Logger.getLogger(MdmAppInstaller.class.getName());
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;
    private volatile ValidationService validationService;
    private volatile CustomPropertySetService customPropertySetService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Insight","DMA"), dataModel, Installer.class, Collections.emptyMap());
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
            userService.createGroup(MdmAppService.Roles.METER_EXPERT.value(), MdmAppService.Roles.METER_EXPERT.description());
            userService.createGroup(MdmAppService.Roles.METER_OPERATOR.value(), MdmAppService.Roles.METER_OPERATOR.description());
        }

        private void assignPrivilegesToDefaultRoles() {
            userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MdmAppService.APPLICATION_KEY, getPrivilegesMeterExpert());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.METER_EXPERT.value(), MdmAppService.APPLICATION_KEY, getPrivilegesMeterExpert());
            userService.grantGroupWithPrivilege(MdmAppService.Roles.METER_OPERATOR.value(), MdmAppService.APPLICATION_KEY, getPrivilegesMeterOperator());

            //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
            userService.getUser(1)
                    .ifPresent(u -> u.join(userService.getGroups()
                            .stream()
                            .filter(e -> e.getName().equals(MdmAppService.Roles.METER_EXPERT.value()))
                            .findFirst()
                            .get()));
        }

        private String[] getPrivilegesMeterExpert() {
            return MdmAppPrivileges.getApplicationAllPrivileges().stream().toArray(String[]::new);
        }

        private String[] getPrivilegesMeterOperator() {
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

                    //Relative periods
                    com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD
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

}
