package com.energyict.mdc.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Implementation of a type ID for types that have no subtypes. It is externalizable as a trade off for flex. We don't want the implementation details
 * to be public (getters/setters), so we have to do some serialization ourselves.
 *
 * @author alex
 */
public class HardTypeId extends AbstractTypeId {

    /**
     * The factory ID.
     */
    private int factoryId;

    /**
     * Default constructor. We need this one for externalization.
     */
    public HardTypeId() {
        super();
    }

    /**
     * Create a new instance using the given factory.
     *
     * @param factoryId The factory ID.
     */
    public HardTypeId(final int factoryId) {
        super();

        this.factoryId = factoryId;
    }

    /**
     * {@inheritDoc}
     */
    public final BusinessObjectFactory getFactory() {
        return Environment.DEFAULT.get().findFactory(this.factoryId);
    }

    /**
     * {@inheritDoc}
     */
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        this.factoryId = in.readInt();
    }

    /**
     * {@inheritDoc}
     */
    public final void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(this.factoryId);
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        return "Hard type ID, factory ID [" + this.factoryId + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + factoryId;
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
        final HardTypeId other = (HardTypeId) obj;
        return factoryId == other.factoryId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFactoryId() {
        return this.factoryId;
    }
}
