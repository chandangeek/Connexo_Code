/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HsmConfigurationPropFileImpl extends HsmAbstractConfiguration implements HsmConfiguration {

    public static final ChainingMode DEFAULT_CHAINING_MODE = ChainingMode.CBC;
    public static final PaddingAlgorithm DEFAULT_PADDING_ALGORITHM = PaddingAlgorithm.PKCS;

    private final File file;
    private final Properties properties;
    private final Map<String, String> importToHsmLabelMap = new HashMap<>();
    private final Map<String, HsmLabelConfiguration> labelToConfigMap = new HashMap<>();

    public HsmConfigurationPropFileImpl(String configFile) throws HsmBaseException {
        properties = new Properties();
        this.file = new File(configFile);
        try (FileReader fr = new FileReader(file)) {
            properties.load(fr);
            loadImportToHsmLabelMap();
        } catch (IOException e) {
            throw new HsmBaseException(e);
        }
    }

    /**
     * This constructor should be used only for test purposes.
     * @param properties
     * @throws HsmBaseException
     */
    public HsmConfigurationPropFileImpl(Properties properties) throws HsmBaseException {
        this.file = null;
        this.properties = properties;
        loadImportToHsmLabelMap();
    }

    public HsmConfigurationPropFileImpl(File file) throws HsmBaseException {
        this(file.getAbsolutePath());
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
    public HsmLabelConfiguration get(String label)  {
        HsmLabelConfiguration hsmLabelConfiguration = labelToConfigMap.get(label);
        if (Objects.isNull(hsmLabelConfiguration)) {
            return new HsmLabelConfiguration(label, DEFAULT_CHAINING_MODE, DEFAULT_PADDING_ALGORITHM);
        }
        return hsmLabelConfiguration;
    }

    @Override
    public Collection<HsmLabelConfiguration> getLabels() {
        return labelToConfigMap.values();
    }


    @Override
    public int getTimeoutRetryCount() {
        String value = properties.getProperty(HSM_CONFIG_TIMEOUT_RETRY_COUNT);
        if (Objects.isNull(value) || value.isEmpty() || Integer.parseInt(value)<=0) {
            return HSM_CONFIG_TIMEOUT_RETRY_COUNT_DEFAULT;
        }
        return Integer.parseInt(value);
    }


    private void loadImportToHsmLabelMap() throws HsmBaseException {
        Set<Map.Entry<Object, Object>> allEntries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : allEntries) {
            Object key = entry.getKey();
            if (key instanceof String && ((String) key).startsWith(HSM_CONFIG_LABEL_PREFIX)) {
                String label = ((String) key).replace(HSM_CONFIG_LABEL_PREFIX + HSM_CONFIG_SEPARATOR, "");
                HsmLabelConfiguration cfg = new HsmLabelConfiguration(label, (String) entry.getValue());
                importToHsmLabelMap.put(getFileImportLabel(label, cfg), label);
                labelToConfigMap.put(label, cfg);
            }
        }
    }



    private String getFileImportLabel(String fileLabel, HsmLabelConfiguration cfg) {
        try {
            return cfg.getImportFileLabel();
        } catch (HsmBaseException e) {
            return fileLabel;
        }
    }

}
