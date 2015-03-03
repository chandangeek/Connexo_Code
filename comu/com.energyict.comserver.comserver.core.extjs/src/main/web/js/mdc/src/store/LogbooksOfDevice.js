Ext.define('Mdc.store.LogbooksOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LogbookOfDevice',
    storeId: 'LogbooksOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/logbooks',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000,

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});