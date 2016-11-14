package com.energyict.cbo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Used in the collected DeviceTopology to indicate when a readout slave device was last seen by the gateway/DC.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/04/2016 - 11:17
 */
public class LastSeenDateInfo {

    /**
     * The name of the LastSeenDate general property
     */
    private String propertyName;

    /**
     * The value of the LastSeenDate general property
     */
    private Object propertyValue;

    public LastSeenDateInfo(String propertyName, Object propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public Date toDate(){
       return new Date(toLong());
    }

    public Long toLong() {
        return parse(getPropertyValue());
    }

    @Override
    public String toString(){
        try {
            return toDate().toString();
        }catch (Exception ex){
            return ex.toString();
        }
    }

    /**
     * Parse the value of general property LastSeenDate.
     * Since this property can be configured on different protocols, its type can be different.
     * Currently only LastSeenDate properties of type Integer, Long, BigDecimal, Date and String are supported here.
     */
    public static Long parse(Object lastSeenDateObject){
        if (lastSeenDateObject instanceof Integer) {
            return Long.valueOf((Integer) lastSeenDateObject);
        } else if (lastSeenDateObject instanceof Long) {
            return (Long) lastSeenDateObject;
        } else if (lastSeenDateObject instanceof BigDecimal) {
            return ((BigDecimal) lastSeenDateObject).longValue();
        } else if (lastSeenDateObject instanceof Date) {
            return ((Date) lastSeenDateObject).getTime();   //Epoch
        } else if (lastSeenDateObject instanceof String) {
            try {
                return Long.valueOf((String) lastSeenDateObject);
            } catch (NumberFormatException e) {
                return null;        //Non numeric value is not supported.
            }
        }
        return null;
    }
}