package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.BaseLogBook;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Uniquely identifies a log book that is stored in a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:51)
 */
public interface LogBookIdentifier<T extends BaseLogBook> extends Serializable {

    /**
     * Finds the LogBook that is uniquely identified by this LogBookIdentifier.
     *
     * @return the LogBook
     */
    public T getLogBook();


    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    /**
     * @return the DeviceIdentifier for this LogBookIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier();
}