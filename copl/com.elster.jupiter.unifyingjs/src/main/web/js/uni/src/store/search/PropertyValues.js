/**
 * @class Uni.store.search.PropertyValues
 */
Ext.define('Uni.store.search.PropertyValues', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.PropertyValue',

    proxy: {
        type: 'ajax',
        url: '/api/jsr/search/{searchDomain}/searchcriteria/{property}', // Gets overwritten anyways.
        reader: {
            type: 'json',
            root: 'values'
        }
    },

    sorters: [
        {
            property: 'displayValue',
            direction: 'ASC'
        }
    ]
});