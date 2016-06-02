package com.elster.jupiter.rest.util.hypermedia;

/**
 * This enum models the REL field of a hypermedia link
 * @see  @link https://tools.ietf.org/html/rfc5988
 * <p>
 * Created by bvn on 11/18/15.
 */
public enum Relation {
    REF_SELF("self"),
    REF_PARENT("up"),
    REF_RELATION("related"),
    REF_CURRENT("current"),
    REF_NEXT("next"),
    REF_PREVIOUS("prev");

    private final String rel;

    Relation(String rel) {

        this.rel = rel;
    }

    public String rel() {
        return rel;
    }
}
