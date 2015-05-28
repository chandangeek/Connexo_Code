Ext.define('Mdc.store.ValidationBlocks', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationBlock',
    proxy: {
        type: 'rest',
 //       urlTpl: '/api/ddr/devices/{mRID}/channels/{id}/datavalidationissues/{id}/validationblocks',
        url: '/apps/mdc/fakedata/ValidationBlocks.json',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'validationBlocks',
            idProperty: 'startTime'
        }
    }
});
