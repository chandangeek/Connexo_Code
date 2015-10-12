/**
 * @class Uni.store.search.Domains
 */
Ext.define('Uni.store.search.Domains', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Domain',
    storeId: 'Uni.store.search.Domains',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/jsr/search',
        reader: {
            type: 'json',
            root: 'domains'
        }
    },

    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});