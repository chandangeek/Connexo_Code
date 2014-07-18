Ext.define('Mdc.store.MeasurementTypesToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    autoload: false,
    pageSize: 1000,
    proxy: {
        type: 'rest',
        url: '/api/dtc/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});