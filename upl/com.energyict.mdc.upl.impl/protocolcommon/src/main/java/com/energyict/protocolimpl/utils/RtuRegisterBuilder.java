package com.energyict.protocolimpl.utils;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuTypeShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterMappingShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterSpecShadow;
import com.energyict.obis.ObisCode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/04/11
 * Time: 10:20
 */
public class RtuRegisterBuilder {

    public static final String DEVICE_TYPE_NAME = "ACE6000 DLMS";
    public static final String REGISTER_MAPPING_PREFIX = "ACE6000 - ";
    public static final int UNDEFINED_IN_ANY_PRODUCT_SPEC_ID = 0;
    private static final int DEFAULT_RTU_REGISTER_GROUP_ID = 0;
    public RtuType deviceType = null;

    public static final String[] OBIS_CODES = new String[]{
            "1.1.1.8.3.VZ"
/*
            "0.0.96.2.0.255",
            "0.0.148.2.2.255",
            "0.0.96.6.0.255",
            "0.0.96.6.3.255"
*/
/*
            "1.1.0.1.0.255",
            "0.0.0.1.1.255",
            "0.0.128.3.0.255",
            "0.0.128.6.0.255",
            "0.0.128.7.1.255",
            "0.0.141.0.5.255",
            "0.0.96.12.1.255",
            "0.0.96.2.0.255",
            "0.0.96.2.10.255",
            "0.0.96.2.128.255",
            "0.0.96.2.129.255",
            "0.0.96.2.5.255",
            "0.0.96.51.60.255",
            "0.0.96.52.10.255",
            "0.0.96.52.11.255",
            "0.0.96.52.13.255",
            "0.0.96.52.15.255",
            "0.0.96.52.16.255",
            "0.0.96.52.6.255",
            "0.0.96.52.8.255",
            "0.0.96.54.2.255",
            "0.0.96.54.3.255",
            "0.0.96.54.4.255",
            "0.0.96.6.0.255",
            "0.0.96.61.1.255",
            "0.0.96.61.2.255",
            "0.0.96.8.0.255",
            "0.0.96.9.0.255",
            "0.0.97.97.1.255",
            "0.0.97.97.2.255",
            "1.1.0.3.0.255",
            "1.1.0.3.1.255",
            "1.1.0.8.0.255",
            "1.1.0.8.4.255",
            "1.1.0.8.5.255",
            "1.1.1.17.0.255",
            "1.1.1.2.0.255",
            "1.1.1.2.1.255",
            "1.1.1.2.2.255",
            "1.1.1.35.0.255",
            "1.1.1.35.1.255",
            "1.1.1.35.2.255",
            "1.1.1.36.0.255",
            "1.1.1.36.1.255",
            "1.1.1.36.2.255",
            "1.1.1.37.0.255",
            "1.1.1.37.1.255",
            "1.1.1.37.2.255",
            "1.1.1.38.0.255",
            "1.1.1.38.1.255",
            "1.1.1.38.2.255",
            "1.1.1.7.0.255",
            "1.1.1.8.0.255",
            "1.1.1.8.1.255",
            "1.1.1.8.2.255",
            "1.1.1.8.3.255",
            "1.1.1.8.4.255",
            "1.1.10.17.0.255",
            "1.1.10.7.0.255",
            "1.1.10.8.0.255",
            "1.1.13.3.0.255",
            "1.1.13.5.0.255",
            "1.1.13.7.0.255",
            "1.1.14.3.0.255",
            "1.1.14.6.0.255",
            "1.1.14.7.0.255",
            "1.1.2.17.0.255",
            "1.1.2.7.0.255",
            "1.1.2.8.0.255",
            "1.1.2.8.15.255",
            "1.1.21.17.0.255",
            "1.1.21.7.0.255",
            "1.1.21.8.0.255",
            "1.1.22.17.0.255",
            "1.1.22.7.0.255",
            "1.1.22.8.0.255",
            "1.1.23.17.0.255",
            "1.1.23.7.0.255",
            "1.1.23.8.0.255",
            "1.1.24.17.0.255",
            "1.1.24.7.0.255",
            "1.1.24.8.0.255",
            "1.1.25.17.0.255",
            "1.1.25.8.0.255",
            "1.1.26.17.0.255",
            "1.1.26.8.0.255",
            "1.1.27.17.0.255",
            "1.1.27.8.0.255",
            "1.1.28.17.0.255",
            "1.1.28.8.0.255",
            "1.1.29.17.0.255",
            "1.1.29.7.0.255",
            "1.1.29.8.0.255",
            "1.1.3.17.0.255",
            "1.1.3.7.0.255",
            "1.1.3.8.0.255",
            "1.1.3.8.15.255",
            "1.1.30.17.0.255",
            "1.1.30.7.0.255",
            "1.1.30.8.0.255",
            "1.1.31.6.0.255",
            "1.1.31.7.0.255",
            "1.1.32.6.0.255",
            "1.1.32.7.0.255",
            "1.1.33.7.0.255",
            "1.1.4.17.0.255",
            "1.1.4.7.0.255",
            "1.1.4.8.0.255",
            "1.1.4.8.15.255",
            "1.1.41.17.0.255",
            "1.1.41.7.0.255",
            "1.1.41.8.0.255",
            "1.1.42.17.0.255",
            "1.1.42.7.0.255",
            "1.1.42.8.0.255",
            "1.1.43.17.0.255",
            "1.1.43.7.0.255",
            "1.1.43.8.0.255",
            "1.1.44.17.0.255",
            "1.1.44.7.0.255",
            "1.1.44.8.0.255",
            "1.1.45.17.0.255",
            "1.1.45.8.0.255",
            "1.1.46.17.0.255",
            "1.1.46.8.0.255",
            "1.1.47.17.0.255",
            "1.1.47.8.0.255",
            "1.1.48.17.0.255",
            "1.1.48.8.0.255",
            "1.1.49.17.0.255",
            "1.1.49.7.0.255",
            "1.1.49.8.0.255",
            "1.1.5.17.0.255",
            "1.1.5.2.0.255",
            "1.1.5.2.1.255",
            "1.1.5.2.2.255",
            "1.1.5.35.0.255",
            "1.1.5.35.1.255",
            "1.1.5.35.2.255",
            "1.1.5.36.0.255",
            "1.1.5.36.1.255",
            "1.1.5.36.2.255",
            "1.1.5.37.0.255",
            "1.1.5.37.1.255",
            "1.1.5.37.2.255",
            "1.1.5.38.0.255",
            "1.1.5.38.2.255",
            "1.1.5.7.0.255",
            "1.1.5.8.0.255",
            "1.1.5.8.1.255",
            "1.1.5.8.15.255",
            "1.1.5.8.2.255",
            "1.1.50.17.0.255",
            "1.1.50.7.0.255",
            "1.1.50.8.0.255",
            "1.1.51.6.0.255",
            "1.1.51.7.0.255",
            "1.1.52.6.0.255",
            "1.1.52.7.0.255",
            "1.1.53.7.0.255",
            "1.1.6.17.0.255",
            "1.1.6.7.0.255",
            "1.1.6.8.0.255",
            "1.1.6.8.15.255",
            "1.1.61.17.0.255",
            "1.1.61.7.0.255",
            "1.1.61.8.0.255",
            "1.1.62.17.0.255",
            "1.1.62.7.0.255",
            "1.1.62.8.0.255",
            "1.1.63.17.0.255",
            "1.1.63.7.0.255",
            "1.1.63.8.0.255",
            "1.1.64.17.0.255",
            "1.1.64.7.0.255",
            "1.1.64.8.0.255",
            "1.1.65.17.0.255",
            "1.1.65.8.0.255",
            "1.1.66.17.0.255",
            "1.1.66.8.0.255",
            "1.1.67.17.0.255",
            "1.1.67.8.0.255",
            "1.1.68.17.0.255",
            "1.1.68.8.0.255",
            "1.1.69.17.0.255",
            "1.1.69.7.0.255",
            "1.1.69.8.0.255",
            "1.1.7.17.0.255",
            "1.1.7.7.0.255",
            "1.1.7.8.0.255",
            "1.1.7.8.15.255",
            "1.1.70.17.0.255",
            "1.1.70.7.0.255",
            "1.1.70.8.0.255",
            "1.1.71.6.0.255",
            "1.1.71.7.0.255",
            "1.1.72.7.0.255",
            "1.1.73.7.0.255",
            "1.1.8.17.0.255",
            "1.1.8.2.0.255",
            "1.1.8.2.1.255",
            "1.1.8.2.2.255",
            "1.1.8.35.0.255",
            "1.1.8.35.1.255",
            "1.1.8.35.2.255",
            "1.1.8.36.0.255",
            "1.1.8.36.1.255",
            "1.1.8.36.2.255",
            "1.1.8.37.0.255",
            "1.1.8.37.1.255",
            "1.1.8.37.2.255",
            "1.1.8.38.0.255",
            "1.1.8.38.1.255",
            "1.1.8.38.2.255",
            "1.1.8.7.0.255",
            "1.1.8.8.0.255",
            "1.1.8.8.1.255",
            "1.1.8.8.15.255",
            "1.1.8.8.2.255",
            "1.1.81.7.10.255",
            "1.1.81.7.2.255",
            "1.1.81.7.21.255",
            "1.1.81.7.40.255",
            "1.1.81.7.51.255",
            "1.1.81.7.62.255",
            "1.1.9.17.0.255",
            "1.1.9.7.0.255",
            "1.1.9.8.0.255",
            "1.1.92.7.0.255",
            "1.1.96.5.0.255",
            "1.1.96.50.1.255",
            "1.1.96.50.2.255",
            "1.1.96.50.3.255",
            "1.1.96.50.4.255",
            "1.1.96.50.5.255",
            "1.1.96.50.6.255",
            "1.6.82.8.0.255",
            "1.7.82.8.0.255",
            "1.8.82.8.0.255",
            "1.9.82.8.0.255"
*/
    };

