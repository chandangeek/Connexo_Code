package com.elster.jupiter.rest.util.hypermedia;

/**
 * This enum models the REL field of a hypermedia link
 * <p>
 * Created by bvn on 11/18/15.
 */
public enum Relation {
    REF_SELF("self"),
    REF_PARENT("up"),
    REF_RELATION("related");

    private final String rel;

    Relation(String rel) {

        this.rel = rel;
    }

    public String rel() {
        return rel;
    }
}
