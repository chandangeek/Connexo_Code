package com.energyict.mdc.common;

import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.TypeId;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract base class for type id implementations. Contains some functionality that is common to all implementations.
 *
 * @author alex
 */
public abstract class AbstractTypeId implements TypeId {

    /**
     * The name of the type.
     */
    private String typeName;

    /**
     * Create a new instance for the given type name.
     */
    protected AbstractTypeId() {
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isMetaType() {
        return this.getFactory().isMetaTypeFactory();
    }

    /**
     * {@inheritDoc}
     */
    public final String getTypeName() {
        if (this.typeName == null) {
            BusinessObjectFactory factory = getFactory();
            if (factory != null) {
                // if factory == null then probably the module is not licensed
                this.typeName = factory.getType();
            } else {
                this.typeName = "unknown";
            }
        }
        return this.typeName;
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        this.typeName = in.readUTF();
    }

    /**
     * {@inheritDoc}
     */
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(this.getTypeName());
    }

    /** methods for the {@link com.energyict.metadata.DbTypeId DbTypeId}, a composite usertype for embedding TypeId objects into the DB. */

    /**
     * Get the factory id of the TypeId.
     *
     * @return the factory id.
     */
    protected int getFactoryId() {
        return 0;
    }

    /**
     * Get the handle of the 'sub' factory.
     *
     * @return The handle of the 'sub' factory.
     */
    protected byte[] getHandle() {
        return null;
    }

    /* default implementation, SoftTypeId will return the subtype id contained in the handle */
    public int getSubTypeId() {
        return 0;
    }
}
