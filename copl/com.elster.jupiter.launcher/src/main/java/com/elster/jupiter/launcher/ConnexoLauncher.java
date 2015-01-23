package com.elster.jupiter.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 21/01/2015
 * Time: 16:15
 */
public class ConnexoLauncher {


    private static Logger logger = Logger.getLogger(ConnexoLauncher.class.getName());
    private static Framework framework;

    public static void main(String[] args) {
        File installDir = determineInstallDir();
        if (installDir == null) {
            logger.severe("Could not determine valid installation directory !!");
            System.exit(1);
        }
        registerShutdownHook();
        Map<String, String> configMap = null;
        try {
            configMap = loadConfig(installDir);
        } catch (IOException e) {
            logger.severe("Could not load config file ! " + e.getLocalizedMessage());
            System.exit(2);
        }
        boolean interactive = false;
        boolean install = false;
        if (args.length > 0) {
            for (String arg : args) {
                if ("--interactive".equalsIgnoreCase(arg)) {
                    interactive = true;
                }
                if ("--install".equals(arg)) {
                    install = true;
                }
            }
        }
        if (!interactive) {
            configMap.put("gosh.args", "--nointeractive");
        }
        startFramework(configMap, installDir);
        if (install) {
            initAll();
            stopFramework();
        }


    }

    private static void stopFramework() {
        try {
            if (framework != null) {
                framework.stop();
                framework.waitForStop(0);
            }
        } catch (Exception ex) {
            System.err.println("Error stopping framework: " + ex);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
            public void run() {
                stopFramework();
            }
        });
    }

    private static void startFramework(Map<String, String> configMap, File rootDir) {
        try {
            FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
            framework = frameworkFactory.newFramework(configMap);
            framework.init();

            BundleContext bundleContext = framework.getBundleContext();
            ArrayList<Bundle> installed = new ArrayList<Bundle>();
            for (URL url : findBundles(rootDir)) {
                logger.info("Installing bundle [" + url + "]");
                Bundle bundle = bundleContext.installBundle(url.toExternalForm());
                installed.add(bundle);
            }

            for (Bundle bundle : installed) {
                if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
                    logger.info("Starting bundle [" + bundle.getSymbolicName() + "]");
                    bundle.start();
                }
            }
            framework.start();
            logger.info("OSGi framework started");
        } catch (BundleException e) {
            logger.severe("Could start the OSGi framework ! " + e.getLocalizedMessage());
            System.exit(3);
        }
    }

    private static void initAll() {
        ServiceReference serviceReference = framework.getBundleContext().getServiceReference("com.elster.jupiter.install.InstallerService");
        if (serviceReference != null) {
            Object installService = framework.getBundleContext().getService(serviceReference);
            try {
                installService.getClass().getMethod("initAll").invoke(installService);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.severe("Caught exception while installing " + e.getLocalizedMessage());
            }
        }
    }


    private static Map<String, String> loadConfig(File rootDir) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(rootDir, "conf/config.properties")));

        HashMap<String, String> map = new HashMap<>();
        for (Object key : props.keySet()) {
            map.put(key.toString(), props.getProperty(key.toString()));
        }
        return map;
    }

    private static List<URL> findBundles(File rootDir) {
        File bundlesDir = new File(rootDir, "bundles");
        ArrayList<URL> list = new ArrayList<>();
        for (File file : bundlesDir.listFiles((dir, name) -> name.endsWith(".jar"))) {
            try {
                list.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                logger.warning("While listing bundles, caught exception " + e.getLocalizedMessage());
            }
        }

        return list;
    }

    private static File determineInstallDir() {
        File cwd = new File(System.getProperty("user.dir"));
        if (isValidInstallDir(cwd.getParentFile())) {
            return cwd.getParentFile();
        } else if (isValidInstallDir(cwd)) {
            return cwd;
        } else {
            URL location = ConnexoLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            try {
                File classLocation = new File(location.toURI()).getParentFile();
                if (isValidInstallDir(classLocation)) {
                    return classLocation;
                }
            } catch (URISyntaxException e) {
                //igore hmhm
            }
        }
        return null;


    }

    private static boolean isValidInstallDir(File installDir) {
        if (installDir.isDirectory()) {
            if (new File(installDir, "conf").exists()) {
                if (new File(installDir, "conf").isDirectory()) {
                    if (new File(installDir, "bundles").exists()) {
                        if (new File(installDir, "bundles").isDirectory()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}