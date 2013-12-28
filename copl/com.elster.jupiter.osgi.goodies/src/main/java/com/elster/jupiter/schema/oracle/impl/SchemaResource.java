package com.elster.jupiter.schema.oracle.impl;


import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.elster.jupiter.schema.oracle.UserTable;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import java.util.*;

@Path("/tables")
public class SchemaResource {
	
	@Inject
	private OracleSchemaService schemaService;

	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public String[] getTables() {
		List<UserTable> tables = schemaService.getTableNames();
		String result[] = new String[tables.size()];
		for (int i = 0 ; i < tables.size(); i++) {
			result[i] = tables.get(i).getName();
		}
		return result;
	}
	
	@GET
    @Path("/{tableName}.txt")
    @Produces("text/plain")
    public String getCode(@PathParam("tableName") String tableName) {
		Optional<UserTable>  table = schemaService.getTable(tableName);
		if (table.isPresent()) {
			List<String> code = table.get().generate();
			return Joiner.on("\n").join(code.toArray(new String[code.size()]));
		} else {
			return "Not found ";
		}
    }

}
