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
    
    /**
     * Set the IIAP to the default 0x81 value to be compatible with older protocols
     */
    public InvokeIdAndPriority(){
    	this.invokeIdAndPriority = (byte)0x81; // Priority=High, invoke-id-zero=1, UNCONFIRMED
    }
    
    /**
     * Set the IIAP to the value of your choice, see GreenBook for documentation
     * @param iiap
     */
    public InvokeIdAndPriority(byte iiap){
    	this.invokeIdAndPriority = iiap;
    }
	
    /**
     * Set the priority bit (bit 7);
     * 0 = normal, 1 = high 
     * @param priority
     * @throws DLMSConnectionException if priority isn't valid
     */
    public void setPriority(int priority) throws DLMSConnectionException{
    	if(priority == PRIORITY_NORMAL){
    		this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority&0x7F);
    	} else if(priority == PRIORITY_HIGH){
    		this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority|0x80);
    	} else {
    		throw new DLMSConnectionException("Not a valid priority bit: " + priority);
    	}
    }
    
    /**
     * Set the serviceClass bit (bit 6);
     * 0 = Unconfirmed, 1 = Confirmed 
     * @param serviceClass
     * @throws DLMSConnectionException if serviceClass isn't valid
     */
    public void setServiceClass(int serviceClass) throws DLMSConnectionException{
    	if(serviceClass == SERVICE_CLASS_UNCONFIRMED){
    		this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority&0xBF);
    	} else if(serviceClass == SERVICE_CLASS_CONFIRMED){
    		this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority|0x40);
    	} else {
    		throw new DLMSConnectionException("Not a valid serviceClass bit: " + serviceClass);
    	}
    }
    
    /**
     * Set the invoke-Id bit (0-1-2-3);
     * @param invokeId
     * @throws DLMSConnectionException if invokeId isn't valid}
     */
    public void setTheInvokeId(int invokeId) throws DLMSConnectionException{
    	
    	if(invokeId > 4 || invokeId < 0){
    		throw new DLMSConnectionException("Not a valid invokeId bit: " + invokeId);
    	}
    	
    	byte mask = (byte)0xF0;
    	this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority&mask);
    	mask = (byte) Math.pow(2, invokeId);
    	this.invokeIdAndPriority = (byte) (this.invokeIdAndPriority|mask);
    }
    
    public byte getInvokeIdAndPriorityData(){
    	return this.invokeIdAndPriority;
    }
    
}
