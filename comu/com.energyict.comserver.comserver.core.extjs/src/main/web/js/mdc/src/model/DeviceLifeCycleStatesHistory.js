Ext.define('Mdc.model.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Model',
    fields: [
        'from',
        'to',
        'author',
        'modTime',
        'type'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mrid}/history/devicelifecyclechanges',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mrid}', params.mRID);
        }
    }
});
