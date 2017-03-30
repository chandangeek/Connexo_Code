/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides a PreparedStatement from a Connection.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (15:37)
 */
public interface PreparedStatementProvider {

    PreparedStatement prepare(Connection connection) throws SQLException;

}