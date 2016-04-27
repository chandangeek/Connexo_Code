package com.energyict.mdc.multisense.api.impl;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 4/30/15.
 */
public class LinkInfo {
    /**
     * This entity's unique identifier
     */
    public Long id;
    /**
     * Links to related entities. The link will contain a reference to:
     * <ul>
     *  <li>the entity itself</li>
     *  <li>to next and previous pages if applicable</li>
     *  <li>to related entities</li>
     *  <li>to parent entity</li>
     * </ul>
     */
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link link;
    /**
     * version of this entity, used for concurrency check
     */
    public Long version;

}
