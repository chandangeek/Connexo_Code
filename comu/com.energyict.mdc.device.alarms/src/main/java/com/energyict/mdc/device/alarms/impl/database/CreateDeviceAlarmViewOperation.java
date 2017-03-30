/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateDeviceAlarmViewOperation {
    protected static final Logger LOG = Logger.getLogger(CreateDeviceAlarmViewOperation.class.getName());

    private final DataModel dataModel;

    public CreateDeviceAlarmViewOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public void execute(){
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement statement = buildStatement(conn, buildCreateSQL());
            statement.execute();
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to create view for all alarms", sqlEx);
        }
    }

    protected SqlBuilder buildCreateSQL(){
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE OR REPLACE VIEW " + TableSpecs.DAL_ALARM_ALL + " AS ");
        builder.append("select * from " + TableSpecs.DAL_ALARM_OPEN.name() + " union select * from " + TableSpecs.DAL_ALARM_HISTORY.name());
        return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        if (connection == null){
            throw new IllegalArgumentException("Connection can't be null");
        }
        return sql.prepare(connection);
    }
}
