package test.com.energyict.protocolimplV2.elster.ctr.demo;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.shadow.amr.RtuRegisterMappingShadow;
import com.energyict.obis.ObisCode;

import java.sql.SQLException;
import java.util.List;

public class RtuRegisterMappingCRUD {

    private RtuRegisterMappingCRUD() {

    }

    public static RtuRegisterMapping findOrCreateRegisterMapping(String name, String obisCodeString) throws BusinessException, SQLException {
        return findOrCreateRegisterMapping(name, null, true, ObisCode.fromString(obisCodeString), 1, 0);
    }

    public static RtuRegisterMapping findOrCreateRegisterMapping(String name, String description, boolean cumulative, ObisCode obisCode, int productSpecId, int rtuRegisterGroupId) throws BusinessException, SQLException {
        RtuRegisterMapping mapping = findRegisterMapping(obisCode);
        if (mapping == null) {
            mapping = findRegisterMapping(name);
        }
        if (mapping == null) {
            mapping = createRegisterMapping(name, description, cumulative, obisCode, productSpecId, rtuRegisterGroupId);
        }
        return mapping;
    }

    public static RtuRegisterMapping createRegisterMapping(String name, String description, boolean cumulative, ObisCode obisCode, int productSpecId, int rtuRegisterGroupId) throws BusinessException, SQLException {
        RtuRegisterMappingCRUD.deleteRegisterMapping(name); // first delete if it already exists
        RtuRegisterMappingShadow shadow = new RtuRegisterMappingShadow();

        shadow.setName(name);
        shadow.setDescription(description);
        shadow.setCumulative(cumulative);
        shadow.setObisCode(obisCode);
        shadow.setProductSpecId(productSpecId);
        shadow.setRtuRegisterGroupId(rtuRegisterGroupId);

        return MeteringWarehouse.getCurrent().getRtuRegisterMappingFactory().create(shadow);
    }

    public static void deleteRegisterMapping(String name) throws BusinessException, SQLException {
        List<RtuRegisterMapping> mappings = MeteringWarehouse.getCurrent().getRtuRegisterMappingFactory().findByName(name);
        for (RtuRegisterMapping mapping : mappings) {
            mapping.delete();
        }
    }

    public static RtuRegisterMapping findRegisterMapping(String name) {
        List<RtuRegisterMapping> mappings = MeteringWarehouse.getCurrent().getRtuRegisterMappingFactory().findByName(name);
        if (mappings.isEmpty()) {
            return null;
        }
        return mappings.get(0);
    }

    private static RtuRegisterMapping findRegisterMapping(ObisCode obisCode) {
        return MeteringWarehouse.getCurrent().getRtuRegisterMappingFactory().find(obisCode, 1);
    }

}
