package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.util.exception.BaseException;

import java.net.MalformedURLException;

/**
 * RuntimeException to wrap SQLExceptions
 */
public class UnderlyingNetworkException extends BaseException {
	private static final long serialVersionUID = 1L;

    public UnderlyingNetworkException(MalformedURLException cause) {
        super(MessageSeeds.NETWORK, cause);
    }
}
