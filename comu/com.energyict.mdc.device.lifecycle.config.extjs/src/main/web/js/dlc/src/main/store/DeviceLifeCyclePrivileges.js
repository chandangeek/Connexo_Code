Ext.define('Dlc.main.store.DeviceLifeCyclePrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{deviceLifeCycleId}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(params) {
            this.url = this.urlTpl.replace('{deviceLifeCycleId}', params.deviceLifeCycleId);
        }
    }
});