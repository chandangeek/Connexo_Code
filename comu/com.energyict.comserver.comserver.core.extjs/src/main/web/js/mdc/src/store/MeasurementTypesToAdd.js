Ext.define('Mdc.store.MeasurementTypesToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    autoload: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/registertypes',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});