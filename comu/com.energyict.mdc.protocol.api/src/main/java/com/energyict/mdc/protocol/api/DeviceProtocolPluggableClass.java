/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.List;

/**
 * Models a {@link DeviceProtocol} that was registered in the HeadEnd as a {@link PluggableClass}.
 * <p>
 * <p>
 * Date: 3/07/12
 * Time: 8:58
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface DeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link DeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    String getVersion();

    DeviceProtocol getDeviceProtocol();

    TypedProperties getProperties();

    /**
     * Provide the list of possible {@link ConnectionFunction}s that are provided by the {@link DeviceProtocol}
     * and therefore can be used as {@link ConnectionFunction} on connections of corresponding device(s).<br/>
     * In case no specific connection function(s) are supported, an empty list should be returned.
     *
     * @return the possible ConnectionFunctions that are provided
     */
    List<ConnectionFunction> getProvidedConnectionFunctions();

    /**
     * Provide the list of possible {@link ConnectionFunction}s that can be consumed by this {@link DeviceProtocol}
     * and therefore  can be used as {@link ConnectionFunction} on communication tasks of corresponding device(s).<br/>
     * In case no specific connection function(s) are supported, an empty list should be returned.<br/>
     *
     * @return the possible ConnectionFunctions that can be consumed
     */
    List<ConnectionFunction> getConsumableConnectionFunctions();

    default boolean supportsFileManagement() {
        return this.getDeviceProtocol()
                .getSupportedMessages()
                .stream()
                .map(DeviceMessageSpec::getPropertySpecs)
                .flatMap(Collection::stream)
                .anyMatch(propertySpec -> propertySpec.getValueFactory().getValueTypeName().equalsIgnoreCase(com.energyict.mdc.upl.properties.DeviceMessageFile.class.getName()));
    }

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);
}