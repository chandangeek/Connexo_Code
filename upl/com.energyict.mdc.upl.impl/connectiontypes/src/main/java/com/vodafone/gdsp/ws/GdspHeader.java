/**
 * GdspHeader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class GdspHeader  implements java.io.Serializable {
    private com.vodafone.gdsp.ws.GdspCredentials gdspCredentials;

    public GdspHeader() {
    }

    public GdspHeader(
           com.vodafone.gdsp.ws.GdspCredentials gdspCredentials) {
           this.gdspCredentials = gdspCredentials;
    }


    /**
     * Gets the gdspCredentials value for this GdspHeader.
     * 
     * @return gdspCredentials
     */
    public com.vodafone.gdsp.ws.GdspCredentials getGdspCredentials() {
        return gdspCredentials;
    }


    /**
     * Sets the gdspCredentials value for this GdspHeader.
     * 
     * @param gdspCredentials
     */
    public void setGdspCredentials(com.vodafone.gdsp.ws.GdspCredentials gdspCredentials) {
        this.gdspCredentials = gdspCredentials;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GdspHeader)) return false;
        GdspHeader other = (GdspHeader) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.gdspCredentials==null && other.getGdspCredentials()==null) || 
             (this.gdspCredentials!=null &&
              this.gdspCredentials.equals(other.getGdspCredentials())));
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
        if (getGdspCredentials() != null) {
            _hashCode += getGdspCredentials().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GdspHeader.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "gdspHeader"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gdspCredentials");
        elemField.setXmlName(new javax.xml.namespace.QName("", "gdspCredentials"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "gdspCredentials"));
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
