package com.elster.insight.app.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.insight.app.InsightAppService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(name = "com.elster.insight.app.install", service = {InstallService.class}, property = "name=" + InsightAppService.COMPONENTNAME, immediate = true)
@SuppressWarnings("unused")
public class InsightAppInstaller implements InstallService {

    private final Logger logger = Logger.getLogger(InsightAppInstaller.class.getName());
    private volatile UserService userService;

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "MTR", "UPC", "VAL", "CPS");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void createDefaultRoles() {
        try {
            userService.createGroup(InsightAppService.Roles.METER_EXPERT.value(), InsightAppService.Roles.METER_EXPERT.description());
            userService.createGroup(InsightAppService.Roles.METER_OPERATOR.value(), InsightAppService.Roles.METER_OPERATOR.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        List<String> privilegesMeterExpert =  getPrivilegesMeterExpert();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, InsightAppService.APPLICATION_KEY, privilegesMeterExpert.stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, InsightAppService.APPLICATION_KEY, privilegesMeterExpert.stream().toArray(String[]::new));

        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_OPERATOR.value(), InsightAppService.APPLICATION_KEY, getPrivilegesMeterOperator().stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(InsightAppService.Roles.METER_EXPERT.value(), InsightAppService.APPLICATION_KEY, privilegesMeterExpert.stream().toArray(String[]::new));
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, InsightAppService.APPLICATION_KEY, privilegesMeterExpert.stream().toArray(String[]::new));

        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(InsightAppService.Roles.METER_EXPERT.value())).findFirst().get()));
        //TODO: workaround: attached Report viewer to user admin !!! to remove this line when the user can be created/added to system
//        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(InsightAppService.Roles.REPORT_VIEWER.value())).findFirst().get())); 
    }

    private List<String> getPrivilegesMeterExpert() {
        return InsightAppPrivileges.getApplicationPrivileges();
    }


    private List<String> getPrivilegesMeterOperator(){
    	  return Arrays.asList(
    		        
    	        	//com.elster.jupiter.metering.security - usage points
    	            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
    	            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN,
    	        		
    	            //validation
    	            com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,

    	            //metrology configuration
    	            com.elster.insight.usagepoint.config.Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG,
    	            
    	            //com.elster.jupiter.time
    	            com.elster.jupiter.time.security.Privileges.Constants.VIEW_RELATIVE_PERIOD,
    	            
    	            //com.elster.jupiter.cps
    	            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_1,
    	            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_2,
    	            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_3,
    	            com.elster.jupiter.cps.Privileges.Constants.VIEW_CUSTOM_PROPERTIES_4,

    	           	//import
    	        	com.elster.jupiter.fileimport.security.Privileges.Constants.VIEW_IMPORT_SERVICES
    	  );
    }
}
