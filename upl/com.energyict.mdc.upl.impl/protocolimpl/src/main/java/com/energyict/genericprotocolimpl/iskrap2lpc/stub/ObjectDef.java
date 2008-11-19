/**
 * ObjectDef.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class ObjectDef  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedShort classId;
    private java.lang.String instanceId;
    private byte attributeId;
    private org.apache.axis.types.UnsignedShort dataId;

    public ObjectDef() {
    }

    public ObjectDef(
           org.apache.axis.types.UnsignedShort classId,
           java.lang.String instanceId,
           byte attributeId,
           org.apache.axis.types.UnsignedShort dataId) {
           this.classId = classId;
           this.instanceId = instanceId;
           this.attributeId = attributeId;
           this.dataId = dataId;
    }


    /**
     * Gets the classId value for this ObjectDef.
     * 
     * @return classId
     */
    public org.apache.axis.types.UnsignedShort getClassId() {
        return classId;
    }


    /**
     * Sets the classId value for this ObjectDef.
     * 
     * @param classId
     */
    public void setClassId(org.apache.axis.types.UnsignedShort classId) {
        this.classId = classId;
    }


    /**
     * Gets the instanceId value for this ObjectDef.
     * 
     * @return instanceId
     */
    public java.lang.String getInstanceId() {
        return instanceId;
    }


    /**
     * Sets the instanceId value for this ObjectDef.
     * 
     * @param instanceId
     */
    public void setInstanceId(java.lang.String instanceId) {
        this.instanceId = instanceId;
    }


    /**
     * Gets the attributeId value for this ObjectDef.
     * 
     * @return attributeId
     */
    public byte getAttributeId() {
        return attributeId;
    }


    /**
     * Sets the attributeId value for this ObjectDef.
     * 
     * @param attributeId
     */
    public void setAttributeId(byte attributeId) {
        this.attributeId = attributeId;
    }


    /**
     * Gets the dataId value for this ObjectDef.
     * 
     * @return dataId
     */
    public org.apache.axis.types.UnsignedShort getDataId() {
        return dataId;
    }


    /**
     * Sets the dataId value for this ObjectDef.
     * 
     * @param dataId
     */
    public void setDataId(org.apache.axis.types.UnsignedShort dataId) {
        this.dataId = dataId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ObjectDef)) return false;
        ObjectDef other = (ObjectDef) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.classId==null && other.getClassId()==null) || 
             (this.classId!=null &&
              this.classId.equals(other.getClassId()))) &&
            ((this.instanceId==null && other.getInstanceId()==null) || 
             (this.instanceId!=null &&
              this.instanceId.equals(other.getInstanceId()))) &&
            this.attributeId == other.getAttributeId() &&
            ((this.dataId==null && other.getDataId()==null) || 
             (this.dataId!=null &&
              this.dataId.equals(other.getDataId())));
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
        if (getClassId() != null) {
            _hashCode += getClassId().hashCode();
        }
        if (getInstanceId() != null) {
            _hashCode += getInstanceId().hashCode();
        }
        _hashCode += getAttributeId();
        if (getDataId() != null) {
            _hashCode += getDataId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ObjectDef.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "ObjectDef"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("classId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ClassId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("instanceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "InstanceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributeId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "AttributeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DataId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"));
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
