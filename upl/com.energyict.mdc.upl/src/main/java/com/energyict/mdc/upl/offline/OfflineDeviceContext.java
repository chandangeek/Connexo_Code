package com.energyict.mdc.upl.offline;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

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

    boolean needsFirmwareVersions();

    boolean needsTouCalendar();

}
