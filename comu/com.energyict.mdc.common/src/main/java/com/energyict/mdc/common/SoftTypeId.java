package com.energyict.mdc.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * Soft type ID. Used for types that have subtypes, such as folder types, rtu types and virtual meter types. It is externalizable because we want to
 * be able to ship this to Flex and the likes without sacrificing data hiding and making the guts of the class public.
 *
 * @author alex
 */
public class SoftTypeId extends AbstractTypeId {

    /**
     * Default constructor, needed for serialization.
     */
    public SoftTypeId() {
        super();
    }

    /**
     * The factory ID.
     */
    private int factoryId;

    private transient BusinessObjectFactory factory;
    private transient boolean factoryUnKnown = false;

    /**
     * The handle.
     */
    private byte[] handle;

    public SoftTypeId (int factoryId, int objectTypeId) {
        this(factoryId, PrimaryKeyExternalRepresentationConvertor.intToBytes(objectTypeId));
    }

    /**
     * Create a new instance for the given factory ID and the given handle.
     *
     * @param factoryId The factory ID.
     * @param handle    The handle.
     */
    public SoftTypeId(final int factoryId, final byte[] handle) {
        super();

        this.factoryId = factoryId;
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     */
    public final BusinessObjectFactory getFactory() {
        if (this.factory == null && !this.factoryUnKnown) {
            final BusinessObject object = Environment.DEFAULT.get().findFactory(this.factoryId).findByHandle(this.handle);

            if (object instanceof BusinessObjectFactory) {
                this.factory = (BusinessObjectFactory) object;
            } else {
                this.factoryUnKnown = true;
                String message;
                if (object == null) {
                    message = "No BusinessObjectFactory found for id " + this.factoryId + "!";
                } else {
                    message = "This type ID specifies [" + object.getClass().getName() + "] as it's factory object, but the object does not implement BusinessObjectFactory !";
                }
                throw new IllegalStateException(message);
            }
        }
        return factory;
    }

    public boolean hasUnknownFactory() {
        return this.factoryUnKnown;
    }

    /**
     * {@inheritDoc}
     */
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        this.factoryId = in.readInt();
        this.factory = null;

        final int handleLength = in.readInt();

        if (this.handle == null) {
            this.handle = new byte[handleLength];
        }

        // READ Carefully the contract of in.read -> will only block until some data is available
        // Fixes the BGB issue
        int numberOfBytesToRead = handleLength;
        int offSet = 0;

        while (numberOfBytesToRead > 0) {
            final int currentBytesRead = in.read(this.handle, offSet, numberOfBytesToRead);
            numberOfBytesToRead -= currentBytesRead;
            offSet += currentBytesRead;
        }

    }

    /**
     * {@inheritDoc}
     */
    public final void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(this.factoryId);
        out.writeInt(this.handle.length);
        out.write(this.handle);
        out.flush();
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        return "Soft type ID, factory ID [" + this.factoryId + "], handle [" + Arrays.toString(this.handle) + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + factoryId;
        result = prime * result + Arrays.hashCode(handle);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SoftTypeId other = (SoftTypeId) obj;
        if (factoryId != other.factoryId) {
            return false;
        }
        if (!Arrays.equals(handle, other.handle)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFactoryId() {
        return this.factoryId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getHandle() {
        return this.handle;
    }

    public String getSoftTypeName() {
        return ((NamedBusinessObject) getFactory()).getName();
    }

    public int getSubTypeId() {
        return PrimaryKeyExternalRepresentationConvertor.intFromBytes(handle);
    }
}
