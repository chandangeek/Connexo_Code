package com.energyict.mdc.multisense.api.impl.utils.doc;

/**
 * Interface used to instruct Miredot how to generate doc for javax.ws.rs.core.Link
 */
public class LinkJson {
    public Self self;
    /**
     * HTTP link to described element
     */
    public String href;

    private class Self {
        /**
         * Cfr https://tools.ietf.org/html/rfc5988#section-6.2.2
         */
        public Relation rel;
        public String title;
    }

    private enum Relation {
        self, prev, next, first, last, describedby, glossary
    }
}
