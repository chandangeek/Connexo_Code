package com.energyict.mdc.common;

/**
 * Represents a shadow object for a business object with an unique id
 *
 * @author Karel
 * @see IdBusinessObject
 */
public class IdObjectShadow extends ObjectShadow {

    private int id = 0;

    /**
     * Creates a new instance of IdObjectShadow
     */
    public IdObjectShadow() {
    }

    /**
     * Creates a new instance of IdObjectShadow
     *
     * @param id the object's id
     */
    public IdObjectShadow(int id) {
        this.id = id;
    }

    /**
     * Returns the id of the object shadowed by the receiver
     *
     * @return the shadowed object's id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for the property id.
     * This method is reserved for internal use by business objects.
     * Should not be used by external applications,
     * since object ids are generated automatically by the system.
     *
     * @param id new value for the property id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * prepares the receiver for cloning
     */
    public void prepareCloning() {
        super.prepareCloning();
        this.id = 0;
    }

    /**
     * creates a copy of this shadow that is "prepared" to contain the needed audit information
     *
     * @return a copy of this shadow that will be used to store audit info
     */
    public IdObjectShadow getAuditCopy() {
        try {
            IdObjectShadow clone = (IdObjectShadow) this.clone();
            clone.prepareAudit();
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new ApplicationException(ex);
        }
    }

    /**
     * prepares the receiver for cloning
     */
    protected void prepareAudit() {
    }
}
