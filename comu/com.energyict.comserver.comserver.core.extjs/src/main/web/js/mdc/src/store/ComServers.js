Ext.define('Mdc.store.ComServers',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComServer'
    ],
    model: 'Mdc.model.ComServer',
    storeId: 'ComServers',
    /*sorters: [{
       property: 'name',
       direction: 'ASC'
    }],*/
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/mdc/comservers',
        reader: {
            type: 'json',
            root: 'data'
        }/*,
        simpleSortMode: true*/
    }
});
