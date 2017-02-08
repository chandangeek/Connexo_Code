/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia.doc;

/**
 * Interface used to instruct Miredot how to generate doc for javax.ws.rs.core.Link
 */
public class LinkJson {
    /**
     * Parameters describing the nature of the link
     */
    public Params params;
    /**
     * HTTP link to linked entity
     */
    public String href;

    private class Params {
        /**
         * Describes the nature of the link between the main entity and the entity in this link.
         * Cfr https://tools.ietf.org/html/rfc5988#section-6.2.2
         */
        public Relation rel;
        /**
         * General name of the entity that is linked to. This name can be used for display purposes and should be localized.
         */
        public String title;
    }

    private enum Relation {
        self, prev, next, first, last, describedby, glossary, parent
    }
}
