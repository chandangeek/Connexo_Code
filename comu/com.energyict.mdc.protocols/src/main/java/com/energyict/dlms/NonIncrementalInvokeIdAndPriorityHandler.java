/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

/**
 * Provides a default implementation of the {@link InvokeIdAndPriorityHandler} interface.
 * This implementation should be compliant to our 'old' InvokeIdAndPriority management
 * (in which the InvokeId is fixed and thus NOT incremented for each request).
 *
 * @author sva
 * @since 19/03/13 - 16:00
 */
public class NonIncrementalInvokeIdAndPriorityHandler implements InvokeIdAndPriorityHandler {

    public static final byte DEFAULT_INVOKE_ID_AND_PRIORITY = (byte) 0x41; // 0x41, 0b01000001 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]

    private InvokeIdAndPriority invokeIdAndPriority;

    public NonIncrementalInvokeIdAndPriorityHandler() {
        this.invokeIdAndPriority = new InvokeIdAndPriority(DEFAULT_INVOKE_ID_AND_PRIORITY);
    }

    public NonIncrementalInvokeIdAndPriorityHandler(byte invokeIdAndPriority) {
        this.invokeIdAndPriority = new InvokeIdAndPriority(invokeIdAndPriority);
    }

    public NonIncrementalInvokeIdAndPriorityHandler(InvokeIdAndPriority invokeIdAndPriority) {
        this.invokeIdAndPriority = invokeIdAndPriority;
    }

    public byte getNextInvokeIdAndPriority() {
        return invokeIdAndPriority.getInvokeIdAndPriorityData();
    }

    public byte getCurrentInvokeIdAndPriority() {
        return invokeIdAndPriority.getInvokeIdAndPriorityData();
    }

    public InvokeIdAndPriority getCurrentInvokeIdAndPriorityObject() {
        return invokeIdAndPriority;
    }

    public boolean validateInvokeId(byte receivedInvokeIdAndPriority) {
        return validateInvokeId(getCurrentInvokeIdAndPriority(), receivedInvokeIdAndPriority);
    }

    public boolean validateInvokeId(final byte sentInvokeIdAndPriority, final byte receivedInvokeIdAndPriority) {
        return true;
    }
}
