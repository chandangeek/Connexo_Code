package com.energyict.mdc.upl.cache;

import com.energyict.mdc.upl.ProtocolException;

/**
 * Reports any problem that may occur (except SQLException) when a {@link CachingProtocol}
 * is attempting to fetch its cache from the database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (12:42)
 */
public class ProtocolCacheFetchException extends ProtocolException {

    public ProtocolCacheFetchException() {
        super();
    }

    public ProtocolCacheFetchException(String msg) {
        super(msg);
    }

    public ProtocolCacheFetchException(Exception e) {
        super(e);
    }

    public ProtocolCacheFetchException(Exception e, String msg) {
        super(e, msg);
    }

}