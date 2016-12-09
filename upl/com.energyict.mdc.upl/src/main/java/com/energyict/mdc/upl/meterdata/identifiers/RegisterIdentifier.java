package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.Register;

import com.energyict.obis.ObisCode;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * Provides functionality to identify a specific Register by it's ObisCode.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 13:51
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface RegisterIdentifier extends Serializable {

    /**
     * Finds the {@link Register} that is uniquely identified by this RegisterIdentifier.
     * Note that this may throw a runtime exception when the Register could
     * either not be found or multiple registers were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return The Register
     */
    Register findRegister();

    /**
     * Getter for the ObisCode of the register
     */
    @XmlAttribute
    ObisCode getRegisterObisCode();

    /**
     * The type of this identifier.
     */
    RegisterIdentifierType getRegisterIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    List<Object> getParts();

    /**
     * @return the DeviceIdentifier for this RegisterIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}