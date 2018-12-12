/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

public interface SoapProviderSupportFactory {

    ContextClassLoaderResource create();

}