    public MeteringWarehouse mw() {
        if (MeteringWarehouse.getCurrent() == null) {
            MeteringWarehouse.createBatchContext();
        }
        return MeteringWarehouse.getCurrent();
    }

    public RtuType getDeviceType() throws BusinessException {
        if (deviceType == null) {
            List<RtuType> rtuTypes = mw().getRtuTypeFactory().findByName(DEVICE_TYPE_NAME);
            if (rtuTypes.isEmpty()) {
                throw new BusinessException("RtuType with name [" + DEVICE_TYPE_NAME + "] not found in EIServer!");
            } else if (rtuTypes.size() > 1) {
                throw new BusinessException("Expected 1 RtuType but found [" + rtuTypes.size() + "] RtuTypes in EIServer with name [" + DEVICE_TYPE_NAME + "]!");
            }
            deviceType = rtuTypes.get(0);
        }
        return deviceType;
    }

    private void buildRegisters() throws BusinessException {
        deleteRegisterSpecs(getDeviceType());
        deleteDeviceRegisterMappings();
        createDeviceRegisterMappings();
        createRegisterSpecs(getDeviceType());
    }

    /**
     * @param rtuType
     * @throws BusinessException
     */
    private void deleteRegisterSpecs(RtuType rtuType) throws BusinessException {
        try {
            RtuTypeShadow shadow = rtuType.getShadow();
            shadow.getRegisterSpecShadows().clear();
            rtuType.update(shadow);
            deviceType = null;
        } catch (SQLException e) {
            throw new BusinessException("Unable to delete the RtuRegisterSpecs for deviceType [" + rtuType.getName() + "]", e);
        }
    }

