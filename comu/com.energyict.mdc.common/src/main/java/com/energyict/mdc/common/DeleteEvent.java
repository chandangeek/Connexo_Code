package com.energyict.mdc.common;

/**
 * <code>BussinessEvent</code> that signals a deletion of a <code>BusinessObject</code>.
 */

public class DeleteEvent extends AbstractBusinessEvent<BusinessObject> {

    /**
     * Creates a new instance of a <code>DeleteEvent</code>.
     *
     * @param source the <code>BusinessObject</code> being deleted.
     */
    public DeleteEvent(BusinessObject source) {
        super(source);
    }

}
