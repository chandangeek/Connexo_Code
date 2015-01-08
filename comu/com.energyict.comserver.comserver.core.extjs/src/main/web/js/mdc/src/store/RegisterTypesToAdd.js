Ext.define('Mdc.store.RegisterTypesToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    buffered: true,
    proxy: {
        type: 'rest',
        url: '/api/dtc/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});