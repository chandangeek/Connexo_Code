package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.impl.config.HsmJssConfigLoader;
import com.elster.jupiter.hsm.model.HsmBaseException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.internal.spring.JssEmbeddedRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class HsmReloadableJssConfigResource implements HsmReloadableResource<RawConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(HsmReloadableJssConfigResource.class);

    private final File jssJsonFile;
    private boolean jssStarted = false;

    public HsmReloadableJssConfigResource(File jssJsonFile) {
        this.jssJsonFile = jssJsonFile;
    }

    @Override
    public RawConfiguration load() throws HsmBaseException {
        logger.debug("JSS load called");
        return start();
    }

    @Override
    public RawConfiguration reload() throws HsmBaseException {
        logger.debug("JSS reload called");
        shutdown();
        return start();
    }

    @Override
    public Long timeStamp() {
        return jssJsonFile.lastModified();
    }

    private void setupContext() {
        setClassLoader();
        configureLogger();
    }

    private void setClassLoader() {
        ClassLoader hsmClassLoader = JssEmbeddedRuntimeConfig.class.getClassLoader();
        URLClassLoader appClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader(appClassLoader.getURLs(), hsmClassLoader);
        Thread.currentThread().setContextClassLoader(loader);
    }

    /**
     * This will try to re-configure logger used by JSS and underlying libs
     */
    private void configureLogger() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            String logbackFile = "logback.xml";
            URL resource = Thread.currentThread().getContextClassLoader().getResource(logbackFile);
            if (resource == null) {
                // nothing to do, simply we will not try to reconfigure logger
                return;
            }
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(resource);
        } catch (JoranException e) {
            String msg = "Unable to re-configure logger";
            System.out.println(msg + e.getMessage() + " stackTrace:"); e.printStackTrace(System.out);
            logger.warn(msg, e);
        }
    }

    private RawConfiguration start() throws HsmBaseException {
        logger.debug("Starting JSS ...");
        RawConfiguration rawConfiguration = new HsmJssConfigLoader().load(jssJsonFile);
        setupContext();
        JSSRuntimeControl.initialize();
        JSSRuntimeControl.newConfiguration(rawConfiguration);
        jssStarted = true;
        logger.debug("JSS started");
        return rawConfiguration;
    }

    private void shutdown() {
        logger.debug("Stopping JSS ...");
        if (jssStarted = true) {
            JSSRuntimeControl.shutdown();
            jssStarted = false;
            logger.debug("JSS stopped");
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HsmReloadableJssConfigResource)) {
            return false;
        }

        HsmReloadableJssConfigResource that = (HsmReloadableJssConfigResource) o;

        return jssJsonFile.equals(that.jssJsonFile);
    }

    @Override
    public int hashCode() {
        return jssJsonFile.hashCode();
    }
}
