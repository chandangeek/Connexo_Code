/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
import com.elster.jupiter.hsm.model.configuration.HsmLabelConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class HsmConfigurationPropFileImpl implements HsmConfiguration {

    public static final String HSM_CONFIG_JSS_INIT_FILE = "hsm.config.jss.init.file";
    public static final String HSM_CONFIG_LABEL_PREFIX = "hsm.config.label.";

    private final Properties properties;
    private final Map<String, String> importToHsmLabelMap = new HashMap<>();
    private final Map<String, HsmLabelConfiguration> labelToConfigMap = new HashMap<>();

    public HsmConfigurationPropFileImpl(@Nonnull String configFile) throws HsmBaseException {
        properties = new Properties();
        try (FileReader fr = new FileReader(new File(configFile))) {
            properties.load(fr);
            loadImportToHsmLabelMap();
        } catch (IOException e) {
            throw new HsmBaseException(e);
        }
    }

    public HsmConfigurationPropFileImpl(Properties properties) throws HsmBaseException {
        this.properties = properties;
        loadImportToHsmLabelMap();
    }

    @Override
    public String getJssInitFile() throws HsmBaseException {
        String value = properties.getProperty(HSM_CONFIG_JSS_INIT_FILE);
        if (Objects.isNull(value) || value.isEmpty()) {
            throw new HsmBaseException("Wrong HSM configuration, cause: JSS init file not set");
        }
        return value.trim();
    }

    @Override
    public String map(String fileImportLabel) {
        String label = importToHsmLabelMap.get(fileImportLabel);
        return Objects.isNull(label) ? fileImportLabel : label;
    }

    @Override
    public HsmLabelConfiguration get(String label) throws HsmBaseException {
        HsmLabelConfiguration hsmLabelConfiguration = labelToConfigMap.get(label);
        if (Objects.isNull(hsmLabelConfiguration)) {
            throw new HsmBaseException("Asking configuration for a label that is missing, label:" + label);
        }
        return hsmLabelConfiguration;
    }


    private void loadImportToHsmLabelMap() throws HsmBaseException {
        Set<Map.Entry<Object, Object>> allEntries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : allEntries) {
            Object key = entry.getKey();
            if (key instanceof String && ((String) key).startsWith(HSM_CONFIG_LABEL_PREFIX)) {
                HsmLabelConfiguration cfg = new HsmLabelConfiguration((String) entry.getValue());
                String label = ((String) key).replace(HSM_CONFIG_LABEL_PREFIX, "");
                importToHsmLabelMap.put(getFileImportLabel(label, cfg), label);
                labelToConfigMap.put(label, cfg);
            }
        }
    }

    private String getFileImportLabel(String fileLabel, HsmLabelConfiguration cfg) {
        try {
            return cfg.getFileImportLabel();
        } catch (HsmBaseException e) {
            return fileLabel;
        }
    }
}
