Ext.define('Dlc.main.store.DeviceLifeCyclePrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/{deviceLifeCycleId}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});