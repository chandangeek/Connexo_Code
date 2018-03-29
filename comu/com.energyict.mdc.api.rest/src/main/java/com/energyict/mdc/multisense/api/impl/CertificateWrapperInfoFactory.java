/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class CertificateWrapperInfoFactory extends SelectableFieldFactory<CertificateWrapperInfo, CertificateWrapper> {


    @Inject
    public CertificateWrapperInfoFactory() {

    }

    public LinkInfo asLink(CertificateWrapper certificateWrapper, Relation relation, UriInfo uriInfo) {
        CertificateWrapperInfo certificateWrapperInfo = new CertificateWrapperInfo();
        copySelectedFields(certificateWrapperInfo, certificateWrapper, uriInfo, Arrays.asList("id", "version"));
        certificateWrapperInfo.link = link(certificateWrapper, relation, uriInfo);
        return certificateWrapperInfo;
    }

    public List<LinkInfo> asLink(Collection<CertificateWrapper> certificateWrappers, Relation relation, UriInfo uriInfo) {
        return certificateWrappers.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(CertificateWrapper certificateWrapper, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device type")
                .build(certificateWrapper.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(CertificateWrapperResource.class)
                .path(CertificateWrapperResource.class, "getCertificateWrapper");
    }

    public CertificateWrapperInfo from(CertificateWrapper certificateWrapper, UriInfo uriInfo, List<String> fields) {
        CertificateWrapperInfo certificateWrapperInfo = new CertificateWrapperInfo();
        copySelectedFields(certificateWrapperInfo, certificateWrapper, uriInfo, fields);
        return certificateWrapperInfo;
    }

    protected Map<String, PropertyCopier<CertificateWrapperInfo, CertificateWrapper>> buildFieldMap() {
        Map<String, PropertyCopier<CertificateWrapperInfo, CertificateWrapper>> map = new HashMap<>();
        map.put("id", (certificateWrapperInfo, certificateWrapper, uriInfo) -> {
            certificateWrapperInfo.id = certificateWrapper.getId();
        });
        map.put("version", (certificateWrapperInfo, certificateWrapper, uriInfo) -> {
            certificateWrapperInfo.version = certificateWrapper.getVersion();
        });
        map.put("alias", (certificateWrapperInfo, certificateWrapper, uriInfo) -> {
            certificateWrapperInfo.alias = certificateWrapper.getAlias();
        });
        map.put("link", (certificateWrapperInfo, certificateWrapper, uriInfo) -> {
            certificateWrapperInfo.link = link(certificateWrapper, Relation.REF_SELF, uriInfo);
        });
        return map;
    }

}
