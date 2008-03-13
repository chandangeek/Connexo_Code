/**
 * LimitDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class LimitDefinition  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedInt contractualLimit;
    private org.apache.axis.types.UnsignedInt exceedingPeriod;

    public LimitDefinition() {
    }

    public LimitDefinition(
           org.apache.axis.types.UnsignedInt contractualLimit,
           org.apache.axis.types.UnsignedInt exceedingPeriod) {
           this.contractualLimit = contractualLimit;
           this.exceedingPeriod = exceedingPeriod;
    }


    /**
     * Gets the contractualLimit value for this LimitDefinition.
     * 
     * @return contractualLimit
     */
    public org.apache.axis.types.UnsignedInt getContractualLimit() {
        return contractualLimit;
    }


    /**
     * Sets the contractualLimit value for this LimitDefinition.
     * 
     * @param contractualLimit
     */
    public void setContractualLimit(org.apache.axis.types.UnsignedInt contractualLimit) {
        this.contractualLimit = contractualLimit;
    }


    /**
     * Gets the exceedingPeriod value for this LimitDefinition.
     * 
     * @return exceedingPeriod
     */
    public org.apache.axis.types.UnsignedInt getExceedingPeriod() {
        return exceedingPeriod;
    }


    /**
     * Sets the exceedingPeriod value for this LimitDefinition.
     * 
     * @param exceedingPeriod
     */
    public void setExceedingPeriod(org.apache.axis.types.UnsignedInt exceedingPeriod) {
        this.exceedingPeriod = exceedingPeriod;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LimitDefinition)) return false;
        LimitDefinition other = (LimitDefinition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.contractualLimit==null && other.getContractualLimit()==null) || 
             (this.contractualLimit!=null &&
              this.contractualLimit.equals(other.getContractualLimit()))) &&
            ((this.exceedingPeriod==null && other.getExceedingPeriod()==null) || 
             (this.exceedingPeriod!=null &&
              this.exceedingPeriod.equals(other.getExceedingPeriod())));
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
        if (getContractualLimit() != null) {
            _hashCode += getContractualLimit().hashCode();
        }
        if (getExceedingPeriod() != null) {
            _hashCode += getExceedingPeriod().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LimitDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "LimitDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contractualLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ContractualLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exceedingPeriod");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ExceedingPeriod"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
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
