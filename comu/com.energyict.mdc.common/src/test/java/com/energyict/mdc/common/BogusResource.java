/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Bogus resource to test QueryParameters
 */
@Path("/bogus")
public class BogusResource {

    private final DataModel dateModel;

    @Inject
    public BogusResource(DataModel dateModel) {
        this.dateModel = dateModel;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<String> getSomething(@BeanParam JsonQueryParameters queryParameters) {
        return DefaultFinder.of(String.class, dateModel).from(queryParameters).find();
    }
}
