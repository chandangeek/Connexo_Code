Ext.define('Mdc.store.DeviceCommandPrivileges', {
    extend: 'Ext.data.Store',

    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/devicemessages/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID));
        }

    }
});