/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 25/02/13 - 13:57
 */
public class XemexProperties extends Dsmr40Properties {

    private static String RTU_TYPE = "RtuType";
    private static String FOLDER_EXTERNAL_NAME = "FolderExtName";

    @Override
    public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        return requiredKeys;
    }

    @Override
    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(CLIENT_MAC_ADDRESS);
        optional.add(SECURITY_LEVEL);
        optional.add(MAX_REC_PDU_SIZE);
        optional.add(PROPERTY_FORCED_TO_READ_CACHE);

        optional.add(RTU_TYPE);
        optional.add(FOLDER_EXTERNAL_NAME);

        optional.add(TIMEOUT);
        optional.add(RETRIES);
        optional.add(FORCED_DELAY);
        optional.add(DSMR_40_HEX_PASSWORD);
        return optional;
    }

}
