/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.bootstrap.BootstrapService;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SystemInfoFactory {

    public static final String JAVAX_NET_SSL_TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";
    public static final String JAVAX_NET_SSL_TRUST_STORE = "javax.net.ssl.trustStore";
    public static final String JAVAX_NET_SSL_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String JAVA_TRUSTSTORE_DEFAULT_PASSWORD = "changeit";
    public static final String JAVA_HOME = "java.home";
    public static final String CACERTS_SUBPATH = "/lib/security/cacerts";
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
        info.trustStoreContent = getTrustStoreContent();
        return info;
    }

    private Map<String, Map<String,String>> getTrustStoreContent() {
        Map<String, Map<String,String>> info = new HashMap<>();

        HashMap<String, String> trustDetails = new HashMap<>();

        String trustStore = System.getProperty(JAVAX_NET_SSL_TRUST_STORE);
        if (trustStore==null || trustStore.isEmpty()) {
            trustStore = System.getProperty(JAVA_HOME)+ CACERTS_SUBPATH.replace('/', File.separatorChar);
            trustDetails.put("Property not set, using default",trustStore);
        }

        String trustStoreType = System.getProperty(JAVAX_NET_SSL_TRUST_STORE_TYPE);
        if (trustStoreType==null || trustStoreType.isEmpty()){
            trustStoreType = KeyStore.getDefaultType();
        }

        String trustStorePassword = System.getProperty(JAVAX_NET_SSL_TRUST_STORE_PASSWORD);
        if (trustStorePassword==null || trustStorePassword.isEmpty()){
            trustStorePassword = JAVA_TRUSTSTORE_DEFAULT_PASSWORD;
        }

        trustDetails.put(JAVAX_NET_SSL_TRUST_STORE, trustStore );
        trustDetails.put(JAVAX_NET_SSL_TRUST_STORE_TYPE, trustStoreType );
        info.put("Parameters", trustDetails);

        try {
            FileInputStream is = new FileInputStream(trustStore);
            KeyStore keystore = KeyStore.getInstance(trustStoreType);
            keystore.load(is, trustStorePassword.toCharArray());
            Enumeration<String> enumeration = keystore.aliases();

            while (enumeration.hasMoreElements()){
                String alias = enumeration.nextElement();
                X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

                HashMap<String, String> certificateDetails = new HashMap<>();
                certificateDetails.put("Subject", cert.getSubjectDN().toString());
                certificateDetails.put("Issuer", cert.getIssuerDN().toString());
                certificateDetails.put("SerialNumber", "0x"+cert.getSerialNumber().toString(16));
                certificateDetails.put("Expires", cert.getNotAfter().toString());

                info.put(alias, certificateDetails);
            }
        } catch (Exception e) {
            HashMap<String, String> errorDetails = new HashMap<>();
            errorDetails.put("Message: ", e.getLocalizedMessage());
            if (e.getCause()!=null){
                errorDetails.put("Caused by: ", e.getCause().getLocalizedMessage());
            }
            info.put("Exception", errorDetails);
        }

        return info;
    }

    private Map<String, String> getEnvironmentParameters() {
        Map<String, String> environment = new HashMap<>();

        Properties properties= System.getProperties();
        for (Object key : properties.keySet()) {
            String envName = (String) key;
            String value = (String) properties.get(envName);
            if (envName.toLowerCase().contains("pass")){
                environment.put(envName, "********************");
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
