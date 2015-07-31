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
            sorterFn: function (o1, o2) {
                if (    !(o1.get('constraints') && o1.get('constraints').length)
                    &&  !(o2.get('constraints') && o2.get('constraints').length)
                ) {
                    return 0;
                }
                if (o1.get('constraints').length) {
                    if (o1.get('constraints').indexOf(o2.get('name')) >= 0) {
                        return 1;
                    }
                }

                if (o2.get('constraints').length) {
                    if (o2.get('constraints').indexOf(o1.get('name')) >= 0) {
                        return -1;
                    }
                }

                return 0;
            }
        },
        {
            sorterFn: function (o1, o2) {
                return o1.get('displayValue') < o2.get('displayValue') ? -1 : 1;
            }
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