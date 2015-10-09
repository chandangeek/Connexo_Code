/**
 * @class Uni.store.search.Fields
 */
Ext.define('Uni.store.search.Fields', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Field',
    storeId: 'Uni.store.search.Fields',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'model'
        }
    }
});