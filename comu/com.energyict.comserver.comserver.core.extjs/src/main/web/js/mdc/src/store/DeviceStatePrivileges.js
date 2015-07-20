Ext.define('Mdc.store.DeviceStatePrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID);
        }

    }
});