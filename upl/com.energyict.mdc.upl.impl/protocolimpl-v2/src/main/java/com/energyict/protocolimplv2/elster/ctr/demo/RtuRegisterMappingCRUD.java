package com.energyict.protocolimplv2.elster.ctr.demo;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.shadow.amr.RegisterMappingShadow;
import com.energyict.obis.ObisCode;

import java.sql.SQLException;
import java.util.List;

public class RtuRegisterMappingCRUD {

    private RtuRegisterMappingCRUD() {

    }

    public static RegisterMapping findOrCreateRegisterMapping(String name, String obisCodeString) throws BusinessException, SQLException {
        return findOrCreateRegisterMapping(name, null, true, ObisCode.fromString(obisCodeString), 1, 0);
    }

    public static RegisterMapping findOrCreateRegisterMapping(String name, String description, boolean cumulative, ObisCode obisCode, int productSpecId, int rtuRegisterGroupId) throws BusinessException, SQLException {
        RegisterMapping mapping = findRegisterMapping(obisCode);
        if (mapping == null) {
            mapping = findRegisterMapping(name);
        }
        if (mapping == null) {
            mapping = createRegisterMapping(name, description, cumulative, obisCode, productSpecId, rtuRegisterGroupId);
        }
        return mapping;
    }

    public static RegisterMapping createRegisterMapping(String name, String description, boolean cumulative, ObisCode obisCode, int productSpecId, int rtuRegisterGroupId) throws BusinessException, SQLException {
        RtuRegisterMappingCRUD.deleteRegisterMapping(name); // first delete if it already exists
        RegisterMappingShadow shadow = new RegisterMappingShadow();

        shadow.setName(name);
        shadow.setDescription(description);
        shadow.setCumulative(cumulative);
        shadow.setObisCode(obisCode);
        shadow.setProductSpecId(productSpecId);
        shadow.setRtuRegisterGroupId(rtuRegisterGroupId);

        return MeteringWarehouse.getCurrent().getRegisterMappingFactory().create(shadow);
    }

    public static void deleteRegisterMapping(String name) throws BusinessException, SQLException {
        List<RegisterMapping> mappings = MeteringWarehouse.getCurrent().getRegisterMappingFactory().findByName(name);
        for (RegisterMapping mapping : mappings) {
            mapping.delete();
        }
    }

    public static RegisterMapping findRegisterMapping(String name) {
        List<RegisterMapping> mappings = MeteringWarehouse.getCurrent().getRegisterMappingFactory().findByName(name);
        if (mappings.isEmpty()) {
            return null;
        }
        return mappings.get(0);
    }

    private static RegisterMapping findRegisterMapping(ObisCode obisCode) {
        return MeteringWarehouse.getCurrent().getRegisterMappingFactory().find(obisCode, 1);
    }

}
