/**
 * CosemDateTime.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class CosemDateTime  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedShort year;
    private org.apache.axis.types.UnsignedByte month;
    private org.apache.axis.types.UnsignedByte dayOfMonth;
    private org.apache.axis.types.UnsignedByte dayOfWeek;
    private org.apache.axis.types.UnsignedByte hour;
    private org.apache.axis.types.UnsignedByte minute;

    public CosemDateTime() {
    }

    public CosemDateTime(
           org.apache.axis.types.UnsignedShort year,
           org.apache.axis.types.UnsignedByte month,
           org.apache.axis.types.UnsignedByte dayOfMonth,
           org.apache.axis.types.UnsignedByte dayOfWeek,
           org.apache.axis.types.UnsignedByte hour,
           org.apache.axis.types.UnsignedByte minute) {
           this.year = year;
           this.month = month;
           this.dayOfMonth = dayOfMonth;
           this.dayOfWeek = dayOfWeek;
           this.hour = hour;
           this.minute = minute;
    }


    /**
     * Gets the year value for this CosemDateTime.
     * 
     * @return year
     */
    public org.apache.axis.types.UnsignedShort getYear() {
        return year;
    }


    /**
     * Sets the year value for this CosemDateTime.
     * 
     * @param year
     */
    public void setYear(org.apache.axis.types.UnsignedShort year) {
        this.year = year;
    }


    /**
     * Gets the month value for this CosemDateTime.
     * 
     * @return month
     */
    public org.apache.axis.types.UnsignedByte getMonth() {
        return month;
    }


    /**
     * Sets the month value for this CosemDateTime.
     * 
     * @param month
     */
    public void setMonth(org.apache.axis.types.UnsignedByte month) {
        this.month = month;
    }


    /**
     * Gets the dayOfMonth value for this CosemDateTime.
     * 
     * @return dayOfMonth
     */
    public org.apache.axis.types.UnsignedByte getDayOfMonth() {
        return dayOfMonth;
    }


    /**
     * Sets the dayOfMonth value for this CosemDateTime.
     * 
     * @param dayOfMonth
     */
    public void setDayOfMonth(org.apache.axis.types.UnsignedByte dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }


    /**
     * Gets the dayOfWeek value for this CosemDateTime.
     * 
     * @return dayOfWeek
     */
    public org.apache.axis.types.UnsignedByte getDayOfWeek() {
        return dayOfWeek;
    }


    /**
     * Sets the dayOfWeek value for this CosemDateTime.
     * 
     * @param dayOfWeek
     */
    public void setDayOfWeek(org.apache.axis.types.UnsignedByte dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }


    /**
     * Gets the hour value for this CosemDateTime.
     * 
     * @return hour
     */
    public org.apache.axis.types.UnsignedByte getHour() {
        return hour;
    }


    /**
     * Sets the hour value for this CosemDateTime.
     * 
     * @param hour
     */
    public void setHour(org.apache.axis.types.UnsignedByte hour) {
        this.hour = hour;
    }


    /**
     * Gets the minute value for this CosemDateTime.
     * 
     * @return minute
     */
    public org.apache.axis.types.UnsignedByte getMinute() {
        return minute;
    }


    /**
     * Sets the minute value for this CosemDateTime.
     * 
     * @param minute
     */
    public void setMinute(org.apache.axis.types.UnsignedByte minute) {
        this.minute = minute;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CosemDateTime)) return false;
        CosemDateTime other = (CosemDateTime) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.year==null && other.getYear()==null) || 
             (this.year!=null &&
              this.year.equals(other.getYear()))) &&
            ((this.month==null && other.getMonth()==null) || 
             (this.month!=null &&
              this.month.equals(other.getMonth()))) &&
            ((this.dayOfMonth==null && other.getDayOfMonth()==null) || 
             (this.dayOfMonth!=null &&
              this.dayOfMonth.equals(other.getDayOfMonth()))) &&
            ((this.dayOfWeek==null && other.getDayOfWeek()==null) || 
             (this.dayOfWeek!=null &&
              this.dayOfWeek.equals(other.getDayOfWeek()))) &&
            ((this.hour==null && other.getHour()==null) || 
             (this.hour!=null &&
              this.hour.equals(other.getHour()))) &&
            ((this.minute==null && other.getMinute()==null) || 
             (this.minute!=null &&
              this.minute.equals(other.getMinute())));
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
        if (getYear() != null) {
            _hashCode += getYear().hashCode();
        }
        if (getMonth() != null) {
            _hashCode += getMonth().hashCode();
        }
        if (getDayOfMonth() != null) {
            _hashCode += getDayOfMonth().hashCode();
        }
        if (getDayOfWeek() != null) {
            _hashCode += getDayOfWeek().hashCode();
        }
        if (getHour() != null) {
            _hashCode += getHour().hashCode();
        }
        if (getMinute() != null) {
            _hashCode += getMinute().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CosemDateTime.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/type", "CosemDateTime"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("year");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Year"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("month");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Month"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dayOfMonth");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DayOfMonth"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dayOfWeek");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DayOfWeek"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hour");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Hour"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("minute");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Minute"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
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