    /**
     * @throws BusinessException
     */
    private void deleteDeviceRegisterMappings() throws BusinessException {
        List<RtuRegisterMapping> mappings = getRtuRegisterMappingsWithPrefix();
        for (RtuRegisterMapping mapping : mappings) {
            try {
                mapping.delete();
            } catch (SQLException e) {
                System.out.println("Unable to delete mapping [" + mapping.getName() + "]");
            }
        }
    }

    /**
     *
     */
    private void createDeviceRegisterMappings() {
        for (String obisAsString : OBIS_CODES) {
            ObisCode obis = ObisCode.fromString(obisAsString);
            String name = REGISTER_MAPPING_PREFIX + obisAsString;
            if (!obis.toString().equalsIgnoreCase(obis.getDescription())) {
                name += " [" + obis.getDescription() + "]";
            }
            name = name.length() > 80 ? name.substring(0, 79) : name;
            try {
                RtuRegisterMappingShadow shadow = new RtuRegisterMappingShadow();
                shadow.setCumulative(false);
                shadow.setName(name);
                shadow.setDescription(name);
                shadow.setObisCode(obis);
                shadow.setProductSpecId(UNDEFINED_IN_ANY_PRODUCT_SPEC_ID);
                shadow.setRtuRegisterGroupId(DEFAULT_RTU_REGISTER_GROUP_ID);
                mw().getRtuRegisterMappingFactory().create(shadow);
            } catch (Exception e) {
                System.out.println("Unable to create [" + name + "]: " + e.getMessage());
            }
        }
    }

    /**
     * @param deviceType
     */
    private void createRegisterSpecs(RtuType deviceType) throws BusinessException {
        try {
            RtuTypeShadow deviceTypeShadow = deviceType.getShadow();
            List<RtuRegisterMapping> mappings = getRtuRegisterMappingsWithPrefix();
            for (RtuRegisterMapping mapping : mappings) {
                RtuRegisterSpecShadow shadow = new RtuRegisterSpecShadow();
                shadow.setDeviceChannelIndex(mapping.getObisCode().getB());
                shadow.setIntegral(false);
                shadow.setLoadprofileChannelIndex(0);
                shadow.setNumberOfDigits(15);
                shadow.setNumberOfFractionDigits(3);
                shadow.setRegisterMappingId(mapping.getId());
                shadow.setRtuTypeId(deviceType.getId());
                deviceTypeShadow.getRegisterSpecShadows().add(shadow);
            }
            deviceType.update(deviceTypeShadow);
        } catch (Exception e) {
            throw new BusinessException("Unable to create RtuRegisterSpecs for device type [" + deviceType.getName() + "]:", e);
        }
    }

    public static void main(String[] args) throws BusinessException {
        new RtuRegisterBuilder().buildRegisters();
    }

    public List<RtuRegisterMapping> getRtuRegisterMappingsWithPrefix() {
        List<RtuRegisterMapping> prefixMappings = new ArrayList<RtuRegisterMapping>();
        List<RtuRegisterMapping> allMappings = mw().getRtuRegisterMappingFactory().findAll();
        for (RtuRegisterMapping mapping : allMappings) {
            if (mapping.getName().startsWith(REGISTER_MAPPING_PREFIX)) {
                prefixMappings.add(mapping);
            }
        }
        com.energyict.mdw.core.MeteringWarehouse.getCurrent().getRtuRegisterReadingFactory().findAll().size();
        return prefixMappings;
    }


}
