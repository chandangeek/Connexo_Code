/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.cache;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents a simple object that holds a <i>cache</i> {@link Object} for a specific {@link com.energyict.mdc.upl.meterdata.Device}
 * <p/>
 *
 * Date: 31/08/12
 * Time: 16:30
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface DeviceCache {

    void update();

    void delete();

    /**
     * Gets the id of the Device referring to this Cash
     *
     * @return the id of the Device
     */
    @XmlAttribute
    long getDeviceId();

    /**
     * @return the cache object
     */
    DeviceProtocolCache getCacheObject();

    /**
     * Sets the current cache object
     * @param deviceProtocolCache the current cacheObject
     */
    void setCacheObject(DeviceProtocolCache deviceProtocolCache);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

}