/**
 * @class Uni.store.Apps
 */
Ext.define('Uni.store.Apps', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.App',
    storeId: 'apps',
    singleton: true,
    autoLoad: true,

    proxy: {
        type: 'ajax',
        url: '/api/apps',
        reader: {
            type: 'json',
            root: ''
        }
    },

    // TODO Remove the test data when the REST interface is ready.
    data: [
        {
            name: 'Lorem ipsum',
            icon: 'connexo',
            url: '/apps/master/index.html'
        },
        {
            name: 'Vivamus consequat',
            icon: 'devices',
            url: '#'
        },
        {
            name: 'Pellentesque varius',
            icon: '',
            url: '#'
        }
    ]
});