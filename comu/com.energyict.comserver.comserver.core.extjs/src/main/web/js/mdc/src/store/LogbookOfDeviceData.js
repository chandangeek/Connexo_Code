Ext.define('Mdc.store.LogbookOfDeviceData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LogbookOfDeviceData',
    storeId: 'LogbookOfDeviceData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/logbooks/{logbookId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{logbookId}', params.logbookId);
        }
    }
});