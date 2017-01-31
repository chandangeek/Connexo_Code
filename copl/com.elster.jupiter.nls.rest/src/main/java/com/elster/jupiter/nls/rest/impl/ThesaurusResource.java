/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Path("/thesaurus")
public class ThesaurusResource {

    private final NlsService nlsService;
    private final ThesaurusCache cache;

    @Inject
    public ThesaurusResource(NlsService nlsService, ThesaurusCache cache) {
        this.nlsService = nlsService;
        this.cache = cache;
    }

    @GET
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ThesaurusInfo getThesaurus(@Context UriInfo uriInfo) {
        MultivaluedMap<String,String> parameters = uriInfo.getQueryParameters();
        List<String> components = Optional.ofNullable(parameters.get("cmp")).orElse(Collections.<String>emptyList());
        ThesaurusInfo thesaurusInfo = new ThesaurusInfo();
        for (String component : components) {
            addComponent(thesaurusInfo, component);
        }
        return thesaurusInfo;
    }

    private void addComponent(ThesaurusInfo thesaurusInfo, String component) {
        Thesaurus thesaurus;
        Optional<Thesaurus> optional = cache.get(component, Layer.REST);
        if (!optional.isPresent()) {
            thesaurus = nlsService.getThesaurus(component, Layer.REST);
            cache.put(component, Layer.REST, thesaurus);
        } else {
            thesaurus = optional.get();
        }

        thesaurus.getTranslationsForCurrentLocale().forEach((key, value) -> thesaurusInfo.translations.add(new TranslationInfo(component, key, value)));

    }
}
