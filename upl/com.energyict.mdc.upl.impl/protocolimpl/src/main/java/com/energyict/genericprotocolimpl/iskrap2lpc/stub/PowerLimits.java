/**
 * PowerLimits.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class PowerLimits  implements java.io.Serializable {
    private com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition energyLimit;
    private com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition powerLimit;

    public PowerLimits() {
    }

    public PowerLimits(
           com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition energyLimit,
           com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition powerLimit) {
           this.energyLimit = energyLimit;
           this.powerLimit = powerLimit;
    }


    /**
     * Gets the energyLimit value for this PowerLimits.
     * 
     * @return energyLimit
     */
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition getEnergyLimit() {
        return energyLimit;
    }


    /**
     * Sets the energyLimit value for this PowerLimits.
     * 
     * @param energyLimit
     */
    public void setEnergyLimit(com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition energyLimit) {
        this.energyLimit = energyLimit;
    }


    /**
     * Gets the powerLimit value for this PowerLimits.
     * 
     * @return powerLimit
     */
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition getPowerLimit() {
        return powerLimit;
    }


    /**
     * Sets the powerLimit value for this PowerLimits.
     * 
     * @param powerLimit
     */
    public void setPowerLimit(com.energyict.genericprotocolimpl.iskrap2lpc.stub.LimitDefinition powerLimit) {
        this.powerLimit = powerLimit;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PowerLimits)) return false;
        PowerLimits other = (PowerLimits) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.energyLimit==null && other.getEnergyLimit()==null) || 
             (this.energyLimit!=null &&
              this.energyLimit.equals(other.getEnergyLimit()))) &&
            ((this.powerLimit==null && other.getPowerLimit()==null) || 
             (this.powerLimit!=null &&
              this.powerLimit.equals(other.getPowerLimit())));
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
        if (getEnergyLimit() != null) {
            _hashCode += getEnergyLimit().hashCode();
        }
        if (getPowerLimit() != null) {
            _hashCode += getPowerLimit().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PowerLimits.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "PowerLimits"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("energyLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EnergyLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "LimitDefinition"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("powerLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PowerLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "LimitDefinition"));
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
