package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.config.HsmJssConfigLoader;
import com.elster.jupiter.hsm.impl.context.HsmClassLoaderHelper;
import com.elster.jupiter.hsm.impl.resources.HsmRefreshableFileResourceBuilder;
import com.elster.jupiter.hsm.impl.resources.HsmRefreshableResourceBuilder;
import com.elster.jupiter.hsm.model.HsmBaseException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public final class HsmJssLoader {

    private static final Logger LOG = LoggerFactory.getLogger(HsmJssLoader.class);

    private static HsmJssLoader instance;

    private HsmRefreshableResourceBuilder<File> jssFileLoader;
    private Long loadTime;
    private RawConfiguration rawConfiguration;

    private boolean jssStarted = false;

    private HsmJssLoader(HsmRefreshableResourceBuilder<File> jssFileLoader) throws HsmBaseException {
        if (Objects.isNull(jssFileLoader)){
            throw new HsmBaseException("Could not instantiate resource re-loader based on null resource loader");
        }
        this.jssFileLoader = jssFileLoader;
        load();
    }

    public synchronized RawConfiguration load() throws HsmBaseException {
        Long timeStamp = jssFileLoader.timeStamp();
        if (loadTime == null) {
            start(jssFileLoader.build());
            LOG.debug("Initialising JSS");
            loadTime = timeStamp;
        }
        if (loadTime < timeStamp) {
            LOG.debug("Restarting JSS");
            shutdown();
            start(jssFileLoader.build());
            loadTime = timeStamp;
        }
        return rawConfiguration;
    }

    private void setupContext() {
        HsmClassLoaderHelper.setClassLoader();
        configureLogger();
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
        } catch (Exception e) {
            String msg = "Unable to re-configure logger";
            System.out.println(msg +  e.getMessage() + " stackTrace:");
            LOG.warn(msg, e);
        }
    }

    private void start(File jssFile) {
        setupContext();
        try {
            this.rawConfiguration = new HsmJssConfigLoader().load(jssFile);
            JSSRuntimeControl.initialize();
            JSSRuntimeControl.newConfiguration(rawConfiguration);
            jssStarted = true;
        } catch (Throwable e) {
            LOG.error("Unable to initialize JSS", e);
            throw (e);
        }
    }

    private void shutdown() {
        if (jssStarted = true) {
            JSSRuntimeControl.shutdown();
            jssStarted = false;
        }
    }


    public static HsmJssLoader getInstance(HsmRefreshableFileResourceBuilder newLoader) throws HsmBaseException {
        if (instance == null || !instance.jssFileLoader.equals(newLoader)) {
            instance =  new HsmJssLoader(newLoader);
        }
        return instance;
    }
}
