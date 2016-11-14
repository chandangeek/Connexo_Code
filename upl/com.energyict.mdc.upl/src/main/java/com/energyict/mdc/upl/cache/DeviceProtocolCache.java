package com.energyict.mdc.upl.cache;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * Defines a <i>object</i> that represents a generic cache for a DeviceProtocol.
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public interface DeviceProtocolCache extends Serializable {

    /**
     * Indicates that the content of this object changed during a communication session with a device.
     * The ComServer will use this as an argument to update the content in the DataBase.
     *
     * @return true if the content changed, false otherwise
     */
    boolean contentChanged();

}