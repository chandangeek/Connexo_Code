/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.config;


import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Collection;

public interface HsmConfiguration {

    String HSM_CONFIG_JSS_INIT_FILE = "hsm.config.jss.init.file";
    String HSM_CONFIG_LABEL_PREFIX = "hsm.config.label";
    String HSM_CONFIG_SEPARATOR = ".";

    /**
     *
      * @return the configuration file needed for JSS to be initialized
     */
    String getJssInitFile() throws HsmBaseException;

    /**
     *
     * @param label that we search for mapping (this should be label present in file)
     * @return a matching (HSM) label if mapped, otherwise same as param
     */
    String map(String label) throws HsmBaseException;


    /**
     *
     * @param label used in HSM (not one in importer, which is mapped via another method in this interface)
     * @return different HSM label specs for the requested @label
     */
    HsmLabelConfiguration get(String label) throws HsmBaseException;

    /**
     *
     * @return all configured labels
     */
    Collection<HsmLabelConfiguration> getLabels() throws HsmBaseException;
}
