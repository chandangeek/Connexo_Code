package com.energyict.dlms;

/**
 * Provides an implementation of the {@link InvokeIdAndPriorityHandler} interface, compliant to the DLMS GreenBook.
 * In this implementation, for each request/response combination the InvokeId is incremented.
 *
 * @author sva
 * @since 19/03/13 - 16:12
 */
public class IncrementalInvokeIdAndPriorityHandler implements InvokeIdAndPriorityHandler {

    public static final byte DEFAULT_INVOKE_ID_AND_PRIORITY = (byte) 0x41; // 0x41, 0b01000001 -> [invoke-id = 1, service_class = 1 (confirmed), priority = 0 (normal)]

    private InvokeIdAndPriority invokeIdAndPriority;

    public IncrementalInvokeIdAndPriorityHandler() {
        this.invokeIdAndPriority = new InvokeIdAndPriority(DEFAULT_INVOKE_ID_AND_PRIORITY);
    }

    public IncrementalInvokeIdAndPriorityHandler(byte invokeIdAndPriority) {
        this.invokeIdAndPriority = new InvokeIdAndPriority(invokeIdAndPriority);
    }

    public IncrementalInvokeIdAndPriorityHandler(InvokeIdAndPriority invokeIdAndPriority) {
        this.invokeIdAndPriority = invokeIdAndPriority;
    }

    public byte getNextInvokeIdAndPriority() {
        int currentInvokeId = this.invokeIdAndPriority.getInvokeId();
        int nextInvokeId = (currentInvokeId + 1) % 16;

        try {
            this.invokeIdAndPriority.setTheInvokeId(nextInvokeId);
        } catch (DLMSConnectionException e) {
            // Can be ignored, cause will never happen.
        }

        return this.invokeIdAndPriority.getInvokeIdAndPriorityData();
    }

    public byte getCurrentInvokeIdAndPriority() {
        return invokeIdAndPriority.getInvokeIdAndPriorityData();
    }

    public InvokeIdAndPriority getCurrentInvokeIdAndPriorityObject() {
        return invokeIdAndPriority;
    }


    public boolean validateInvokeId(final byte sentInvokeIdAndPriority, final byte receivedInvokeIdAndPriority) {
        int sentInvokeId = sentInvokeIdAndPriority & 0x0F;
        int receivedInvokeId = receivedInvokeIdAndPriority & 0x0F;

        return (sentInvokeId == receivedInvokeId);
    }

    public boolean validateInvokeId(byte receivedInvokeIdAndPriority) {
        return validateInvokeId(getCurrentInvokeIdAndPriority(), receivedInvokeIdAndPriority);
    }
}
