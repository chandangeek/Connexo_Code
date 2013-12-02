Ext.define('Uni.store.AppItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.AppItem',
    storeId: 'appitems',
    singleton: true,
    autoLoad: true,

    proxy: {
        type: 'ajax',
        url: '/api/apps/pages',
        reader: {
            type: 'json',
            root: ''
        }
    }
});