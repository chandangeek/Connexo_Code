package com.energyict.mdc.upl.offline;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;

/**
 * Copyrights EnergyICT
 * Date: 14/03/13
 * Time: 14:42
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineDeviceContext {

    boolean needsSlaveDevices();

    boolean needsMasterLoadProfiles();

    boolean needsAllLoadProfiles();

    boolean needsLogBooks();

    boolean needsRegisters();

    boolean needsPendingMessages();

    boolean needsSentMessages();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return this.getClass().getName();
    }

    default void setXmlType(String ignore) {
        // For JSON serializing only
    }

}
