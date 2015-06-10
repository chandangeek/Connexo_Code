/**
 * @class Uni.store.search.Removables
 */
Ext.define('Uni.store.search.Removables', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Property',
    storeId: 'Uni.store.search.Removables',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'properties'
        }
    },

    sorters: [
        {
            property: 'displayValue',
            direction: 'ASC'
        }
    ]
});