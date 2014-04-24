package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:07
 */
@Path("/codetables")
public class CodeTableResource {

    public CodeTableResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CodeTableInfos getCodeTablePropertyContext(@Context UriInfo uriInfo){
        CodeTableInfos timeZoneInUseInfos = new CodeTableInfos();
        List<CodeFactory> codeFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CodeFactory.class);
        for (CodeFactory codeFactory : codeFactories) {
            for (Code codeTable : codeFactory.findAllCodeTables()) {
                timeZoneInUseInfos.codeTableInfos.add(new CodeTableInfo(codeTable));
            }
        }
        return timeZoneInUseInfos;
    }
}
