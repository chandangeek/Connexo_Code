package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateSearchCriteriaViewOperation {
    protected static final Logger LOG = Logger.getLogger(CreateSearchCriteriaViewOperation.class.getName());

    private final DataModel dataModel;

    public CreateSearchCriteriaViewOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public void execute(){
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement statement = buildStatement(conn, buildCreateSQL());
            statement.execute();
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to create view for all search criteria", sqlEx);
        }
    }

    protected SqlBuilder buildCreateSQL(){
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE OR REPLACE VIEW " + TableSpecs.DYN_SEARCHCRITERIA );
       return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        if (connection == null){
            throw new IllegalArgumentException("Connection can't be null");
        }
        return sql.prepare(connection);
    }
}
