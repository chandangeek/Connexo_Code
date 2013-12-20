package com.energyict.mdc.common;

/**
 * <code>BussinessEvent</code> that signals a creation of a <code>BusinessObject</code>.
 */
public class CreateEvent extends AbstractBusinessEvent<BusinessObject> {

    /**
     * Creates a new instance of a <code>CreateEvent</code>.
     *
     * @param source the <code>BusinessObject</code> being created.
     */
    public CreateEvent(BusinessObject source) {
        super(source);
    }

}
