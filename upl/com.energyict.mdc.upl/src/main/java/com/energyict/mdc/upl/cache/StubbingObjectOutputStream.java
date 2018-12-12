package com.energyict.mdc.upl.cache;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Replaces all {@link Replaceable} objects with their corresponding {@link Stub}.
 */
public class StubbingObjectOutputStream extends ObjectOutputStream {

    public StubbingObjectOutputStream(OutputStream in) throws IOException {
        super(in);
        enableReplaceObject(true);
    }

    public Object replaceObject(Object in) {
        if (ignore(in)) {
            return null;
        }
        if (in instanceof Replaceable) {
            return ((Replaceable) in).getReplacement();
        }
        return in;
    }

    protected boolean ignore(Object in) {
        return (in instanceof PreparedStatement) || (in instanceof ResultSet);
    }

}