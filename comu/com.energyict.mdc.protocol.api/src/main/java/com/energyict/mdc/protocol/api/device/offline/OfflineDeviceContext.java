/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

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