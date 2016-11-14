package com.energyict.mdc.upl.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Replaces all {@link Stub}s with their corresponding server implementation.
 * @see Stub#getServerImplementation()
 */
public class ServerObjectInputStream extends ObjectInputStream {

    public ServerObjectInputStream(InputStream in) throws IOException {
        super(in);
        enableResolveObject(true);
    }

    public Object resolveObject(Object in) {
        if (in instanceof Stub) {
            return ((Stub) in).getServerImplementation();
        }
        return in;
    }

}