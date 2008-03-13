/**
 * IncludeExcludeMeterList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class IncludeExcludeMeterList  implements java.io.Serializable {
    private com.energyict.genericprotocolimpl.iskrap2lpc.stub.ListTypeDef listType;
    private java.lang.String[] meter;

    public IncludeExcludeMeterList() {
    }

    public IncludeExcludeMeterList(
           com.energyict.genericprotocolimpl.iskrap2lpc.stub.ListTypeDef listType,
           java.lang.String[] meter) {
           this.listType = listType;
           this.meter = meter;
    }


    /**
     * Gets the listType value for this IncludeExcludeMeterList.
     * 
     * @return listType
     */
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.ListTypeDef getListType() {
        return listType;
    }


    /**
     * Sets the listType value for this IncludeExcludeMeterList.
     * 
     * @param listType
     */
    public void setListType(com.energyict.genericprotocolimpl.iskrap2lpc.stub.ListTypeDef listType) {
        this.listType = listType;
    }


    /**
     * Gets the meter value for this IncludeExcludeMeterList.
     * 
     * @return meter
     */
    public java.lang.String[] getMeter() {
        return meter;
    }


    /**
     * Sets the meter value for this IncludeExcludeMeterList.
     * 
     * @param meter
     */
    public void setMeter(java.lang.String[] meter) {
        this.meter = meter;
    }

    public java.lang.String getMeter(int i) {
        return this.meter[i];
    }

    public void setMeter(int i, java.lang.String _value) {
        this.meter[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IncludeExcludeMeterList)) return false;
        IncludeExcludeMeterList other = (IncludeExcludeMeterList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.listType==null && other.getListType()==null) || 
             (this.listType!=null &&
              this.listType.equals(other.getListType()))) &&
            ((this.meter==null && other.getMeter()==null) || 
             (this.meter!=null &&
              java.util.Arrays.equals(this.meter, other.getMeter())));
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
        if (getListType() != null) {
            _hashCode += getListType().hashCode();
        }
        if (getMeter() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMeter());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMeter(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(IncludeExcludeMeterList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "IncludeExcludeMeterList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ListType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "ListTypeDef"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("meter");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Meter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
