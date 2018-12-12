/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * Types of actions that can be performed on entities of a Device when one wants to
 * change the configuration of a Device
 */
public enum DeviceConfigChangeActionType {
    ADD,
    REMOVE,
    MATCH,
    CONFLICT
}
