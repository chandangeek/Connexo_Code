/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.HasName;

public interface IdentifiedObject extends HasName {

    /**
     * The aliasName is free text human readable name of the object alternative to IdentifiedObject.name. It may be non unique and may not correlate to a naming hierarchy.
     * The attribute aliasName is retained because of backwards compatibility between CIM relases. It is however recommended to replace aliasName with the Name class as aliasName is planned for retirement at a future time.
     */
    String getAliasName();

    /**
     * The description is a free human readable text describing or naming the object. It may be non unique and may not correlate to a naming hierarchy.
     */
    String getDescription();

    /*
     * Master resource identifier issued by a model authority. The mRID must semantically be a UUID as specified in RFC 4122. The mRID is globally unique.
     * For CIMXML data files in RDF syntax, the mRID is mapped to rdf:ID or rdf:about attributes that identify CIM object elements.
     */
    String getMRID();

}
