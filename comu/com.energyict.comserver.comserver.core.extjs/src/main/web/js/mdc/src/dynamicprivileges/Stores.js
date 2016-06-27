Ext.define('Mdc.dynamicprivileges.Stores', {
    singleton: true,

    required: [
        'Mdc.store.DeviceStatePrivileges',
        'Mdc.store.DeviceCommandPrivileges',
        'Mdc.store.DeviceTypeCapabilities'
    ],

    deviceStateStore: [
        'Mdc.store.DeviceStatePrivileges'
    ],

    all: [
        'Mdc.store.DeviceStatePrivileges',
        'Mdc.store.DeviceCommandPrivileges'
    ],

    deviceTypeCapabilitiesStore: [
        'Mdc.store.DeviceTypeCapabilities'
    ]
});
