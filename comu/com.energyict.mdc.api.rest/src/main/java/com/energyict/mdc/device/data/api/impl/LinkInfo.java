package com.energyict.mdc.device.data.api.impl;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 4/30/15.
 */
public class LinkInfo {
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link self;
}
