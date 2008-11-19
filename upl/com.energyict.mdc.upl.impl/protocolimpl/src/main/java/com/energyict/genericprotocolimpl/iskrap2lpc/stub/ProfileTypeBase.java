/**
 * ProfileTypeBase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class ProfileTypeBase implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ProfileTypeBase(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _LoadProfile1 = "LoadProfile1";
    public static final java.lang.String _LoadProfile2 = "LoadProfile2";
    public static final java.lang.String _BillingProfile = "BillingProfile";
    public static final java.lang.String _ScheduledProfile = "ScheduledProfile";
    public static final ProfileTypeBase LoadProfile1 = new ProfileTypeBase(_LoadProfile1);
    public static final ProfileTypeBase LoadProfile2 = new ProfileTypeBase(_LoadProfile2);
    public static final ProfileTypeBase BillingProfile = new ProfileTypeBase(_BillingProfile);
    public static final ProfileTypeBase ScheduledProfile = new ProfileTypeBase(_ScheduledProfile);
    public java.lang.String getValue() { return _value_;}
    public static ProfileTypeBase fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ProfileTypeBase enumeration = (ProfileTypeBase)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ProfileTypeBase fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ProfileTypeBase.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "ProfileTypeBase"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
