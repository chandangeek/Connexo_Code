package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:07
 */
@Path("/codetables")
public class CodeTableResource {

    private final CodeFactory codeFactory;

    @Inject
    public CodeTableResource(CodeFactory codeFactory) {
        this.codeFactory = codeFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public CodeTableInfos getCodeTablePropertyContext(@Context UriInfo uriInfo){
        CodeTableInfos timeZoneInUseInfos = new CodeTableInfos();
        this.codeFactory
                .findAllCodeTables()
                .stream()
                .map(CodeTableInfo::new)
                .forEach(timeZoneInUseInfos.codeTableInfos::add);
        return timeZoneInUseInfos;
    }

}