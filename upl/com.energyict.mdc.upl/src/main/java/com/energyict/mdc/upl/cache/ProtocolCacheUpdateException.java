package com.energyict.mdc.upl.cache;

import com.energyict.mdc.upl.ProtocolException;

/**
 * Reports any problem that may occur (except SQLException) when a {@link CachingProtocol}
 * is attempting to update its cache from the database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (12:42)
 */
public class ProtocolCacheUpdateException extends ProtocolException {

    public ProtocolCacheUpdateException() {
        super();
    }

    public ProtocolCacheUpdateException(String msg) {
        super(msg);
    }

    public ProtocolCacheUpdateException(Exception e) {
        super(e);
    }

    public ProtocolCacheUpdateException(Exception e, String msg) {
        super(e, msg);
    }

}