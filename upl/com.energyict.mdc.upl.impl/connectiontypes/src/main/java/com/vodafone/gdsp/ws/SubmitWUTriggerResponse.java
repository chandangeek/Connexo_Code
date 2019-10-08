/**
 * SubmitWUTriggerResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class SubmitWUTriggerResponse  implements java.io.Serializable {
    private com.vodafone.gdsp.ws.WuTriggerResponse _return;

    public SubmitWUTriggerResponse() {
    }

    public SubmitWUTriggerResponse(
           com.vodafone.gdsp.ws.WuTriggerResponse _return) {
           this._return = _return;
    }


    /**
     * Gets the _return value for this SubmitWUTriggerResponse.
     * 
     * @return _return
     */
    public com.vodafone.gdsp.ws.WuTriggerResponse get_return() {
        return _return;
    }


    /**
     * Sets the _return value for this SubmitWUTriggerResponse.
     * 
     * @param _return
     */
    public void set_return(com.vodafone.gdsp.ws.WuTriggerResponse _return) {
        this._return = _return;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmitWUTriggerResponse)) return false;
        SubmitWUTriggerResponse other = (SubmitWUTriggerResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._return==null && other.get_return()==null) || 
             (this._return!=null &&
              this._return.equals(other.get_return())));
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
        if (get_return() != null) {
            _hashCode += get_return().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubmitWUTriggerResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "submitWUTriggerResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_return");
        elemField.setXmlName(new javax.xml.namespace.QName("", "return"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "wuTriggerResponse"));
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
