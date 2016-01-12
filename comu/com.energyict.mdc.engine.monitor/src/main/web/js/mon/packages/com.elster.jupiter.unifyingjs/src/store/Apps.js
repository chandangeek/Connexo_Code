/**
 * @class Uni.store.Apps
 */
Ext.define('Uni.store.Apps', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.App',
    storeId: 'apps',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/apps/apps',
        reader: {
            type: 'json',
            root: ''
        }
    },

    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});