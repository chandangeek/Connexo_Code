package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/thesaurus")
public class ThesaurusResource {

    private volatile NlsService nlsService;

    @Inject
    public ThesaurusResource(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @GET
    public ThesaurusInfo getThesaurus(@Context UriInfo uriInfo) {
        MultivaluedMap<String,String> parameters = uriInfo.getQueryParameters();
        List<String> components = Optional.<List<String>>of(parameters.get("cmp")).or(Collections.<String>emptyList());
        ThesaurusInfo thesaurusInfo = new ThesaurusInfo();
        for (String component : components) {
            addComponent(thesaurusInfo, component);
        }
        return thesaurusInfo;
    }

    private void addComponent(ThesaurusInfo thesaurusInfo, String component) {
        Thesaurus thesaurus = nlsService.getThesaurus(component, Layer.REST);
        for (Map.Entry<String, String> entry : thesaurus.getTranslations().entrySet()) {
            thesaurusInfo.translations.add(new TranslationInfo(component, entry.getKey(), entry.getValue()));
        }
    }
}
