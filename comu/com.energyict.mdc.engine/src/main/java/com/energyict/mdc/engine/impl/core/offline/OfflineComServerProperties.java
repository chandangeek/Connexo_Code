/*
 * PropertiesLoader.java
 *
 * Created on 14 augustus 2003, 11:21
 */

package com.energyict.mdc.engine.impl.core.offline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * @author Koen
 */
public class OfflineComServerProperties {

    private static final Log logger = LogFactory.getLog(OfflineComServerProperties.class);
    private static final String PROPERTIES_FILE = "comservermobile.properties";

    private static OfflineComServerProperties instance = null;
    Properties properties = null;

    /**
     * Creates a new instance of PropertiesLoader
     */
    private OfflineComServerProperties() {
        properties = loadProperties();
    }

    public static OfflineComServerProperties getInstance() {
        if (instance == null) {
            instance = new OfflineComServerProperties();
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    private Properties loadProperties() {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            File connexoHome = new File(System.getProperty("connexo.home", ""));
            File propertiesFile = new File(connexoHome.getAbsoluteFile(), "conf/" + PROPERTIES_FILE);
            inputStream = new FileInputStream(propertiesFile);
            properties.load(inputStream);
            return properties;
        } catch (FileNotFoundException e) {
            return properties;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return properties;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}