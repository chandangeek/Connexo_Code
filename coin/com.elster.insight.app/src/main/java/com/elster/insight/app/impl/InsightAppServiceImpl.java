package com.elster.insight.app.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

//import com.elster.jupiter.license.License;
import com.elster.insight.app.InsightAppService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

@Component(
        name = "com.elster.insight.app",
        service = {InsightAppService.class, ApplicationPrivilegesProvider.class, TranslationKeyProvider.class},
//        property = "name=" + InsightAppService.COMPONENTNAME,
        immediate = true)
public class InsightAppServiceImpl implements InsightAppService, ApplicationPrivilegesProvider, TranslationKeyProvider {
    private static final Logger LOGGER = Logger.getLogger(InsightAppServiceImpl.class.getName());
    public static final String HTTP_RESOURCE_ALIAS = "/insight";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/insight";

    public static final String APP_KEY = "INS";
    public static final String APP_NAME = "Insight";
    public static final String APP_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile UserService userService;
    //private volatile License license;
    
    public InsightAppServiceImpl() {
    }

    @Inject
    public InsightAppServiceImpl(UserService userService, BundleContext context) {
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
       HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
//        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/home/kurtk/Projects/git/insight/com.elster.jupiter.insight.app/src/main/web/js/insight", new FileResolver(),
//                new DefaultStartPage(APP_NAME));
        App app = new App(APP_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, user -> isAllowed(user));

        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }
    
//    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
//    public void setLicense(License license) {
//        this.license = license;
//    }
    
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private boolean isAllowed(User user) {
   //     return true;
    	List<? super Privilege> appPrivileges = getDBApplicationPrivileges();
        return user.getPrivileges(APPLICATION_KEY).stream().anyMatch(appPrivileges::contains);
    }
    
    private List<? super Privilege> getDBApplicationPrivileges() {
        return userService.getPrivileges(APPLICATION_KEY);
    }
    
    @Override
    public List<String> getApplicationPrivileges() {
        return InsightAppPrivileges.getApplicationPrivileges();
    }
    
	@Override
	public String getApplicationName() {
		// TODO Auto-generated method stub
		return APPLICATION_KEY;
	}

    @Override
    public String getComponentName() {
        //TODO: Change to COMPONENT_NAME after pulling the extjs stuff into a new bundle, etc.
        return APPLICATION_KEY;
    }
    
    @Override
    public Layer getLayer() {
        return Layer.REST;
    }
    
    @Override
    public List<TranslationKey> getKeys() {
        try {
            return SimpleTranslationKey.loadFromInputStream(this.getClass().getClassLoader().getResourceAsStream("i18n.properties"));
        } catch (IOException e) {
            LOGGER.severe("Failed to load translations for the '" + COMPONENTNAME + "' component bundle.");
        }
        return null;
        
//        List<TranslationKey> translationKeys = new ArrayList<>();
//        translationKeys.add(new SimpleTranslationKey(APPLICATION_KEY, APPLICATION_NAME));
//        return translationKeys;
        
    }

    
//    @Override
//    public void install() {
//        assignPrivilegesToDefaultRoles();
//    }
//
//    @Override
//    public List<String> getPrerequisiteModules() {
//        return Arrays.asList(UserService.COMPONENTNAME, MeteringService.COMPONENTNAME);
//    }
//
//
//
//    private void assignPrivilegesToDefaultRoles() {
//        List<String> availablePrivileges = getApplicationPrivileges();
//        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, MeteringService.COMPONENTNAME, availablePrivileges.stream().toArray(String[]::new));
//        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, MeteringService.COMPONENTNAME, availablePrivileges.stream().toArray(String[]::new));
//    }
//
//
//    private List<String> getApplicationPrivileges(){
//        return Arrays.asList(
//            //validation
//            com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
//            com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
//            com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL,
//            com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
//            com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,
//            //com.elster.jupiter.metering.security - usage points
//            com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_ANY,
//            com.elster.jupiter.metering.security.Privileges.Constants.ADMIN_OWN,
//            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_ANY,
//            com.elster.jupiter.metering.security.Privileges.Constants.BROWSE_OWN);
//    }


}
