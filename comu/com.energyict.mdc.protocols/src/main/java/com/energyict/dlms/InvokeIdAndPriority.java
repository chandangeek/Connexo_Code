package com.energyict.dlms;

/**
 * <pre>
 * See GreenBook 6th p178.
 * Invoke-Id-And-Priority      ::= BIT STRING
 * {
 *     invoke-id-zero            (0),
 *     invoke-id-one             (1),
 *     invoke-id-two             (2),
 *     invoke-id-three           (3),
 *     reserved-four             (4),
 *     reserved-five             (5),
 *     service_class             (6),       -- 0 = Unconfirmed, 1 = Confirmed
 *     priority                  (7)        -- 0 = normal, 1 = high
 * }
 * </pre>
 *
 * @author gna
 */
public class InvokeIdAndPriority {

    private byte invokeIdAndPriority;

    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_HIGH = 1;

    public static final int SERVICE_CLASS_UNCONFIRMED = 0;
    public static final int SERVICE_CLASS_CONFIRMED = 1;

    public static final int INVOKE_ID_ZERO = 0;
    public static final int INVOKE_ID_ONE = 1;
    public static final int INVOKE_ID_TWO = 2;
    public static final int INVOKE_ID_THREE = 3;

    public static enum ServiceClass {
        CONFIRMED,
        UNCONFIRMED;
    }

    public static enum Priority {
        NORMAL,
        HIGH;
    }

    /**
     * Set the IIAP to the default 0x81 value to be compatible with older protocols
     */
    public InvokeIdAndPriority() {
        this.invokeIdAndPriority = (byte) 0x41; // Priority=High, invoke-id-zero=1, Confirmed
    }

    /**
     * Set the IIAP to the value of your choice, see GreenBook for documentation
     *
     * @param iiap
     */
    public InvokeIdAndPriority(byte iiap) {
        this.invokeIdAndPriority = iiap;
    }

    /**
     * Set the priority bit (bit 7);
     * 0 = normal, 1 = high
     *
     * @param priority
     * @throws DLMSConnectionException if priority isn't valid
     */
    public void setPriority(int priority) throws DLMSConnectionException {
        if (priority == PRIORITY_NORMAL) {
            this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority & 0x7F);
        } else if (priority == PRIORITY_HIGH) {
            this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority | 0x80);
        } else {
            throw new DLMSConnectionException("Not a valid priority bit: " + priority);
        }
    }

    /**
     * Set the used priority
     *
     * @param priority The used priority
     * @throws DLMSConnectionException if the priority isn't valid (null or unknown)
     */
    public void setPriority(final Priority priority) throws DLMSConnectionException {
        if (priority != null) {
            switch (priority) {
                case NORMAL:
                    setPriority(PRIORITY_NORMAL);
                    return;
                case HIGH:
                    setPriority(PRIORITY_HIGH);
                    return;
            }
        }

        throw new DLMSConnectionException("Not a valid priority bit value [" + priority + "]! ");

    }

    /**
     * Getter for the InvokeId
     *
     * @return The InvokeId
     */
    public final int getInvokeId() {
        return this.invokeIdAndPriority & 0x0F;
    }

    /**
     * Getter for the service class bit (confirmed or unconfirmed)
     *
     * @return The value of the ServiceClass bit
     */
    public final Priority getPriority() {
        return (this.invokeIdAndPriority & 0x80) != 0 ? Priority.HIGH : Priority.NORMAL;
    }

    /**
     * Set the serviceClass bit (bit 6);
     * 0 = Unconfirmed, 1 = Confirmed
     *
     * @param serviceClass
     * @throws DLMSConnectionException if serviceClass isn't valid
     */
    public void setServiceClass(int serviceClass) throws DLMSConnectionException {
        if (serviceClass == SERVICE_CLASS_UNCONFIRMED) {
            this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority & 0xBF);
        } else if (serviceClass == SERVICE_CLASS_CONFIRMED) {
            this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority | 0x40);
        } else {
            throw new DLMSConnectionException("Not a valid serviceClass bit: " + serviceClass);
        }
    }

    /**
     * Set the used service class (confirmed or unconfirmed)
     *
     * @param serviceClass The service class to use
     * @throws DLMSConnectionException if serviceClass isn't valid (null or unknown)
     */
    public final void setServiceClass(final ServiceClass serviceClass) throws DLMSConnectionException {
        if (serviceClass != null) {
            switch (serviceClass) {
                case CONFIRMED:
                    setServiceClass(SERVICE_CLASS_CONFIRMED);
                    return;
                case UNCONFIRMED:
                    setServiceClass(SERVICE_CLASS_UNCONFIRMED);
                    return;
            }
        }

        throw new DLMSConnectionException("Invalid service class [" + serviceClass + "]!");
    }

    /**
     * Getter for the service class bit (confirmed or unconfirmed)
     *
     * @return The value of the ServiceClass bit
     */
    public final ServiceClass getServiceClass() {
        return (this.invokeIdAndPriority & 0x40) != 0 ? ServiceClass.CONFIRMED : ServiceClass.UNCONFIRMED;
    }

    /**
     * Set the invoke-Id bit (0-1-2-3);
     *
     * @param invokeId
     * @throws DLMSConnectionException if invokeId isn't valid}
     */
    public void setTheInvokeId(int invokeId) throws DLMSConnectionException {

        if (invokeId > 15 || invokeId < 0) {
            throw new DLMSConnectionException("Not a valid invokeId bit: " + invokeId);
        }

        byte mask = (byte) 0xF0;
        this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority & mask);
        this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority | invokeId);
    }

    public byte getInvokeIdAndPriorityData() {
        return this.invokeIdAndPriority;
    }

    /**
     * Check if a request from the client needs a response from the device
     * This check is based on the Service class bit, included in the InvokeIdAndPriority
     *
     * @return true if a response is required
     */
    public final boolean needsResponse() {
        return getServiceClass() == ServiceClass.CONFIRMED;
    }

}
