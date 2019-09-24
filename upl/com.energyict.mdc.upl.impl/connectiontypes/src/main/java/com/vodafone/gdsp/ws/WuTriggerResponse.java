/**
 * WuTriggerResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class WuTriggerResponse  implements java.io.Serializable {
    private java.lang.String deviceId;

    private com.vodafone.gdsp.ws.ReturnCode returnCode;

    private java.lang.String triggerType;

    public WuTriggerResponse() {
    }

    public WuTriggerResponse(
           java.lang.String deviceId,
           com.vodafone.gdsp.ws.ReturnCode returnCode,
           java.lang.String triggerType) {
           this.deviceId = deviceId;
           this.returnCode = returnCode;
           this.triggerType = triggerType;
    }


    /**
     * Gets the deviceId value for this WuTriggerResponse.
     * 
     * @return deviceId
     */
    public java.lang.String getDeviceId() {
        return deviceId;
    }


    /**
     * Sets the deviceId value for this WuTriggerResponse.
     * 
     * @param deviceId
     */
    public void setDeviceId(java.lang.String deviceId) {
        this.deviceId = deviceId;
    }


    /**
     * Gets the returnCode value for this WuTriggerResponse.
     * 
     * @return returnCode
     */
    public com.vodafone.gdsp.ws.ReturnCode getReturnCode() {
        return returnCode;
    }


    /**
     * Sets the returnCode value for this WuTriggerResponse.
     * 
     * @param returnCode
     */
    public void setReturnCode(com.vodafone.gdsp.ws.ReturnCode returnCode) {
        this.returnCode = returnCode;
    }


    /**
     * Gets the triggerType value for this WuTriggerResponse.
     * 
     * @return triggerType
     */
    public java.lang.String getTriggerType() {
        return triggerType;
    }


    /**
     * Sets the triggerType value for this WuTriggerResponse.
     * 
     * @param triggerType
     */
    public void setTriggerType(java.lang.String triggerType) {
        this.triggerType = triggerType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WuTriggerResponse)) return false;
        WuTriggerResponse other = (WuTriggerResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.deviceId==null && other.getDeviceId()==null) || 
             (this.deviceId!=null &&
              this.deviceId.equals(other.getDeviceId()))) &&
            ((this.returnCode==null && other.getReturnCode()==null) || 
             (this.returnCode!=null &&
              this.returnCode.equals(other.getReturnCode()))) &&
            ((this.triggerType==null && other.getTriggerType()==null) || 
             (this.triggerType!=null &&
              this.triggerType.equals(other.getTriggerType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getDeviceId() != null) {
            _hashCode += getDeviceId().hashCode();
        }
        if (getReturnCode() != null) {
            _hashCode += getReturnCode().hashCode();
        }
        if (getTriggerType() != null) {
            _hashCode += getTriggerType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WuTriggerResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "wuTriggerResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deviceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "deviceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("returnCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "returnCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "returnCode"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("triggerType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "triggerType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
