Ext.define('Mdc.store.ConnectionStrategies', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.ConnectionStrategy'
    ],
    model: 'Mdc.model.field.ConnectionStrategy',
    storeId: 'ConnectionStrategies',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/field/connectionStrategy',
        reader: {
            type: 'json',
            root: 'connectionStrategies'
        }
    }
});
