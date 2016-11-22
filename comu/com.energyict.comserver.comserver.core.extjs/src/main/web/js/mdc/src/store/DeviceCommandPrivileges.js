Ext.define('Mdc.store.DeviceCommandPrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/devicemessages/privileges',
        reader: {
            type: 'json',
            root: 'privileges'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});