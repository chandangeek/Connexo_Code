package com.energyict.mdc.engine.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.issues.IssueService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.engine.impl.LaunchComServer", service = LaunchComServer.class,
        property = {"osgi.command.scope=mdc",
                "osgi.command.function=launchComServer",
                "osgi.command.function=stopComServer",
                "osgi.command.function=lcs",
                "osgi.command.function=scs"},
        immediate = true)
public class LaunchComServer implements EngineService.DeactivationNotificationListener {

    private static final String PROPERTY_NAME_AUTO_START = "com.energyict.comserver.autostart";

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile EngineService engineService;

    private boolean autoStart;
    private ComServerLauncher launcher;


    @Activate
    public void activate(BundleContext context) {
        System.out.println("Activating ComServerLauncher test");
        autoStart = Boolean.parseBoolean(getOptionalProperty(context, PROPERTY_NAME_AUTO_START, "false"));
        if (autoStart) {
            launchComServer();
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("Deactivating ComServerLauncher test");
        if (autoStart) {
            stopComServer();
        }
    }

    private String getOptionalProperty(BundleContext context, String property, String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value;
    }

    public void stopComServer() {
        if (this.launcher != null) {
            this.engineService.unregister(this);
            System.out.println("Shutting down the ComServer, this may take a while ...");
            this.launcher.stopComServer();
            this.launcher = null;
            System.out.println("ComServer has been shut down...");
        } else {
            System.out.println("No ComServer available to shut down!");
            System.out.println("*** Use 'lsc' to start a ComServer on your local machine.");
        }
    }

    public void scs() {
        stopComServer();
    }

    public void lcs() {
        launchComServer();
    }

    public void launchComServer() {
        if (this.launcher == null) {
            System.out.println("Starting ComServer");
            try {
                this.engineService.register(this);
                this.launcher = new ComServerLauncher(getServiceProvider());
                launcher.startComServer();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("There is already a ComServer running on this machine.");
        }
    }

    private ServiceProvider getServiceProvider() {
        return ServiceProvider.instance.get();
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    @Override
    public void engineServiceDeactivationStarted() {
        this.stopComServer();
    }

}