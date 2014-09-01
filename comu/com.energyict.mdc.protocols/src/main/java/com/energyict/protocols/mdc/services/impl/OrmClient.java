package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.common.BusinessException;

import com.energyict.dlms.UniversalObject;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Acts as a facade for the {@link com.elster.jupiter.orm.DataModel}
 * of this protocols module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-01 (08:45)
 */
public interface OrmClient {

    public Object getCacheObject(int deviceId) throws IOException;

    public void setCacheObject(int deviceId, Object cacheObject) throws SQLException;

    public int getConfProgChange(int deviceId) throws SQLException, BusinessException;

    public void setConfProgChange(int deviceId, int confProgChange) throws SQLException;

    public UniversalObject[] getUniversalObjectList (int deviceId) throws SQLException;

    public void saveUniversalObjectList(int deviceId, UniversalObject... universalObjects) throws SQLException;

}