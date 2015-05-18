Ext.define('Mdc.model.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Model',
    fields: [
        'fromState',
        'toState',
        'author',
        'modTime'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mrid}/history/devicelifecyclestates',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mrid}', params.mRID);
        }
    }
});
