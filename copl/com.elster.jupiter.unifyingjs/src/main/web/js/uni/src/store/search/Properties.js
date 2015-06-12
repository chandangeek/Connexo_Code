/**
 * @class Uni.store.search.Properties
 */
Ext.define('Uni.store.search.Properties', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Property',
    storeId: 'Uni.store.search.Properties',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/jsr/search/{domainLinkHref}/searchcriteria', // Gets overwritten anyways.
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
    ],

    // TODO Remove when back-end fix has been implemented for double properties.
    listeners: {
        load: function (store) {
            // Using a map of already used names.
            var hits = {};

            store.filterBy(function (record) {
                var name = record.get('name');
                if (hits[name]) {
                    return false;
                } else {
                    hits[name] = true;
                    return true;
                }
            });

            // Delete the filtered out records.
            delete store.snapshot;
        }
    }
});