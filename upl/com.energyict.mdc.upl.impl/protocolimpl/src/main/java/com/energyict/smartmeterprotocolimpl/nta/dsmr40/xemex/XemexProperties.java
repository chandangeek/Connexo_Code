package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 25/02/13 - 13:57
 */
class XemexProperties extends Dsmr40Properties {

    private static final String RTU_TYPE = "RtuType";
    private static final String FOLDER_EXTERNAL_NAME = "FolderExtName";

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.string(RTU_TYPE, false));
        propertySpecs.add(UPLPropertySpecFactory.string(FOLDER_EXTERNAL_NAME, false));
        return propertySpecs;
    }

    @Override
    protected boolean securityLevelIsRequired() {
        return false;
    }

}