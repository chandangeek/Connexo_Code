/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface WebServiceCallRelatedAttributeBinding {
    WebServiceCallOccurrence getOccurrence();
    WebServiceCallRelatedAttribute getType();
}
