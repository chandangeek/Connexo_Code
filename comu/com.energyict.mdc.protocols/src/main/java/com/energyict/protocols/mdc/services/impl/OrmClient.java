/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.transaction.Transaction;

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

    Object getCacheObject(int deviceId) throws IOException;

    void setCacheObject(int deviceId, Object cacheObject) throws SQLException;

    int getConfProgChange(int deviceId) throws SQLException;

    void setConfProgChange(int deviceId, int confProgChange) throws SQLException;

    UniversalObject[] getUniversalObjectList(int deviceId) throws SQLException;

    void saveUniversalObjectList(int deviceId, UniversalObject... universalObjects) throws SQLException;

    <T> T execute(Transaction<T> transaction);

}