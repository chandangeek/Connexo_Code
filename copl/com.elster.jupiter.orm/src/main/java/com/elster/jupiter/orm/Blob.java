/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The representation in the Connexo platform of a SQL <code>BLOB</code> value.
 * An SQL <code>BLOB</code> is a built-in type that stores a
 * Binary Large Object as a column value in a row of a database table.
 * <p>
 * As this interface is largely inspired by and dependent upon
 * the underlying SQL layer, the mechanism used to reference
 * bytes inside a Blob will be the same as in SQL.
 * In other words, the first byte will have position 1,
 * the second byte will have position 2 and so forth.
 * <p>
 * Use the factory methods found in {@link SimpleBlob}
 * to initialize persistent fields on this type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-06 (14:11)
 */
@ProviderType
public interface Blob {

    /**
     * Returns the number of bytes contained in the Blob.
     *
     * @return The number of bytes
     */
    long length();

    /**
     * Retrieves the contents of this Blob as a stream.
     *
     * @return The stream of bytes
     */
    InputStream getBinaryStream ();

    /**
     * Clears the contents of this Blob.
     * After this call, {@link #length()} should return 0 (zero)
     * and any InputStream will be empty.
     */
    void clear();

    /**
     * Creates a stream that can be used to change the contents of this Blob.
     * The bytes written to the stream will overwrite the existing contents.
     * If the end of the existing contents is reached while writing to the stream,
     * then the length of this Blob will be increased to accommodate the extra bytes.
     * If you intend to replace the entire contents, it may be a good idea
     * to clear the contents first.
     *
     * @return an <code>OutputStream</code> object to which data can be written
     */
    OutputStream setBinaryStream();

}