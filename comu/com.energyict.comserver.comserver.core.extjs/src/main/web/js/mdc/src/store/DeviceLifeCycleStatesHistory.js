Ext.define('Mdc.store.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceLifeCycleStatesHistory',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mrid}/history/devicelifecyclestates',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleStateChanges'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mrid}', params.mRID);
        }
    }
});
