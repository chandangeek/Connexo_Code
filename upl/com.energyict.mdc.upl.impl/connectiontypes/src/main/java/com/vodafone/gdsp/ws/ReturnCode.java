/**
 * ReturnCode.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class ReturnCode  implements java.io.Serializable {
    private java.lang.String majorReturnCode;

    private java.lang.String minorReturnCode;

    public ReturnCode() {
    }

    public ReturnCode(
           java.lang.String majorReturnCode,
           java.lang.String minorReturnCode) {
           this.majorReturnCode = majorReturnCode;
           this.minorReturnCode = minorReturnCode;
    }


    /**
     * Gets the majorReturnCode value for this ReturnCode.
     * 
     * @return majorReturnCode
     */
    public java.lang.String getMajorReturnCode() {
        return majorReturnCode;
    }


    /**
     * Sets the majorReturnCode value for this ReturnCode.
     * 
     * @param majorReturnCode
     */
    public void setMajorReturnCode(java.lang.String majorReturnCode) {
        this.majorReturnCode = majorReturnCode;
    }


    /**
     * Gets the minorReturnCode value for this ReturnCode.
     * 
     * @return minorReturnCode
     */
    public java.lang.String getMinorReturnCode() {
        return minorReturnCode;
    }


    /**
     * Sets the minorReturnCode value for this ReturnCode.
     * 
     * @param minorReturnCode
     */
    public void setMinorReturnCode(java.lang.String minorReturnCode) {
        this.minorReturnCode = minorReturnCode;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReturnCode)) return false;
        ReturnCode other = (ReturnCode) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.majorReturnCode==null && other.getMajorReturnCode()==null) || 
             (this.majorReturnCode!=null &&
              this.majorReturnCode.equals(other.getMajorReturnCode()))) &&
            ((this.minorReturnCode==null && other.getMinorReturnCode()==null) || 
             (this.minorReturnCode!=null &&
              this.minorReturnCode.equals(other.getMinorReturnCode())));
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
        if (getMajorReturnCode() != null) {
            _hashCode += getMajorReturnCode().hashCode();
        }
        if (getMinorReturnCode() != null) {
            _hashCode += getMinorReturnCode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReturnCode.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "returnCode"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("majorReturnCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "majorReturnCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("minorReturnCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "minorReturnCode"));
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
