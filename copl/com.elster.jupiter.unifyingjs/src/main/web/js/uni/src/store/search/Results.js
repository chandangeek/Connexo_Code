/**
 * @class Uni.store.search.Results
 */
Ext.define('Uni.store.search.Results', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    storeId: 'Uni.store.search.Results',
    singleton: true,
    autoLoad: false,
    remoteFilter: true,

    proxy: {
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'searchResults'
        }
    }
});