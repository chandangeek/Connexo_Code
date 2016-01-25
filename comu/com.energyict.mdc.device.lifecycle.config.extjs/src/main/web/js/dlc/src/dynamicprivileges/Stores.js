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