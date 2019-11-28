package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OfflinePropertiesProvider {

    private static final Log logger = LogFactory.getLog(OfflineComServerProperties.class);

    private static OfflinePropertiesProvider instance;

    private Properties properties;

    private OfflinePropertiesProvider() {
        properties = new Properties();
        try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("offline.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static OfflinePropertiesProvider getInstance() {
        if (instance == null){
            instance = new OfflinePropertiesProvider();
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getConnexoVersion() {
        return getProperties().getProperty("connexo.version");
    }
}
