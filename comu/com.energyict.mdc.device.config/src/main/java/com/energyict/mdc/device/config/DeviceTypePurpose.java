/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * Models the different kinds of DeviceTypes that we support
 */
public enum DeviceTypePurpose {

    /**
     * A regular DeviceType can be use to create devices with all sorts of DataSources.
     * All communication related functionality will be available. Regular devices can
     * be (but are not limited to) gateways, concentrators, smart meters, modbus meters, ...
     */
    REGULAR,
    /**
     * Datalogger slave device types are designed for 'simple' devices that can only
     * define DataSources. No communication related items are available. The idea behind it
     * is that no actions can be performed on these types of devices. The data they receive
     * will be collected by an actual Datalogger. Data logger slaves are most probably plain
     * pulse counters.
     */
    DATALOGGER_SLAVE,
    /**
     * Submeters are 'simple' LOGICAL devices that can only define DataSources. In fact they represent
     * a plain pulse counter element within a Multi-element enabled device. The data collected by this submeter element
     * is collected by an actual Multi-element device.
     * Submeters give us the same functionality as a data logger slave (=physical device) that we can link and unlink from its 'multi-element' meter.
     * But as they are not real hardware devices, they have no real lifecycle.
     */
    MULTI_ELEMENT_SLAVE;
}