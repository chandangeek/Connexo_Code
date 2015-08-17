Ext.define('Mdc.dynamicprivileges.Stores', {
    singleton: true,

    required: [
        'Mdc.store.DeviceStatePrivileges',
        'Mdc.store.DeviceCommandPrivileges'
    ],

    deviceStateStore: [
        'Mdc.store.DeviceStatePrivileges'
    ],

    all: [
        'Mdc.store.DeviceStatePrivileges',
        'Mdc.store.DeviceCommandPrivileges'
    ]
});
