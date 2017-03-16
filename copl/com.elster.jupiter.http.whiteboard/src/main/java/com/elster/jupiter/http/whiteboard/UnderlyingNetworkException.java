/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.util.exception.BaseException;
import org.osgi.service.http.NamespaceException;

import java.net.MalformedURLException;

/**
 * RuntimeException to wrap SQLExceptions
 */
public class UnderlyingNetworkException extends BaseException {
	private static final long serialVersionUID = 1L;

    public UnderlyingNetworkException(MalformedURLException cause) {
        super(MessageSeeds.NETWORK, cause);
    }

    public UnderlyingNetworkException(NamespaceException cause) {
        super(MessageSeeds.NETWORK, cause);
    }
}
