package com.energyict.mdc.multisense.api.impl;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 4/30/15.
 */
public class LinkInfo {
    public static final String REF_SELF = "self";
    public static final String REF_PARENT = "up";
    public static final String REF_RELATION = "related";

    public Long id;
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link link;
    public Long version;

}
