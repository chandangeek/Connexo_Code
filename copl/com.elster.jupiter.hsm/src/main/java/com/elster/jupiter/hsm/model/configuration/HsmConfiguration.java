/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.configuration;


import com.elster.jupiter.hsm.model.HsmBaseException;

public interface HsmConfiguration {

    /**
     *
      * @return the configuration file needed for JSS to be initialized
     */
    String getJssInitFile() throws HsmBaseException;

    /**
     *
     * @param label that we search for mapping
     * @return a matching label if mapped, otherwise same as param
     */
    String map(String label);


    /**
     *
     * @param label used in HSM (not one in importer, which is mapped via another method in this interface)
     * @return different HSM label specs for the requested @label
     */
    HsmLabelConfiguration get(String label) throws HsmBaseException;

}
