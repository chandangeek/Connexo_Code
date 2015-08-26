package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.xml;

/**
 * @author sva
 * @since 21/12/12 - 16:14
 */
public enum XMLTagType {

    /**
     * Indication of a node that can be parsed.
     * The node - and its set of child nodes - represent a single object.
     * So this node - and all its child nodes - should be parsed in one go.
     */
    PARSE,

    /**
     * Indication of a node, containing useful information in its child nodes.
     * The node does not represent a single object, but its child nodes contain workable (smaller) objects.
     * So the node itself must not be parsed, but instead we should loop over the child nodes and try to parse these.
     */
    PARSE_CHILD_NODES,

    /**
     * Indication of a sub part (e.g.: a part of an object).
     */
    SUB_PART,

    /**
     * Indication of a node containing an DLMS AXDR encoded data type.
     */
    AXDR_DATA,

    /**
     * Indication of a node that can be skipped. The info contained in this node (and its child nodes) is not relevant,
     * so the node should not be parsed, but can be skipped.
     */
    SKIP_NODE;
}