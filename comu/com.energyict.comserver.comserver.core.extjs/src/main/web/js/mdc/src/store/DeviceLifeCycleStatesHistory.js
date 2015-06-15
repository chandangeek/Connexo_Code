Ext.define('Mdc.store.DeviceLifeCycleStatesHistory', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceLifeCycleStatesHistory',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mrid}/history/devicelifecyclechanges',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleChanges'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mrid}', params.mRID);
        }
    }
});
