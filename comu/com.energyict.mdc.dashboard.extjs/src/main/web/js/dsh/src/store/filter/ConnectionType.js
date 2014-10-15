Ext.define('Dsh.store.filter.ConnectionType', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectiontypepluggableclasses',
        reader: {
            type: 'json',
            root: 'connectiontypepluggableclasses'
        }
    }
});
