/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.bootstrap.BootstrapService;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
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
        info.osName = getOSName();
        info.osArch = System.getProperty("os.arch");
        info.timeZone = System.getProperty("user.timezone");
        info.totalMemory = Runtime.getRuntime().totalMemory() / 1024;
        info.freeMemory = Runtime.getRuntime().freeMemory() / 1024;
        info.usedMemory = info.totalMemory - info.freeMemory;
        info.lastStartedTime = this.lastStartedTime;
        info.serverUptime = System.currentTimeMillis() - this.lastStartedTime;
        info.environmentParameters = getEnvironmentParameters();
        return info;
    }

    private Map<String, String> getEnvironmentParameters() {
        Map<String, String> environment = new HashMap<>();

        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            String value = env.get(envName);
            if (value.toLowerCase().contains("pass")){
                environment.put(value, "********************");
            } else {
                environment.put(envName, value);
            }
        }

        return environment;
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
