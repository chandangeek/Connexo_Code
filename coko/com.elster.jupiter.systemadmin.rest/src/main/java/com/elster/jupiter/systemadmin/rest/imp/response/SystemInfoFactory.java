/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.bootstrap.BootstrapService;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class SystemInfoFactory {

    private long lastStartedTime;
    private BundleContext bundleContext;

    @Inject
    public SystemInfoFactory(@Named("LAST_STARTED_TIME") long lastStartedTime, BundleContext bundleContext) {
        this.lastStartedTime = lastStartedTime;
        this.bundleContext = bundleContext;
    }

    public SystemInfo asInfo() {
        SystemInfo info = new SystemInfo();
        info.jre = System.getProperty("java.runtime.name") + "(build " + System.getProperty("java.runtime.version") + ")";
        info.jvm = System.getProperty("java.vm.name") + "(build " + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.info" ) + ")";
        info.javaHome = System.getProperty("java.home");
        info.javaClassPath = System.getProperty("java.class.path");
        info.osName = getOSName();
        info.osArch = System.getProperty("os.arch");
        info.timeZone = System.getProperty("user.timezone");
        info.numberOfProcessors = Runtime.getRuntime().availableProcessors();
        info.totalMemory = Runtime.getRuntime().totalMemory() / 1024;
        info.freeMemory = Runtime.getRuntime().freeMemory() / 1024;
        info.usedMemory = info.totalMemory - info.freeMemory;
        info.lastStartedTime = this.lastStartedTime;
        info.serverUptime = System.currentTimeMillis() - this.lastStartedTime;
        info.dbConnectionUrl = this.bundleContext.getProperty(BootstrapService.JDBC_DRIVER_URL);
        info.dbUser = this.bundleContext.getProperty(BootstrapService.JDBC_USER);
        info.dbMaxConnectionsNumber = getPropertyOrDefault(this.bundleContext.getProperty(BootstrapService.JDBC_POOLMAXLIMIT), BootstrapService.JDBC_POOLMAXLIMIT_DEFAULT);
        info.dbMaxStatementsPerRequest = getPropertyOrDefault(this.bundleContext.getProperty(BootstrapService.JDBC_POOLMAXSTATEMENTS), BootstrapService.JDBC_POOLMAXSTATEMENTS_DEFAULT);
        info.environmentParameters = getEnvironmentParameters();
        return info;
    }

    private String getEnvironmentParameters() {
        StringBuilder environment = new StringBuilder();

        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            environment.append("<li>");
            environment.append(env).append("=");
            String value = env.get(envName);
            if (value.toLowerCase().contains("pass")){
                environment.append("********************");
            } else {
                environment.append(value);
            }
            environment.append("</li>");
        }

        return environment.toString();
    }

    private String getPropertyOrDefault(String property, String defaultValue) {
        return property != null ? property : defaultValue;
    }

    private String getOSName() {
       String osName = System.getProperty("os.name");
       if(osName.equalsIgnoreCase("Windows XP")) {
           osName = OSInfo.getOs().name().concat(" ").concat(OSInfo.getOs().getVersion());
       }
       return osName;
    }
}
