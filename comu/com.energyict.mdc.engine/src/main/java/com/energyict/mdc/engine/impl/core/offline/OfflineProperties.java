/*
 * PropertiesLoader.java
 *
 * Created on 14 augustus 2003, 11:21
 */

package com.energyict.mdc.engine.impl.core.offline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Koen
 */
public class OfflineProperties {

    private static final Log logger = LogFactory.getLog(OfflineProperties.class);
    private static final String PROPERTIES_FILE = "comservermobile.properties";

    private static OfflineProperties instance = null;
    Properties properties = null;

    /**
     * Creates a new instance of PropertiesLoader
     */
    private OfflineProperties() {
        properties = loadProperties();
    }

    public static OfflineProperties getInstance() {
        if (instance == null) {
            instance = new OfflineProperties();
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
            inputStream = new FileInputStream("conf/" + PROPERTIES_FILE);
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