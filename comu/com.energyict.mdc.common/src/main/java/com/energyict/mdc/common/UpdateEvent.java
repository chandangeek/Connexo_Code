package com.energyict.mdc.common;

/**
 * <code>BussinessEvent</code> that signals an update of a <code>BusinessObject</code>.
 */
public class UpdateEvent extends AbstractBusinessEvent<BusinessObject> {

    /**
     * Creates a new instance of an <code>UpdateEvent</code>.
     *
     * @param source the <code>BusinessObject</code> being updated.
     */
    public UpdateEvent(BusinessObject source) {
        super(source);
    }

}
