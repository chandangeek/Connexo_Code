/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

/**
 * @author sva
 * @since 19/03/13 - 15:51
 */
public interface InvokeIdAndPriorityHandler {

    /**
     * Getter for the next InvokeIdAndPriority byte.
     *
     * @return the InvokeIdAndPriority byte that should be used for the next request.
     */
    public byte getNextInvokeIdAndPriority();

    /**
     * Getter for the current InvokeIdAndPriority byte.
     * This is the InvokeIdAndPriority byte used in our request.
     *
     * @return the current InvokeIdAndPriority byte
     */
    public byte getCurrentInvokeIdAndPriority();

    /**
     * Getter for the current InvokeIdAndPriority object.
     *
     * @return the current InvokeIdAndPriority object
     */
    public InvokeIdAndPriority getCurrentInvokeIdAndPriorityObject();

    /**
     * Validate the InvokeId of the response matches the current InvokeId.
     *
     * @param receivedInvokeIdAndPriority The InvokeId that has been responded by the device in the response frame
     * @return true     When both InvokeId's match, indicating the response frame is valid
     *         false    When the InvokeId's do not match, this indicate the response frame is not valid and should be discarded
     */
    public boolean validateInvokeId(final byte receivedInvokeIdAndPriority);

    /**
     * Validate the InvokeId of the response matches the InvokeId of the request.
     *
     * @param sentInvokeIdAndPriority     The InvokeId that has been send to the device in the request frame
     * @param receivedInvokeIdAndPriority The InvokeId that has been responded by the device in the response frame
     * @return true     When both InvokeId's match, indicating the response frame is valid
     *         false    When the InvokeId's do not match, this indicate the response frame is not valid and should be discarded
     */
    public boolean validateInvokeId(final byte sentInvokeIdAndPriority, final byte receivedInvokeIdAndPriority);

}
