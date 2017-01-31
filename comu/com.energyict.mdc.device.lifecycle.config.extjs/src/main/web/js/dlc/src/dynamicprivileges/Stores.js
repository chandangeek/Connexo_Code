/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.dynamicprivileges.Stores', {
    singleton: true,

    required: [
        'Dlc.main.store.DeviceLifeCyclePrivileges'
    ],

    deviceLifeCycleStore: [
        'Dlc.main.store.DeviceLifeCyclePrivileges'
    ],

    all: [
        'Dlc.main.store.DeviceLifeCyclePrivileges'
    ]
});