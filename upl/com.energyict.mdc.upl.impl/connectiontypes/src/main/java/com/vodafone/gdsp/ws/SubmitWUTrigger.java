/**
 * SubmitWUTrigger.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class SubmitWUTrigger  implements java.io.Serializable {
    private java.lang.String deviceId;

    private java.lang.String sourceId;

    private java.lang.String triggerType;

    private java.lang.String operatorName;

    private java.lang.String MSISDNNumber;

    public SubmitWUTrigger() {
    }

    public SubmitWUTrigger(
           java.lang.String deviceId,
           java.lang.String sourceId,
           java.lang.String triggerType,
           java.lang.String operatorName,
           java.lang.String MSISDNNumber) {
           this.deviceId = deviceId;
           this.sourceId = sourceId;
           this.triggerType = triggerType;
           this.operatorName = operatorName;
           this.MSISDNNumber = MSISDNNumber;
    }


    /**
     * Gets the deviceId value for this SubmitWUTrigger.
     * 
     * @return deviceId
     */
    public java.lang.String getDeviceId() {
        return deviceId;
    }


    /**
     * Sets the deviceId value for this SubmitWUTrigger.
     * 
     * @param deviceId
     */
    public void setDeviceId(java.lang.String deviceId) {
        this.deviceId = deviceId;
    }


    /**
     * Gets the sourceId value for this SubmitWUTrigger.
     * 
     * @return sourceId
     */
    public java.lang.String getSourceId() {
        return sourceId;
    }


    /**
     * Sets the sourceId value for this SubmitWUTrigger.
     * 
     * @param sourceId
     */
    public void setSourceId(java.lang.String sourceId) {
        this.sourceId = sourceId;
    }


    /**
     * Gets the triggerType value for this SubmitWUTrigger.
     * 
     * @return triggerType
     */
    public java.lang.String getTriggerType() {
        return triggerType;
    }


    /**
     * Sets the triggerType value for this SubmitWUTrigger.
     * 
     * @param triggerType
     */
    public void setTriggerType(java.lang.String triggerType) {
        this.triggerType = triggerType;
    }


    /**
     * Gets the operatorName value for this SubmitWUTrigger.
     * 
     * @return operatorName
     */
    public java.lang.String getOperatorName() {
        return operatorName;
    }


    /**
     * Sets the operatorName value for this SubmitWUTrigger.
     * 
     * @param operatorName
     */
    public void setOperatorName(java.lang.String operatorName) {
        this.operatorName = operatorName;
    }


    /**
     * Gets the MSISDNNumber value for this SubmitWUTrigger.
     * 
     * @return MSISDNNumber
     */
    public java.lang.String getMSISDNNumber() {
        return MSISDNNumber;
    }


    /**
     * Sets the MSISDNNumber value for this SubmitWUTrigger.
     * 
     * @param MSISDNNumber
     */
    public void setMSISDNNumber(java.lang.String MSISDNNumber) {
        this.MSISDNNumber = MSISDNNumber;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmitWUTrigger)) return false;
        SubmitWUTrigger other = (SubmitWUTrigger) obj;
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
            ((this.sourceId==null && other.getSourceId()==null) || 
             (this.sourceId!=null &&
              this.sourceId.equals(other.getSourceId()))) &&
            ((this.triggerType==null && other.getTriggerType()==null) || 
             (this.triggerType!=null &&
              this.triggerType.equals(other.getTriggerType()))) &&
            ((this.operatorName==null && other.getOperatorName()==null) || 
             (this.operatorName!=null &&
              this.operatorName.equals(other.getOperatorName()))) &&
            ((this.MSISDNNumber==null && other.getMSISDNNumber()==null) || 
             (this.MSISDNNumber!=null &&
              this.MSISDNNumber.equals(other.getMSISDNNumber())));
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
        if (getSourceId() != null) {
            _hashCode += getSourceId().hashCode();
        }
        if (getTriggerType() != null) {
            _hashCode += getTriggerType().hashCode();
        }
        if (getOperatorName() != null) {
            _hashCode += getOperatorName().hashCode();
        }
        if (getMSISDNNumber() != null) {
            _hashCode += getMSISDNNumber().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubmitWUTrigger.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "submitWUTrigger"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deviceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "deviceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sourceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "operatorName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MSISDNNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MSISDNNumber"));
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
