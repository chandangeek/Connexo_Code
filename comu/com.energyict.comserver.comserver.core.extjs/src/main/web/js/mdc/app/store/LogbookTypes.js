Ext.define('Mdc.store.LogbookTypes', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LogbookType',
    requires: [
        'Mdc.model.LogbookType'
    ],
    model: 'Mdc.model.LogbookType',
    storeId: 'LogbookTypes',
    proxy: {
        type: 'rest',
        url: '../../api/mds/logbooktypes',
        reader: {
            type: 'json',
            root: 'logbookTypes'
        }
    }
});
