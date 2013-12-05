package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.shadow.PluggableClassShadow;

import java.sql.SQLException;

/**
 * Serves as an implementation of the common CRUD of PluggableClass enabled Services
 *
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:05
 */
abstract class AbstractPluggableClassServiceImpl {

    public PluggableClass create(PluggableClassShadow shadow) throws BusinessException, SQLException {
        return MeteringWarehouse.getCurrent().getPluggableClassFactory().create(shadow);
    }

    public PluggableClass update(int id, PluggableClassShadow shadow) throws BusinessException, SQLException {
        PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id);
        if (pluggableClass != null) {
            pluggableClass.update(shadow);
        }
        return pluggableClass;
    }

    public void delete(int id) throws BusinessException, SQLException {
        PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id);
        pluggableClass.delete();
    }
}
