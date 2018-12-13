/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 4/30/15.
 */
public class LinkInfo<T> {
    /**
     * This entity's unique identifier
     */
    public T id;
    /**
     * Links to related entities. The link will contain a reference to:
     * <ul>
     * <li>the entity itself</li>
     * <li>to next and previous pages if applicable</li>
     * <li>to related entities</li>
     * <li>to parent entity</li>
     * </ul>
     */
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    public Link link;
    /**
     * version of this entity, used for concurrency check
     */
    public Long version;

}
