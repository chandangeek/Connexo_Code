package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
import com.elster.jupiter.hsm.model.configuration.HsmLabelConfiguration;


import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This implementation does not work in OSGi due to thread that is being launched to reload configuration. This thread does not have proper class loader in context therefore on any
 * change performed on underlying file configuration operations will fail.
 */
public class HsmConfigurationAutoReloadFile implements HsmConfiguration {

    private final  ReloadingFileBasedConfigurationBuilder cfgBuilder;

    public HsmConfigurationAutoReloadFile(String fileName) throws HsmBaseException {

        File file = new File(fileName);
        if (!file.exists()) {
            throw new HsmBaseException(new FileNotFoundException());
        }

        FileBasedBuilderParametersImpl fileBasedBuilderParameters = new FileBasedBuilderParametersImpl().setFile(new File(fileName));
        cfgBuilder = new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
        cfgBuilder.setParameters(fileBasedBuilderParameters.getParameters());

        new PeriodicReloadingTrigger(cfgBuilder.getReloadingController(), null, 30, TimeUnit.SECONDS).start();
    }

    public HsmConfigurationAutoReloadFile(ReloadingFileBasedConfigurationBuilder cfgBuilder) {
        this.cfgBuilder = cfgBuilder;
    }

    @Override
    public String getJssInitFile() throws HsmBaseException {
        String value = getUnderlyingConfiguration().getString(HSM_CONFIG_JSS_INIT_FILE);
        if (Objects.isNull(value) || value.isEmpty()) {
            throw new HsmBaseException("Wrong HSM configuration, cause: JSS init file not set");
        }
        return value.trim();
    }

    @Override
    public String map(String label) throws HsmBaseException {
        Iterator keys = getUnderlyingConfiguration().getKeys(HSM_CONFIG_LABEL_PREFIX);
        while (keys.hasNext()) {
            String currentKey = (String) keys.next();
            HsmLabelConfiguration labelConfigured = new HsmLabelConfiguration(this.getUnderlyingConfiguration().getString(currentKey));
            if (label.equals(getImportFileLabelIfExists(labelConfigured))) {
                return currentKey.replace(HSM_CONFIG_LABEL_PREFIX + HSM_CONFIG_SEPARATOR,"");
            }
        }
        return label;
    }


    @Override
    public HsmLabelConfiguration get(String label) throws HsmBaseException {
        String value = getUnderlyingConfiguration().getString(HSM_CONFIG_LABEL_PREFIX + HSM_CONFIG_SEPARATOR + label);
        if (Objects.isNull(value) || value.isEmpty()) {
            throw new HsmBaseException("Asking configuration for a label that is missing, label:" + label);
        }
        return new HsmLabelConfiguration(value);
    }

    @Override
    public Collection<HsmLabelConfiguration> getLabels() throws HsmBaseException {
        List<HsmLabelConfiguration> allLabels = new ArrayList<>();
        Iterator keys = getUnderlyingConfiguration().getKeys(HSM_CONFIG_LABEL_PREFIX);
        while (keys.hasNext()) {
            String currentKey = (String) keys.next();
            allLabels.add(new HsmLabelConfiguration(getUnderlyingConfiguration().getString(currentKey)));
        }
        return allLabels;
    }

    private String getImportFileLabelIfExists(HsmLabelConfiguration labelConfigured) {
        try {
            return labelConfigured.getImportFileLabel();
        } catch (HsmBaseException e) {
            return null;
        }
    }

    private ImmutableConfiguration getUnderlyingConfiguration() throws HsmBaseException {
        try {
            return cfgBuilder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new HsmBaseException(e);
        }
    }

}


