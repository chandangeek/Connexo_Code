/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle.impl;


import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.google.common.base.Joiner;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/tables")
public class SchemaResource {

    @Inject
    private OracleSchemaService schemaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public String[] getTables() {
        List<ExistingTable> tables = schemaService.getTableNames();
        String result[] = new String[tables.size()];
        for (int i = 0; i < tables.size(); i++) {
            result[i] = tables.get(i).getName();
        }
        return result;
    }

    @GET
    @Path("/{tableName}.txt")
    @Produces("text/plain")
    public String getCode(@PathParam("tableName") String tableName) {
        Optional<ExistingTable> table = schemaService.getTable(tableName);
        if (table.isPresent()) {
            List<String> code = new TableModelGenerator(table.get()).generate();
            return Joiner.on("\n").join(code.toArray(new String[code.size()]));
        } else {
            return "Not found ";
        }
    }

}
